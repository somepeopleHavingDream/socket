package org.yangxin.socket.fiveudptcp.tcp.client;

import org.yangxin.socket.fiveudptcp.tcp.client.bean.ServerInfo;
import org.yangxin.socket.fiveudptcp.tcp.clink.utils.ByteUtils;
import org.yangxin.socket.fiveudptcp.tcp.constants.UDPConstants;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author yangxin
 * 2020/07/14 21:10
 */
public class UDPSearcher {

    private static final Integer LISTEN_PORT = UDPConstants.PORT_CLIENT_RESPONSE;

    @SuppressWarnings("DuplicatedCode")
    public static ServerInfo searchServer(int timeout) {
        System.out.println("UDPSearcher started...");

        // 成功收到回送的栅栏
        CountDownLatch receiveLatch = new CountDownLatch(1);
        Listener listener = null;
        try {
            // 监听服务端的udp回送消息
            listener = listen(receiveLatch);
            // 发送广播消息
            sendBroadcast();
            receiveLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        // 完成
        System.out.println("UDPSearcher finished.");
        if (listener == null) {
            return null;
        }

        // 返回服务端信息
        List<ServerInfo> deviceList = listener.getServerAndClose();
        if (deviceList.size() > 0) {
            return deviceList.get(0);
        }
        return null;
    }

    @SuppressWarnings("DuplicatedCode")
    private static void sendBroadcast() throws IOException {
        System.out.println("UDPSearcher sendBroadcast started...");

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
        DatagramPacket requestPacket = new DatagramPacket(byteBuffer.array(), byteBuffer.position() + 1);
        // 广播地址
        requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        // 设置服务器端口
        requestPacket.setPort(UDPConstants.PORT_SERVER);

        // 发送
        datagramSocket.send(requestPacket);
        datagramSocket.close();

        // 完成
        System.out.println("UDPSearcher sendBroadcast finished.");
    }

    private static Listener listen(CountDownLatch receiveLatch) throws InterruptedException {
        System.out.println("UDPSearcher start listen...");

        CountDownLatch startLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, startLatch, receiveLatch);
        Thread thread = new Thread(listener);
        thread.start();
        listener.setThread(thread);
//        new Thread(listener).start();
        startLatch.await();
        return listener;
    }

    /**
     * @author yangxin
     * 2020/07/14 21:13
     */
    private static class Listener implements Runnable {

        private final int listenPort;
        private final CountDownLatch startLatch;
        private final CountDownLatch receiveLatch;
        private final List<ServerInfo> serverInfoList = new ArrayList<>();
        private final byte[] buffer = new byte[128];
        private final int minLength = UDPConstants.HEADER.length + 2 + 4;
//        private boolean done = false;
        private DatagramSocket datagramSocket = null;
        private Thread thread;

        private Listener(int listenPort, CountDownLatch startLatch, CountDownLatch receiveLatch) {
            this.listenPort = listenPort;
            this.startLatch = startLatch;
            this.receiveLatch = receiveLatch;
        }

        public Thread getThread() {
            return thread;
        }

        public void setThread(Thread thread) {
            this.thread = thread;
        }

        @SuppressWarnings("DuplicatedCode")
        @Override
        public void run() {
            // 通知已启动
            startLatch.countDown();

            try {
                // 监听回送端口
                datagramSocket = new DatagramSocket(listenPort);
                // 构建接收实体
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);

                while (!Thread.interrupted()) {
//                while (!done) {
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
                        // 无效继续
                        continue;
                    }

                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, UDPConstants.HEADER.length, dataLength);
                    final short cmd = byteBuffer.getShort();
                    final int serverPort = byteBuffer.getInt();
                    if (cmd != 2 || serverPort <= 0) {
                        System.err.println("UDPSearcher receive cmd: " + cmd + "\tserverPort: " + serverPort);
                        continue;
                    }

                    String sn = new String(buffer, minLength, dataLength - minLength);
                    ServerInfo serverInfo = new ServerInfo(serverPort, ip, sn);
                    serverInfoList.add(serverInfo);

                    // 成功接收到一份
                    receiveLatch.countDown();
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

        public List<ServerInfo> getServerAndClose() {
            thread.interrupt();
            //            done = true;
            close();
            return serverInfoList;
        }
    }
}
