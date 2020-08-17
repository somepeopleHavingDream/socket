package org.yangxin.socket.nio.thread.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * 接收者
 *
 * @author yangxin
 * 2020/08/12 16:09
 */
public interface Receiver extends Closeable {

    /**
     * 异步接收
     *
     * @param listener 输入输出参数事件监听器
     * @return 是否异步接收成功
     */
    @SuppressWarnings("UnusedReturnValue")
    boolean receiveAsync(IOArgs.IOArgsEventListener listener) throws IOException;
}
