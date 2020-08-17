package org.yangxin.socket.nio.thread.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * 发送者
 *
 * @author yangxin
 * 2020/08/12 16:04
 */
public interface Sender extends Closeable {

    /**
     * 异步发送
     *
     * @param args IO参数封装对象
     * @param listener 监听对象
     * @return 是否发送成功（很有可能是设置发送监听，注册发送回调，跟receiveAsync相似）
     */
    boolean sendAsync(IOArgs args, IOArgs.IOArgsEventListener listener) throws IOException;
}
