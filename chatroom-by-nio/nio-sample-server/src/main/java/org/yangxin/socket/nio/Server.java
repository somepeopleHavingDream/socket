package org.yangxin.socket.nio;

import org.yangxin.socket.nio.constants.TCPConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author yangxin
 * 2020/08/12 09:11
 */
public class Server {

    public static void main(String[] args) throws IOException {
        // 开启tcp服务端处理
        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed) {
            System.out.println("Start TCP server failed!");
            return;
        }

        // 开启udp服务端处理，将tcp服务端口信息响应给客户端
        UDPProvider.start(TCPConstants.PORT_SERVER);

        // Server主线程用来响应Server自身的键盘写事件
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String str;
        do {
            str = bufferedReader.readLine();
            tcpServer.broadcast(str);
        } while (!"00bye00".equalsIgnoreCase(str));

        // 关闭udp和tcp服务
        UDPProvider.stop();
        tcpServer.stop();
    }
}
