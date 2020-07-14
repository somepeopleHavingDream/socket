package org.yangxin.socket.fiveudptcp.tcp.server;

import org.yangxin.socket.fiveudptcp.tcp.constants.TCPConstants;
import org.yangxin.socket.thirdudp.UDPProvider;

/**
 * @author yangxin
 * 2020/07/14 21:42
 */
public class Server {

    public static void main(String[] args) {
        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed) {
            System.out.println("Start TCP server failed!");
            return;
        }

        // todo
//        UDPProvider.start(TCPConstants.PORT_SERVER);
    }
}
