package org.yangxin.socket.nio.thread.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author yangxin
 * 2020/08/12 16:04
 */
public interface Sender extends Closeable {

    boolean sendAsync(IOArgs args, IOArgs.IOArgsEventListener listener) throws IOException;
}
