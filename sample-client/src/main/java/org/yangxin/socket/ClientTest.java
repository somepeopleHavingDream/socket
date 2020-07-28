package org.yangxin.socket;

import org.yangxin.socket.bean.ServerInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yangxin
 * 2020/07/28 16:39
 */
public class ClientTest {

    private static boolean done;

    public static void main(String[] args) throws IOException {
        ServerInfo info = UDPSearcher.searchServer(10000);
        System.out.println("Server: " + info);
        if (info == null) {
            return;
        }

        // 当前连接数
        int size = 0;
        final List<TCPClient> tcpClientList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            try {
                TCPClient tcpClient = TCPClient.startWith(info);
                if (tcpClient == null) {
                    System.out.println("连接异常。");
                    continue;
                }

                tcpClientList.add(tcpClient);
                System.out.println("连接成功：" + (++size));
            } catch (IOException e) {
                System.out.println("连接异常。");
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.in.read();

        Thread thread = new Thread(() -> {
            // 这是错误的停止线程的方式，此处用done来只是图了个方便
            while (!done) {
                for (TCPClient tcpClient : tcpClientList) {
                    tcpClient.send("hello!");
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        System.in.read();

        // 等待线程完成
        done = true;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 客户端结束操作
        for (TCPClient tcpClient : tcpClientList) {
            tcpClient.exit();
        }
    }
}
