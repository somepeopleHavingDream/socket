package org.yangxin.socket.fiveudptcp.tcp.constants;

/**
 * @author yangxin
 * 2020/07/07 20:46
 */
public class UDPConstants {

    /**
     * 公有头部
     */
    public static byte[] HEADER = new byte[] {7, 7, 7, 7, 7, 7, 7, 7};

    /**
     * 服务器固话UDP接收端口
     */
    public static Integer PORT_SERVER = 30201;

    /**
     * 客户端回送端口
     */
    public static Integer PORT_CLIENT_RESPONSE = 30202;
}
