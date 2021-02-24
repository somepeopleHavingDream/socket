package org.yangxin.socket.bio.handle;

import org.yangxin.socket.bio.utils.CloseUtils;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 在服务端的对客户端处理的程序
 *
 * @author yangxin
 * 2020/08/03 16:51
 */
@SuppressWarnings("AlibabaAvoidManuallyCreateThread")
public class ClientHandler {

    private final Socket socket;
    private final ClientReadHandler readHandler;
    private final ClientWriteHandler writeHandler;
    private final ClientHandlerCallback clientHandlerCallback;
    private final String clientInfo;

    public ClientHandler(Socket socket, ClientHandlerCallback clientHandlerCallback) throws IOException {
        this.socket = socket;
        this.readHandler = new ClientReadHandler(socket.getInputStream());
        this.writeHandler = new ClientWriteHandler(socket.getOutputStream());
        this.clientHandlerCallback = clientHandlerCallback;
        this.clientInfo = "A[" + socket.getInetAddress().getHostAddress()
                + "] P[" + socket.getPort() + "]";

        System.out.println("新客户端连接：" + clientInfo);
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void exit() {
        readHandler.exit();
        writeHandler.exit();
        CloseUtils.close(socket);
        System.out.println("客户端已退出：" + socket.getInetAddress() +
                " P:" + socket.getPort());
    }

    public void send(String str) {
        writeHandler.send(str);
    }

    /**
     * 读取数据并打印，新开读线程
     */
    public void readToPrint() {
        Thread thread = new Thread(readHandler);
        readHandler.setThread(thread);
        thread.start();
    }

    private void exitBySelf() {
        exit();
        clientHandlerCallback.onSelfClosed(this);
    }

    /**
     * @author yangxin
     * 2020/08/03 16:53
     */
    public interface ClientHandlerCallback {

        /**
         * 自身关闭通知
         * @param handler 处理者
         */
        void onSelfClosed(ClientHandler handler);

        /**
         * 收到消息时通知
         *
         * @param handler 处理者
         * @param msg 消息
         */
        void onNewMessageArrived(ClientHandler handler, String msg);
    }

    /**
     * 对客户端的读事件的处理程序
     *
     * @author yangxin
     * 2020/08/03 16:53
     */
    class ClientReadHandler implements Runnable {

        private final InputStream inputStream;
        private Thread thread;

        ClientReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void setThread(Thread thread) {
            this.thread = thread;
        }

        @Override
        public void run() {
            try {
                // 得到输入流，用于接收数据
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));

                do {
                    // 客户端拿到一条数据
                    String str = socketInput.readLine();
                    if (str == null) {
                        System.out.println("客户端已无法读取数据！");
                        // 退出当前客户端
                        ClientHandler.this.exitBySelf();
                        break;
                    }
                    // 通知到TCPServer
                    clientHandlerCallback.onNewMessageArrived(ClientHandler.this, str);
                } while (!Thread.interrupted());
            } catch (Exception e) {
                if (!Thread.interrupted()) {
                    System.out.println("连接异常断开");
                    ClientHandler.this.exitBySelf();
                }
            } finally {
                // 连接关闭
                CloseUtils.close(inputStream);
            }
        }

        void exit() {
            thread.interrupt();
            CloseUtils.close(inputStream);
        }
    }

    /**
     * 服务端对客户端的写事件处理程序
     *
     * @author yangxin
     * 2020/08/03 16:54
     */
    static class ClientWriteHandler {

        private boolean done = false;
        private final PrintStream printStream;
        private final ExecutorService executorService;

        ClientWriteHandler(OutputStream outputStream) {
            this.printStream = new PrintStream(outputStream);
            this.executorService = Executors.newSingleThreadExecutor();
        }

        void exit() {
            done = true;
            CloseUtils.close(printStream);
            executorService.shutdown();
        }

        void send(String str) {
            if (done) {
                return;
            }
            executorService.execute(new WriteRunnable(str));
        }

        class WriteRunnable implements Runnable {

            private final String msg;

            WriteRunnable(String msg) {
                this.msg = msg;
            }

            @Override
            public void run() {
                if (ClientWriteHandler.this.done) {
                    return;
                }

                try {
                    ClientWriteHandler.this.printStream.println(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
