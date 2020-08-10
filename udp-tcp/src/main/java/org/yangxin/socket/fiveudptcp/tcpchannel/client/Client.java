package org.yangxin.socket.fiveudptcp.tcpchannel.client;


import org.yangxin.socket.fiveudptcp.tcpchannel.client.bean.ServerInfo;

import java.io.IOException;

/**
 * @author yangxin
 * 2020/07/15 16:33
 */
public class Client {

    public static void main(String[] args) {
        // 通过udp广播，获知服务器tcp端口相关信息
        ServerInfo info = UDPSearcher.searchServer(10000);
        System.out.println("Server:" + info);

        if (info != null) {
            try {
                // 对服务端发起tcp点对点连接
                TCPClient.linkWith(info);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
