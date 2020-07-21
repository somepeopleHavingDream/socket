package org.yangxin.socket.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yangxin
 * 2020/07/21 20:48
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerInfo {

    private Integer port;
    private String address;
    private String sn;
}
