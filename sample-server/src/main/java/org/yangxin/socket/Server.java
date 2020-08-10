package org.yangxin.socket;

import org.yangxin.socket.constants.TCPConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 服务端入口
 *
 * @author yangxin
 * 2020/08/03 16:55
 */
public class Server {

    public static void main(String[] args) throws IOException {
        // 开启tcp服务端
        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed) {
            System.out.println("Start TCP server failed!");
            return;
        }

        // 服务端使用udp对外提供tcp服务端口信息
        UDPProvider.start(TCPConstants.PORT_SERVER);

        // 循环写，会阻塞
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String str;
        do {
            str = bufferedReader.readLine();
            // 服务端的写事件会对所有已连接的tcp套接字发送消息
            tcpServer.broadcast(str);
        } while (!"00bye00".equalsIgnoreCase(str));

        UDPProvider.stop();
        tcpServer.stop();
    }
}
