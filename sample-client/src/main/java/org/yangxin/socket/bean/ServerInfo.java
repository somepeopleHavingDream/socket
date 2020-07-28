package org.yangxin.socket.bean;

import lombok.Data;

/**
 * @author yangxin
 * 2020/07/28 15:43
 */
@Data
public class ServerInfo {

    private String sn;
    private Integer port;
    private String address;

    public ServerInfo(int port, String ip, String sn) {
        this.port = port;
        this.address = ip;
        this.sn = sn;
    }
}
