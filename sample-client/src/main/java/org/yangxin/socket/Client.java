package org.yangxin.socket;

import org.yangxin.socket.bean.ServerInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 客户端入口
 *
 * @author yangxin
 * 2020/08/03 16:41
 */
public class Client {

    public static void main(String[] args) {
        // 客户端用udp广播，获得服务器返回的tcp端口信息
        ServerInfo info = UDPSearcher.searchServer(10000);
        System.out.println("Server:" + info);

        if (info != null) {
            TCPClient tcpClient = null;

            try {
                tcpClient = TCPClient.startWith(info);
                if (tcpClient == null) {
                    return;
                }

                // 循环写，会阻塞
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
