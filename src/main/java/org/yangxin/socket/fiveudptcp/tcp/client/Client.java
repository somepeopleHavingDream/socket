package org.yangxin.socket.fiveudptcp.tcp.client;

import org.yangxin.socket.fiveudptcp.tcp.client.bean.ServerInfo;

import java.io.IOException;

/**
 * @author yangxin
 * 2020/07/14 21:08
 */
public class Client {

    public static void main(String[] args) {
        // udp发起广播，以获得服务端tcp端口
        ServerInfo serverInfo = UDPSearcher.searchServer(10000);
        System.out.println("Server: " + serverInfo);

        // tcp点对点连接
        if (serverInfo != null) {
            try {
                TCPClient.linkWith(serverInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
