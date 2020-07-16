package org.yangxin.socket.fiveudptcp.tcp.server;

import org.yangxin.socket.fiveudptcp.tcp.clink.utils.ByteUtils;
import org.yangxin.socket.fiveudptcp.tcp.constants.UDPConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * @author yangxin
 * 2020/07/15 15:39
 */
public class UDPProvider {

    private static Provider PROVIDER_INSTANCE;

    static void start(int port) {
        stop();

        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn, port);
        new Thread(provider).start();
        PROVIDER_INSTANCE = provider;
    }

    static void stop() {
        if (PROVIDER_INSTANCE != null) {
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }

    /**
     * @author yangxin
     * 2020/07/15 15:40
     */
    private static class Provider implements Runnable {

        private final byte[] sn;
        private final Integer port;
        private boolean done = false;
        private DatagramSocket datagramSocket = null;

        /**
         * 存储消息的Buffer
         */
        final byte[] buffer = new byte[128];

        private Provider(String sn, Integer port) {
            this.sn = sn.getBytes();
            this.port = port;
        }

        @SuppressWarnings("DuplicatedCode")
        @Override
        public void run() {
            System.out.println("UDPProvider started...");

            try {
                // 监听30201端口，事实上如果udp是广播消息，此处也可以监听到
                datagramSocket = new DatagramSocket(UDPConstants.PORT_SERVER);
                // 接收消息的Packet
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);

                while (!done) {
                    // 接收
                    datagramSocket.receive(receivePack);

                    // 打印接收到的信息与发送者的信息
                    // 发送者的IP地址
                    String clientIp = receivePack.getAddress().getHostAddress();
                    int clientPort = receivePack.getPort();
                    int clientDataLength = receivePack.getLength();
                    byte[] clientData = receivePack.getData();
                    boolean isValid = clientDataLength >= (UDPConstants.HEADER.length + 2 + 4)
                            && ByteUtils.startsWith(clientData, UDPConstants.HEADER);

                    System.out.println("UDPProvider receive from ip: " + clientIp
                            + "\tport: " + clientPort
                            + "\tdataValid: " + isValid);

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
                    System.out.println("cmd: " + cmd + " responsePort: " + responsePort);

                    // 判断合法性
                    if (cmd == 1 && responsePort > 0) {
                        // 构建一份回送数据
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        byteBuffer.put(UDPConstants.HEADER);
                        byteBuffer.putShort((short) 2);
                        byteBuffer.putInt(port);
                        byteBuffer.put(sn);
                        int length = byteBuffer.position();

                        // 直接根据发送者构建一份回送信息
                        DatagramPacket responsePacket = new DatagramPacket(buffer,
                                length,
                                receivePack.getAddress(),
                                responsePort);
                        datagramSocket.send(responsePacket);

                        System.out.println("UDPProvider response to: " + clientIp
                            + "\tport: " + responsePort
                            + "\tdataLength: " + length);
                    } else {
                        System.out.println("UDPProvider receive cmd nonsupport; cmd: " + cmd + "\tport: " + port);
                    }
                }
            } catch (IOException ignored) {
            } finally {
                close();
            }

            // 完成
            System.out.println("UDPProvider finished.");
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
        public void exit() {
            done = true;
            close();
        }
    }
}
