package org.yangxin.socket.udptcp.fiveudptcp.tcpchannel.server.handle;

import org.yangxin.socket.udptcp.fiveudptcp.tcpchannel.clink.utils.CloseUtils;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 服务端对客户端发送过来的tcp消息进行处理
 *
 * @author yangxin
 * 2020/07/16 21:30
 */
public class ClientHandler {

    private final Socket socket;
    private final ClientReadHandler readHandler;
    private final ClientWriteHandler writeHandler;
    private final CloseNotify closeNotify;

    public ClientHandler(Socket socket, CloseNotify closeNotify) throws IOException {
        this.socket = socket;
        this.readHandler = new ClientReadHandler(socket.getInputStream());
        this.writeHandler = new ClientWriteHandler(socket.getOutputStream());
        this.closeNotify = closeNotify;
        System.out.println("新客户端连接：" + socket.getInetAddress() +
                " P:" + socket.getPort());
    }

    /**
     * 读写线程处理器全部退出
     */
    public void exit() {
        readHandler.exit();
        writeHandler.exit();
        CloseUtils.close(socket);
        System.out.println("客户端已退出：" + socket.getInetAddress() +
                " P:" + socket.getPort());
    }

    /**
     * exitBySelf()方法是对exit()方法的进一步包装，增加了closeNotify.onSelfClosed(this)代码
     * ，移除了服务端对客户端的所有连接
     */
    private void exitBySelf() {
        exit();
        closeNotify.onSelfClosed(this);
    }

    public void readToPrint() {
        Thread thread = new Thread(readHandler);
        readHandler.setThread(thread);
        thread.start();
//        new Thread(readHandler).start();
    }

    public void send(String str) {
        writeHandler.send(str);
    }

    /**
     * @author yangxin
     * 2020/07/16 21:33
     */
    public interface CloseNotify {
        void onSelfClosed(ClientHandler handler);
    }

    /**
     * 对客户端发送过来消息进行读事件处理
     *
     * @author yangxin
     * 2020/06/16 21:32
     */
    class ClientReadHandler implements Runnable {

//        private boolean done = false;
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
                    // 打印到屏幕
                    System.out.println(str);
                } while (!Thread.interrupted());
//                } while (!done);
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
//            done = true;
            CloseUtils.close(inputStream);
        }
    }

    /**
     * 服务端写入数据给客户端
     *
     * @author yangxin
     * 2020/07/16 21:35
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
//            executorService.shutdownNow();
        }

        void send(String str) {
            executorService.execute(new WriteRunnable(str));
        }

        /**
         * @author yangxin
         * 2020/07/16 21:40
         */
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
