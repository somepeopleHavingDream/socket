package org.yangxin.socket.udptcp.fiveudptcp.tcpchannel.server;

import org.yangxin.socket.udptcp.fiveudptcp.tcpchannel.clink.utils.ByteUtils;
import org.yangxin.socket.udptcp.fiveudptcp.tcpchannel.constants.UDPConstants;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * @author yangxin
 * 2020/07/16 21:10
 */
public class UDPProvider {

    private static Provider PROVIDER_INSTANCE;

    public static void start(Integer port) {
        stop();

        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn, port);
        Thread thread = new Thread(provider);
        provider.setThread(thread);
//        new Thread(provider).start();
        PROVIDER_INSTANCE = provider;
    }

    static void stop() {
        if (PROVIDER_INSTANCE != null) {
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }

    /**
     *负责处理udp请求，并返回服务器监听的tcp端口信息
     *
     * @author yangxin
     * 2020/07/16 21:08
     */
    private static class Provider implements Runnable {

        private final byte[] sn;
        private final int port;
//        private boolean done = false;
        private DatagramSocket ds = null;
        // 存储消息的Buffer
        final byte[] buffer = new byte[128];
        private Thread thread;

        Provider(String sn, int port) {
            super();
            this.sn = sn.getBytes();
            this.port = port;
        }

        public void setThread(Thread thread) {
            this.thread = thread;
        }

        @SuppressWarnings("DuplicatedCode")
        @Override
        public void run() {
            System.out.println("UDPProvider Started.");
            try {
                // 监听30201端口，但其实通过广播发过来的udp消息也可以接收到
                ds = new DatagramSocket(UDPConstants.PORT_SERVER);
                // 接收消息的Packet
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);

                while (!Thread.interrupted()) {
//                while (!done) {
                    // 接收
                    ds.receive(receivePack);

                    // 打印接收到的信息与发送者的信息
                    // 发送者的IP地址
                    String clientIp = receivePack.getAddress().getHostAddress();
                    int clientPort = receivePack.getPort();
                    int clientDataLen = receivePack.getLength();
                    byte[] clientData = receivePack.getData();
                    boolean isValid = clientDataLen >= (UDPConstants.HEADER.length + 2 + 4)
                            && ByteUtils.startsWith(clientData, UDPConstants.HEADER);

                    System.out.println("UDPProvider receive form ip:" + clientIp
                            + "\tport:" + clientPort + "\tdataValid:" + isValid);

                    if (!isValid) {
                        // 无效继续
                        continue;
                    }

                    // 解析命令与回送端口
                    int index = UDPConstants.HEADER.length;
                    short cmd = (short) ((clientData[index++] << 8) | (clientData[index++] & 0xff));
                    int responsePort = (((clientData[index++]) << 24) |
                            ((clientData[index++] & 0xff) << 16) |
                            ((clientData[index++] & 0xff) << 8) |
                            ((clientData[index] & 0xff)));

                    // 判断合法性
                    if (cmd == 1 && responsePort > 0) {
                        // 构建一份回送数据
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        byteBuffer.put(UDPConstants.HEADER);
                        byteBuffer.putShort((short) 2);
                        byteBuffer.putInt(port);
                        byteBuffer.put(sn);
                        int len = byteBuffer.position();
                        // 直接根据发送者构建一份回送信息
                        DatagramPacket responsePacket = new DatagramPacket(buffer,
                                len,
                                receivePack.getAddress(),
                                responsePort);
                        ds.send(responsePacket);
                        System.out.println("UDPProvider response to:" + clientIp + "\tport:" + responsePort + "\tdataLen:" + len);
                    } else {
                        System.out.println("UDPProvider receive cmd nonsupport; cmd:" + cmd + "\tport:" + port);
                    }
                }
            } catch (Exception ignored) {
            } finally {
                close();
            }

            // 完成
            System.out.println("UDPProvider Finished.");
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

        /**
         * 提供结束
         */
        void exit() {
            thread.interrupt();
//            done = true;
            close();
        }
    }
}
