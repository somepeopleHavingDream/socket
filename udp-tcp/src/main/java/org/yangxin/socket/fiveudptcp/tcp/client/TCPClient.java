package org.yangxin.socket.fiveudptcp.tcp.client;

import org.yangxin.socket.fiveudptcp.tcp.client.bean.ServerInfo;

import java.io.*;
import java.net.*;

/**
 * @author yangxin
 * 2020/07/14 21:34
 */
public class TCPClient {

    public static void linkWith(ServerInfo serverInfo) throws IOException {
        Socket socket = new Socket();
        // 超时时间
        socket.setSoTimeout(3000);

        // 连接本地，端口2000；超时时间3000ms
        socket.connect(new InetSocketAddress(InetAddress.getByName(serverInfo.getAddress()),
                serverInfo.getPort()),
                3000);

        System.out.println("已发起服务器连接，并进入后续流程……");
        System.out.println("客户端信息：" + socket.getLocalAddress() + " P: " + socket.getLocalPort());
        System.out.println("服务器信息：" + socket.getInetAddress() + " P: " + socket.getPort());

        // 发送接收数据
        todo(socket);
    }

    @SuppressWarnings("DuplicatedCode")
    private static void todo(Socket client) throws IOException {
        // 构建键盘输入流
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        // 得到socket输出流，并转换为打印流
        OutputStream outputStream = client.getOutputStream();
        PrintStream socketPrintStream = new PrintStream(outputStream);

        // 得到socket输入流，并转换为BufferedReader
        InputStream inputStream = client.getInputStream();
        BufferedReader socketBufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        boolean flag = true;
        do {
            // 键盘读取一行
            String s = input.readLine();
            // 发送到服务器
            socketPrintStream.println(s);

            // 从服务器读取一行
            String echo = socketBufferedReader.readLine();
            if ("bye".equalsIgnoreCase(echo)) {
                flag = false;
            } else {
                System.out.println(echo);
            }
        } while (flag);

        // 资源释放
        socketPrintStream.close();
        socketBufferedReader.close();
    }
}
