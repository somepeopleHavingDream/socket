package org.yangxin.socket.fiveudptcp.tcp.client.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yangxin
 * 2020/07/14 21:07
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ServerInfo {

    private Integer port;
    private String sn;
    private String address;
}
