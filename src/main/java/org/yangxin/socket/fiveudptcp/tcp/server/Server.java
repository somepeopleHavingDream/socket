package org.yangxin.socket.fiveudptcp.tcp.server;

import org.yangxin.socket.fiveudptcp.tcp.constants.TCPConstants;

import java.io.IOException;

/**
 * @author yangxin
 * 2020/07/14 21:42
 */
public class Server {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String[] args) {
        // 监听并处理tcp端口的socket请求
        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed) {
            System.out.println("Start TCP server failed!");
            return;
        }

        // 监听并处理udp端口的socket请求，将tcp监听端口返回出去
        UDPProvider.start(TCPConstants.PORT_SERVER);

        // 键盘事件退出
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UDPProvider.stop();
        tcpServer.stop();
    }
}
