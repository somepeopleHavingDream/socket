package org.yangxin.socket;

import org.yangxin.socket.bean.ServerInfo;
import org.yangxin.socket.utils.CloseUtils;

import java.io.*;
import java.net.*;

/**
 * @author yangxin
 * 2020/07/28 16:22
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

        // 连接本地，端口20000；超时时间3000ms
        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()), info.getPort()), 3000);

        System.out.println("已发起服务器连接，并进入后续流程……");
        System.out.println("客户端信息：" + socket.getLocalAddress() + " P: " + socket.getLocalPort());
        System.out.println("服务器信息：" + socket.getInetAddress() + " P: " + socket.getPort());

        try {
            ReadHandler readHandler = new ReadHandler(socket.getInputStream());
            new Thread(readHandler).start();

            return new TCPClient(socket, readHandler);
        } catch (Exception e) {
            System.out.println("连接异常。");
            CloseUtils.close(socket);
        }

        return null;
    }

    /**
     * @author yangxin
     * 2020/07/28 16:24
     */
    static class ReadHandler implements Runnable {

        private boolean done = false;
        private final InputStream inputStream;

        ReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try {
                // 得到输入流，用于接收数据
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));

                do {
                    String str;

                    // 客户端拿到一条数据
                    try {
                        str = socketInput.readLine();
                    } catch (IOException e) {
                        continue;
                    }

                    if (str == null) {
                        System.out.println("连接已关闭，无法读取数据！");
                        break;
                    }

                    // 打印到屏幕
                    System.out.println(str);
                } while (!done);
            } catch (Exception e) {
                if (!done) {
                    System.out.println("连接异常断开：" + e.getMessage());
                }
            } finally {
                // 连接关闭
                CloseUtils.close(inputStream);
            }
        }

        void exit() {
            done = true;
            CloseUtils.close(inputStream);
        }
    }
}