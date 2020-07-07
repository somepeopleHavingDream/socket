package org.yangxin.socket.fiveudptcp.server;

import org.yangxin.socket.fiveudptcp.constants.TCPConstants;

import java.io.IOException;

/**
 * @author yangxin
 * 2020/07/07 20:48
 */
public class Server {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String[] args) {
        ServerProvider.start(TCPConstants.PORT_SERVER);

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ServerProvider.stop();
    }
}
