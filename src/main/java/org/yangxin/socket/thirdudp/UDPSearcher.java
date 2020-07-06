package org.yangxin.socket.thirdudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * UDP搜索者，用于搜索服务支持方
 *
 * @author yangxin
 * 2020/07/06 16:34
 */
public class UDPSearcher {

    public static void main(String[] args) throws IOException {
        System.out.println("UDPSearcher started...");

        // 作为搜索方，让系统自动分配
        DatagramSocket datagramSocket = new DatagramSocket();

        // 构建一份请求数据
        String requestData = "HelloWorld!";
        byte[] requestDataBytes = requestData.getBytes();
        // 直接构建packet
        DatagramPacket requestPacket = new DatagramPacket(requestDataBytes, requestDataBytes.length);
        // 本机20000端口
        requestPacket.setAddress(InetAddress.getLocalHost());
        requestPacket.setPort(20000);

        // 发送
        datagramSocket.send(requestPacket);

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

        // 构建一份回送数据
        String responseData = "Receive data with len: " + length;
        byte[] responseDataBytes = responseData.getBytes();

        // 直接根据发送者构建一份回送信息
        DatagramPacket responsePacket = new DatagramPacket(responseDataBytes,
                responseDataBytes.length,
                receivePack.getAddress(),
                receivePack.getPort());
        datagramSocket.send(responsePacket);

        // 完成
        System.out.println("UDPSearcher finished...");
        datagramSocket.close();
    }
}
