package org.yangxin.socket.nio.thread;

import org.yangxin.socket.nio.thread.constants.TCPConstants;
import org.yangxin.socket.nio.thread.core.IOContext;
import org.yangxin.socket.nio.thread.impl.IOSelectorProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 服务端
 *
 * @author yangxin
 * 2020/08/12 16:48
 */
public class Server {

    public static void main(String[] args) throws IOException {
        IOContext.setup()
                .ioProvider(new IOSelectorProvider())
                .start();

        // 服务端开启tcp服务
        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed) {
            System.out.println("Start TCP server failed!");
            return;
        }

        // udp提供tcp服务端口信息
        UDPProvider.start(TCPConstants.PORT_SERVER);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String str;
        do {
            str = bufferedReader.readLine();
            tcpServer.broadcast(str);
        } while (!"00bye00".equalsIgnoreCase(str));

        UDPProvider.stop();
        tcpServer.stop();

        IOContext.close();
    }
}
