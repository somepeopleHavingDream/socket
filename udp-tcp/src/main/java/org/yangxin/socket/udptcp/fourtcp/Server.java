package org.yangxin.socket.udptcp.fourtcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * @author yangxin
 * 2020/07/06 21:30
 */
public class Server {

    private static final Integer PORT = 20000;

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) throws IOException {
        ServerSocket server = createServerSocket();

        initServerSocket(server);

        // 绑定到本地端口上
        // backlog=50，会影响到操作系统中的accept queue队列接收请求的个数，接受的请求数就是此值与/proc/sys/net/core/somaxconn的值取最小
        server.bind(new InetSocketAddress(Inet4Address.getLocalHost(), PORT), 50);

        System.out.println("服务器准备就绪……");
        System.out.println("服务器信息：" + server.getInetAddress() + " P: " + server.getLocalPort());

        // 等待客户端连接
        for (;;) {
            // 得到客户端
            Socket client = server.accept();
            // 客户端构建异步线程
            ClientHandler clientHandler = new ClientHandler(client);
            // 启动线程
            new Thread(clientHandler).start();
        }
    }

    private static void initServerSocket(ServerSocket serverSocket) throws SocketException {
        // 是否复用未完全关闭的地址端口
        serverSocket.setReuseAddress(true);

        // 等效Socket#setReceiveBufferSize
        serverSocket.setReceiveBufferSize(64 * 1024 * 1024);

        // 设置serverSocket#accept超时时间
//        serverSocket.setSoTimeout(2000);

        // 设置性能参数：短链接、延迟、带宽的相对重要性
        serverSocket.setPerformancePreferences(1, 1, 1);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private static ServerSocket createServerSocket() throws IOException {
        // 创建基础的ServerSocket
        ServerSocket serverSocket = new ServerSocket();

//        // 绑定到本地端口20000上，并且设置当前可允许等待连接的队列为50个
//        serverSocket = new ServerSocket(PORT);
//
//        // 等效于上面的方案，队列设置为50个
//        serverSocket = new ServerSocket(PORT, 50);
//
//        // 与上面等同
//        serverSocket = new ServerSocket(PORT, 50, Inet4Address.getLocalHost());

        return serverSocket;
    }

    /**
     * @author yangxin
     * 2020/07/05 21:39
     */
    private static class ClientHandler implements Runnable {

        private final Socket socket;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("新客户端连接：" + socket.getInetAddress() + " P: " + socket.getPort());

            try {
                // 得到套接字流
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();

                byte[] buffer = new byte[256];
                int readCount = inputStream.read(buffer);
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, readCount);

                // byte
                byte be = byteBuffer.get();

                // char
                char c = byteBuffer.getChar();

                // int
                int i = byteBuffer.getInt();

                // boolean
                boolean b = byteBuffer.get() == 1;

                // long
                long l = byteBuffer.getLong();

                // float
                float f = byteBuffer.getFloat();

                // double
                double d = byteBuffer.getDouble();

                // String
                int position = byteBuffer.position();
                String s = new String(buffer, position, readCount - position);
//                String s = new String(buffer, position, readCount - position - 1);

                System.out.println("收到数量："
                        + readCount
                        + " 数据："
                        + be + "\n"
                        + c + "\n"
                        + i + "\n"
                        + b + "\n"
                        + l + "\n"
                        + f + "\n"
                        + d + "\n"
                        + s + "\n");

                outputStream.write(buffer, 0, readCount);
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                System.err.println("连接异常断开……");
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
