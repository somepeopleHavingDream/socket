package org.yangxin.socket.nio.thread.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author yangxin
 * 2020/08/12 16:06
 */
public class IOArgs {

    private final byte[] byteBuffer = new byte[256];
    private final ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);

    public int read(SocketChannel channel) throws IOException {
        buffer.clear();
        return channel.read(buffer);
    }

    public int write(SocketChannel channel) throws IOException {
        return channel.write(buffer);
    }

    public String bufferString() {
        // 丢弃换行符
        return new String(byteBuffer, 0, buffer.position() - 1);
    }

    /**
     * @author yangxin
     * 2020/08/12 16:08
     */
    public interface IOArgsEventListener {

        void onStarted(IOArgs args);
        void onCompleted(IOArgs args);
    }
}
