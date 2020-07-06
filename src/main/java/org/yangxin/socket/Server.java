package org.yangxin.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author yangxin
 * 2020/07/04 22:03
 */
public class Server {

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(2000);

        System.out.println("服务器准备就绪……");
        System.out.println("服务器信息：" + serverSocket.getInetAddress() + " P:" + serverSocket.getLocalPort());

        // 等待客户端连接
        while (true) {
            // 得到客户端
            Socket clientSocket = serverSocket.accept();
            // 客户端构建异步线程
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            // 启动线程
            new Thread(clientHandler).start();
        }
    }

    /**
     * @author yangxin
     * 2020/07/04 22:30
     */
    private static class ClientHandler implements Runnable {
//    private static class ClientHandler extends Thread {

        private final Socket socket;
        private boolean flag = true;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("新客户端连接：" + socket.getInetAddress() + " P:" + socket.getPort());

            try {
                // 得到打印流，用于数据输出，服务器回送数据使用
                PrintStream printStream = new PrintStream(socket.getOutputStream());
                // 得到输入流，用于接收数据
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                do {
                    // 从客户端拿到一条数据
                    String s = bufferedReader.readLine();
                    if ("bye".equalsIgnoreCase(s)) {
                        flag = false;
                        // 回送
                        printStream.println("bye");
                    } else {
                        // 打印到屏幕，并回送数据长度
                        System.out.println(s);
                        printStream.println("回送：" + s.length());
                    }
                } while (flag);

                bufferedReader.close();
                printStream.close();
            } catch (IOException e) {
//                e.printStackTrace();
                System.err.println("连接异常断开");
            } finally {
                // 连接关闭
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("客户端已退出：" + socket.getInetAddress() + " P:" + socket.getPort());
        }
    }
}
