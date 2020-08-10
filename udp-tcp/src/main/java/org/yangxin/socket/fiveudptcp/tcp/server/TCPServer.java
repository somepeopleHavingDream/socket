package org.yangxin.socket.fiveudptcp.tcp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author yangxin
 * 2020/07/14 21:43
 */
public class TCPServer {

    private final int port;
    private ClientListener clientListener;

    public TCPServer(int port) {
        this.port = port;
    }

    /**
     * 开启对客户端的监听线程
     */
    public boolean start() {
        try {
            clientListener = new ClientListener(port);
            Thread thread = new Thread(clientListener);
            thread.start();
            clientListener.setThread(thread);
//            new Thread(clientListener).start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void stop() {
        if (clientListener != null) {
            clientListener.exit();
        }
    }

    /**
     * @author yangxin
     * 2020/07/14 21:44
     */
    private static class ClientListener implements Runnable {

        private final ServerSocket serverSocket;
        private Thread thread;

        private ClientListener(int port) throws IOException {
            serverSocket = new ServerSocket(port);
            System.out.println("服务器信息：" + serverSocket.getInetAddress() + " P: " + serverSocket.getLocalPort());
        }

        public Thread getThread() {
            return thread;
        }

        public void setThread(Thread thread) {
            this.thread = thread;
        }

        @Override
        public void run() {
            System.out.println("服务器准备就绪……");

            // 等待客户端连接
            do {
                // 得到客户端
                Socket client;

                try {
                    client = serverSocket.accept();
                } catch (IOException e) {
                    continue;
                }

                // 客户端构建异步线程
                ClientHandler clientHandler = new ClientHandler(client);
                // 启动线程
                new Thread(clientHandler).start();
            } while (!Thread.interrupted());
//            } while (!done);

            System.out.println("服务器已关闭！");
        }

        public void exit() {
            thread.interrupt();
//            done = true;
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 客户端消息处理
     *
     * @author yangxin
     * 2020/07/14 21:47
     */
    private static class ClientHandler implements Runnable {

        private final Socket socket;
        private boolean flag = true;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @SuppressWarnings("DuplicatedCode")
        @Override
        public void run() {
            System.out.println("新客户端连接：" + socket.getInetAddress() + " P: " + socket.getPort());

            try {
                // 得到打印流，用于数据输出；服务器回送数据使用
                PrintStream socketOutput = new PrintStream(socket.getOutputStream());
                // 得到输入流，用于接收数据
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                do {
                    // 客户端拿到一条数据
                    String s = socketInput.readLine();
                    if ("bye".equalsIgnoreCase(s)) {
                        flag = false;
                        // 回送
                        socketOutput.println("bye");
                    } else {
                        // 打印到屏幕，并回送数据长度
                        System.out.println(s);
                        socketOutput.println("回送：" + s.length());
                    }
                } while (flag);

                socketInput.close();
                socketOutput.close();
            } catch (IOException e) {
                System.err.println("连接异常断开");
            } finally {
                // 连接关闭
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("客户端已退出：" + socket.getInetAddress() + " P: " + socket.getPort());
        }
    }
}
