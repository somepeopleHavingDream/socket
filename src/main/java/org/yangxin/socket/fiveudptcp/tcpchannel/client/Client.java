package org.yangxin.socket.fiveudptcp.tcpchannel.client;


import org.yangxin.socket.fiveudptcp.tcpchannel.client.bean.ServerInfo;

import java.io.IOException;

/**
 * @author yangxin
 * 2020/07/15 16:33
 */
public class Client {

    public static void main(String[] args) {
//        UDPSearcher.searchServer(10000);
        ServerInfo info = UDPSearcher.searchServer(10000);
        System.out.println("Server:" + info);

        if (info != null) {
            try {
                TCPClient.linkWith(info);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
