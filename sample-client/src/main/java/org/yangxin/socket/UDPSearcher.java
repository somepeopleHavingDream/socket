package org.yangxin.socket;

import org.yangxin.socket.bean.ServerInfo;
import org.yangxin.socket.constants.UDPConstants;
import org.yangxin.socket.utils.ByteUtils;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author yangxin
 * 2020/07/28 15:51
 */
public class UDPSearcher {

    private static final Integer LISTEN_PORT = UDPConstants.PORT_CLIENT_RESPONSE;

    public static ServerInfo searchServer(int timeout) {
        System.out.println("UDPSearcher Started.");

        // 成功收到回送的栅栏
        CountDownLatch receiveLatch = new CountDownLatch(1);
        Listener listener = null;
        try {
            listener = listen(receiveLatch);
            sendBroadcast();

            receiveLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        // 完成
        System.out.println("UDPSearcher Finished.");
        if (listener == null) {
            return null;
        }

        List<ServerInfo> deviceList = listener.getServerAndClose();
        if (deviceList.size() > 0) {
            return deviceList.get(0);
        }
        return null;
    }

    private static void sendBroadcast() throws IOException {
        System.out.println("UDPSearcher sendBroadcast started.");

        // 作为搜索方，让系统自动分配端口
        DatagramSocket datagramSocket = new DatagramSocket();

        // 构建一份请求数据
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        // 头部
        byteBuffer.put(UDPConstants.HEADER);
        // cmd命名
        byteBuffer.putShort((short) 1);
        // 回送端口信息
        byteBuffer.putInt(LISTEN_PORT);

        // 直接构建packet
        // todo
        DatagramPacket requestPacket = new DatagramPacket(byteBuffer.array(), byteBuffer.position());
        // 广播地址
        requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        // 设置服务器端口
        requestPacket.setPort(UDPConstants.PORT_CLIENT_RESPONSE);

        // 发送
        datagramSocket.send(requestPacket);
        datagramSocket.close();

        // 完成
        System.out.println("UDPSearcher sendBroadcast finished.");
    }

    private static Listener listen(CountDownLatch receiveLatch) throws InterruptedException {
        System.out.println("UDPSearcher start listen.");

        CountDownLatch startDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, startDownLatch, receiveLatch);
        new Thread(listener).start();
        startDownLatch.await();

        return listener;
    }

    /**
     * @author yangxin
     * 2020/07/28 15:59
     */
    private static class Listener implements Runnable {

        private final Integer listenPort;
        private final CountDownLatch startDownLatch;
        private final CountDownLatch receiveDownLatch;
        private final List<ServerInfo> serverInfoList = new ArrayList<>();
        private final byte[] buffer = new byte[128];
        private final Integer minLength = UDPConstants.HEADER.length + 2 + 4;
        private boolean done = false;
        private DatagramSocket datagramSocket;

        private Listener(Integer listenPort, CountDownLatch startDownLatch, CountDownLatch receiveDownLatch) {
            this.listenPort = listenPort;
            this.startDownLatch = startDownLatch;
            this.receiveDownLatch = receiveDownLatch;
        }

        @Override
        public void run() {
            // 通知已启动
            startDownLatch.countDown();

            try {
                // 监听回送端口
                datagramSocket = new DatagramSocket(listenPort);

                // 构建接收实体
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);

                while (!done) {
                    // 接收
                    datagramSocket.receive(receivePack);

                    // 打印接收到的信息与发送者的信息
                    // 发送者的IP地址
                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int dataLength = receivePack.getLength();
                    byte[] data = receivePack.getData();
                    boolean isValid = dataLength >= minLength && ByteUtils.startsWith(data, UDPConstants.HEADER);

                    System.out.println("UDPSearcher receive from ip: " + ip
                        + "\tport: " + port
                        + "\tdataValid: " + isValid);

                    if (!isValid) {
                        // 无效，继续
                        continue;
                    }

                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, UDPConstants.HEADER.length, dataLength);
                    final short cmd = byteBuffer.getShort();
                    final int serverPort = byteBuffer.getInt();
                    if (cmd != 2 || serverPort <= 0) {
                        System.out.println("UDPSearcher receive cmd: " + cmd
                            + "\tserverPort: " + serverPort);
                        continue;
                    }

                    String sn = new String(buffer, minLength, dataLength - minLength);
                    ServerInfo info = new ServerInfo(serverPort, ip, sn);
                    serverInfoList.add(info);

                    // 成功接收到一份
                    receiveDownLatch.countDown();
                }
            } catch (IOException ignored) {
            } finally {
                close();
            }

            System.out.println("UDPSearcher listener finished.");
        }

        private void close() {
            if (datagramSocket != null) {
                datagramSocket.close();
                datagramSocket = null;
            }
        }

        List<ServerInfo> getServerAndClose() {
            done = true;
            close();
            return serverInfoList;
        }
    }
}
