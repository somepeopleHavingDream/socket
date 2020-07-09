package org.yangxin.socket.fiveudptcp.server;

import org.yangxin.socket.fiveudptcp.clink.utils.ByteUtils;
import org.yangxin.socket.fiveudptcp.constants.UDPConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author yangxin
 * 2020/07/07 20:52
 */
public class ServerProvider {

    private static Provider PROVIDER_INSTANCE;

    public static void start(int port) {
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

    private static class Provider implements Runnable {

        private final byte[] sn;
        private final int port;
        private boolean done = false;
        private DatagramSocket datagramSocket = null;

        /**
         * 存储消息的Buffer
         */
        final byte[] buffer = new byte[128];

        Provider(String sn, int port) {
            this.sn = sn.getBytes();
            this.port = port;
        }

        @Override
        public void run() {
            System.out.println("UDPProvider started...");

            try {
                // 监听端口
                datagramSocket = new DatagramSocket(UDPConstants.PORT_SERVER);
                // 接收消息的Packet
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);

                while (!done) {
                    // 接收
                    datagramSocket.receive(receivePack);

                    // 打印接收到的信息与发送者的信息
                    // 发送者的IP地址
                    String clientIP = receivePack.getAddress().getHostAddress();
                    int clientPort = receivePack.getPort();
                    int clientDataLength = receivePack.getLength();
                    byte[] clientData = receivePack.getData();
                    System.out.println("clientData: " + Arrays.toString(clientData));
                    boolean isValid = clientDataLength >= (UDPConstants.HEADER.length + 2 + 4)
                            && ByteUtils.startsWith(clientData, UDPConstants.HEADER);

                    System.out.println("ServerProvider receive from ip: " + clientIP
                            + "\tport: " + clientPort + "\tdataValid: " + isValid);

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

                        System.out.println("ServerProvider response to: " + clientIP
                                + "\tport: " + responsePort
                                + "\tdataLength: " + length);
                    } else {
                        System.out.println("ServerProvider receive cmd nonsupport; cmd: " + cmd + "\tport: " + port);
                    }
                }
            } catch (IOException ignored) {
            } finally {
                close();
            }
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
        void exit() {
            done = true;
            close();
        }
    }
}
