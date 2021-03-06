package org.yangxin.socket.udptcp.fiveudptcp.tcpchannel.client.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yangxin
 * 2020/07/15 16:32
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ServerInfo {

    private Integer port;
    private String address;
    private String sn;
}
