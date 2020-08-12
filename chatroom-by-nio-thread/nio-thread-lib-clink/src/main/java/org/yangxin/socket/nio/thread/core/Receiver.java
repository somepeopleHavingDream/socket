package org.yangxin.socket.nio.thread.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author yangxin
 * 2020/08/12 16:09
 */
public interface Receiver extends Closeable {

    boolean receiveAsync(IOArgs.IOArgsEventListener listener) throws IOException;
}
