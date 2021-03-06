package org.yangxin.socket.bio;

import org.yangxin.socket.bio.bean.ServerInfo;
import org.yangxin.socket.bio.utils.CloseUtils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * @author yangxin
 * 2020/08/03 16:46
 */
public class TCPClient {

    private final Socket socket;
    private final ReadHandler readHandler;
    private final PrintStream printStream;

    public TCPClient(Socket socket, ReadHandler readHandler) throws IOException {
        this.socket = socket;
        this.readHandler = readHandler;
        this.printStream = new PrintStream(socket.getOutputStream());
    }

    public void exit() {
        readHandler.exit();
        CloseUtils.close(printStream);
        CloseUtils.close(socket);
    }

    public void send(String msg) {
        printStream.println(msg);
    }

    public static TCPClient startWith(ServerInfo info) throws IOException {
        Socket socket = new Socket();
        // 超时时间
        socket.setSoTimeout(3000);

        // 连接本地，端口2000；超时时间3000ms
        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()), info.getPort()), 3000);

        System.out.println("已发起服务器连接，并进入后续流程～");
        System.out.println("客户端信息：" + socket.getLocalAddress() + " P:" + socket.getLocalPort());
        System.out.println("服务器信息：" + socket.getInetAddress() + " P:" + socket.getPort());

        try {
            // 客户端开启对服务端的读事件处理线程
            ReadHandler readHandler = new ReadHandler(socket.getInputStream());
            Thread thread = new Thread(readHandler);
            readHandler.setThread(thread);
//            new Thread(readHandler).start();
            return new TCPClient(socket, readHandler);
        } catch (Exception e) {
            System.out.println("连接异常");
            CloseUtils.close(socket);
        }

        return null;
    }

    /**
     * 客户端对服务端的读事件处理程序
     *
     * @author yangxin
     * 2020/08/03 16:47
     */
    static class ReadHandler implements Runnable {

//        private boolean done = false;
        private final InputStream inputStream;
        private Thread thread;

        ReadHandler(InputStream inputStream) {
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
                    String str;
                    try {
                        // 客户端拿到一条数据
                        str = socketInput.readLine();
                    } catch (SocketTimeoutException e) {
                        continue;
                    }
                    if (str == null) {
                        System.out.println("连接已关闭，无法读取数据！");
                        break;
                    }
                    // 打印到屏幕
                    System.out.println(str);
                } while (!Thread.interrupted());
//                } while (!done);
            } catch (Exception e) {
                if (!Thread.interrupted()) {
//                if (!done) {
                    System.out.println("连接异常断开：" + e.getMessage());
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
}
