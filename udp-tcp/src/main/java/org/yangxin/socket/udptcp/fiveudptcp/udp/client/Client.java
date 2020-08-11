package org.yangxin.socket.udptcp.fiveudptcp.udp.client;


import org.yangxin.socket.udptcp.fiveudptcp.udp.client.bean.ServerInfo;

/**
 * @author yangxin
 * 2020/07/07 21:09
 */
public class Client {

    public static void main(String[] args) {
        ServerInfo info = ClientSearch.searchServer(10000);
        System.out.println("Server: " + info);
    }
}
