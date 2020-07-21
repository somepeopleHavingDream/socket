package org.yangxin.socket;

import org.yangxin.socket.bean.ServerInfo;

import java.io.IOException;

/**
 * @author yangxin
 * 2020/07/21 20:52
 */
public class Client {

    public static void main(String[] args) {
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
