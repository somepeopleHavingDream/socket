package org.yangxin.socket.fiveudptcp.tcp.client;

import org.yangxin.socket.fiveudptcp.tcp.client.bean.ServerInfo;

import java.io.IOException;

/**
 * @author yangxin
 * 2020/07/14 21:08
 */
public class Client {

    public static void main(String[] args) {
        ServerInfo serverInfo = UDPSearcher.searchServer(10000);
        System.out.println("Server: " + serverInfo);

        if (serverInfo != null) {
            try {
                TCPClient.linkWith(serverInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
