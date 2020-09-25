package org.yangxin.socket.udptcp.thirdudp;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * UDP搜索者，用于搜索服务支持方
 *
 * @author yangxin
 * 2020/07/06 16:34
 */
public class UDPSearcher {

    private static final Integer LISTEN_PORT = 30000;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("UDPSearcher started...");

        Listener listener = listen();
        sendBroadcast();

        // 读取任意键盘信息后可以退出
        System.in.read();
        List<Device> deviceList = listener.getDevicesAndClose();
        deviceList.forEach(System.out::println);

        // 完成
        System.out.println("UDPSearcher finished...");
    }

    private static Listener listen() throws InterruptedException {
        System.out.println("UDPSearcher start listen.");

        CountDownLatch countDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, countDownLatch);
        Thread thread = new Thread(listener);
        thread.start();
        listener.setThread(thread);

        countDownLatch.await();

        return listener;
    }

    private static class Device {

        final int port;
        final String ip;
        final String sn;

        private Device(int port, String ip, String sn) {
            this.port = port;
            this.ip = ip;
            this.sn = sn;
        }

        @Override
        public String toString() {
            return "Device{" +
                    "port=" + port +
                    ", ip='" + ip + '\'' +
                    ", sn='" + sn + '\'' +
                    '}';
        }
    }

    /**
     * @author yangxin
     * 2020/07/31 17:42
     */
    private static class Listener implements Runnable {

        private final Integer listenPort;
        private final CountDownLatch countDownLatch;
        private final List<Device> deviceList = new ArrayList<>();
        private DatagramSocket datagramSocket = null;
        private Thread thread;

        private Listener(Integer listenPort, CountDownLatch countDownLatch) {
            this.listenPort = listenPort;
            this.countDownLatch = countDownLatch;
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
            countDownLatch.countDown();

            try {
                // 监听回送端口
                datagramSocket = new DatagramSocket(listenPort);
                while (!Thread.interrupted()) {
                    // 构建接收实体
                    final byte[] buffer = new byte[512];
                    DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);

                    // 接收
                    datagramSocket.receive(receivePack);

                    // 打印接收到的信息与发送者的信息
                    // 发送者的ip地址、端口、数据
                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int length = receivePack.getLength();
                    String data = new String(receivePack.getData(), 0, length);
                    System.out.println("UDPSearcher receive from ip: " + ip + "\tport: " + port + "\tdata: " + data);

                    String sn = MessageCreator.parseSn(data);
                    if (sn != null) {
                        Device device = new Device(port, ip, sn);
                        deviceList.add(device);
                    }
                }
            } catch (Exception ignored) {
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

        List<Device> getDevicesAndClose() {
            thread.interrupt();
            close();
            return deviceList;
        }
    }

    private static void sendBroadcast() throws IOException {
        System.out.println("UDPSearcher sendBroadcast started...");

        // 作为搜索方，让系统自动分配
        DatagramSocket datagramSocket = new DatagramSocket();

        // 构建一份请求数据
        String requestData = MessageCreator.buildWithPort(LISTEN_PORT);
        byte[] requestDataBytes = requestData.getBytes();
        // 直接构建packet
        DatagramPacket requestPacket = new DatagramPacket(requestDataBytes, requestDataBytes.length);
        // 本机20000端口
        requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        requestPacket.setPort(20000);

        // 发送
        datagramSocket.send(requestPacket);
        datagramSocket.close();

        // 完成
        System.out.println("UDPSearcher sendBroadcast finished...");
    }
}
