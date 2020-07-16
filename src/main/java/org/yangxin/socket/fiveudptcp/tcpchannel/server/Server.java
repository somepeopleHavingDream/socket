package org.yangxin.socket.fiveudptcp.tcpchannel.server;


import org.yangxin.socket.fiveudptcp.tcpchannel.constants.TCPConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author yangxin
 * 2020/07/16 21:02
 */
public class Server {

    public static void main(String[] args) throws IOException {
        // tcp监听并处理响应请求
        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed) {
            System.out.println("Start TCP server failed!");
            return;
        }

        // udp服务返回当前服务器的tcp监听端口
        UDPProvider.start(TCPConstants.PORT_SERVER);

        // 键盘事件
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String str;
        do {
            str = bufferedReader.readLine();
            tcpServer.broadcast(str);
        } while (!"00bye00".equalsIgnoreCase(str));

        // 停止udp、tcp服务
        UDPProvider.stop();
        tcpServer.stop();
    }
}
