package org.yangxin.socket.thirdudp;

/**
 * @author yangxin
 * 2020/07/05 17:13
 */
public class MessageCreator {

    private static final String SN_HEADER = "收到暗号，我是（SN）：";
    private static final String PORT_HEADER = "这是暗号，请回电端口（Port）：";

    public static String buildWithPort(int port) {
        return PORT_HEADER + port;
    }

    public static int parsePort(String data) {
        return data.startsWith(PORT_HEADER)
                ? Integer.parseInt(data.substring(PORT_HEADER.length()))
                : -1;
    }

    public static String buildWithSn(String sn) {
        return SN_HEADER + sn;
    }

    public static String parseSn(String data) {
        return data.startsWith(SN_HEADER)
                ? data.substring(SN_HEADER.length())
                : null;
    }
}
