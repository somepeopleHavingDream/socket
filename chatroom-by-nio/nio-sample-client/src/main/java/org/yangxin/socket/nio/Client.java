package org.yangxin.socket.nio;

import org.yangxin.socket.nio.bean.ServerInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author yangxin
 * 2020/08/12 09:25
 */
public class Client {

    public static void main(String[] args) {
        // udp广播，获取服务端响应回来的tcp端口信息
        ServerInfo info = UDPSearcher.searchServer(10000);
        System.out.println("Server:" + info);

        if (info != null) {
            TCPClient tcpClient = null;

            try {
                // 客户端发起tcp连接
                tcpClient = TCPClient.startWith(info);
                if (tcpClient == null) {
                    return;
                }

                // 键盘事件监听
                write(tcpClient);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (tcpClient != null) {
                    tcpClient.exit();
                }
            }
        }
    }

    /**
     * 键盘事件监听
     */
    private static void write(TCPClient tcpClient) throws IOException {
        // 构建键盘输入流
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        do {
            // 键盘读取一行
            String str = input.readLine();
            // 发送到服务器
            tcpClient.send(str);

            if ("00bye00".equalsIgnoreCase(str)) {
                break;
            }
        } while (true);
    }
}
