package org.yangxin.socket.udptcp.thirdudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;

/**
 * udp提供者，用于提供服务
 *
 * @author yangxin
 * 2020/07/06 16:23
 */
public class UDPProvider {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String[] args) throws IOException {
        // 生成一份唯一标识
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);

        Thread thread = new Thread(provider);
        thread.start();

        // 主线程阻塞，监听键盘输入，读取任意键盘信息后可以退出
        System.in.read();
        provider.exit(thread);
    }

    /**
     * @author yangxin
     * 2020/07/06 17:21
     */
    private static class Provider implements Runnable {

        private final String sn;
        private DatagramSocket datagramSocket = null;

        private Provider(String sn) {
            this.sn = sn;
        }

        @SuppressWarnings("DuplicatedCode")
        @Override
        public void run() {
            System.out.println("UDPProvider started...");

            try {
                // 监听20000端口
                datagramSocket = new DatagramSocket(20000);
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
                    System.out.println("UDPProvider receive from ip: " + ip + "\tport: " + port + "\tdata: " + data);

                    // 解析端口号
                    int responsePort = MessageCreator.parsePort(data);
                    if (responsePort != -1) {
                        // 构建一份回送数据
                        String responseData = MessageCreator.buildWithSn(sn);
                        byte[] responseDataBytes = responseData.getBytes();

                        // 直接根据发送者构建一份回送信息
                        DatagramPacket responsePacket = new DatagramPacket(responseDataBytes,
                                responseDataBytes.length,
                                receivePack.getAddress(),
                                responsePort);
                        datagramSocket.send(responsePacket);
                    }

                }
            } catch (IOException ignored) {
            } finally {
                close();
            }

            System.out.println("UDPProvider finished...");
        }

        private void close() {
            if (datagramSocket != null) {
                datagramSocket.close();
                datagramSocket = null;
            }
        }

        /**
         * 提供结束
         */
        void exit(Thread thread) {
            thread.interrupt();
            close();
        }
    }
}
