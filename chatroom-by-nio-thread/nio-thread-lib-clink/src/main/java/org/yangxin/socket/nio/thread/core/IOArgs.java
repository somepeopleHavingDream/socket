package org.yangxin.socket.nio.thread.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * IO参数封装
 *
 * @author yangxin
 * 2020/08/12 16:06
 */
public class IOArgs {

    /**
     * 缓冲数组
     */
    private final byte[] byteBuffer = new byte[256];

    /**
     * 缓冲对象
     */
    private final ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);

    /**
     * 将数据从通道中读到缓冲区中
     *
     * @param channel 客户端通道
     * @return 读了多少个字节
     */
    public int read(SocketChannel channel) throws IOException {
        buffer.clear();
        return channel.read(buffer);
    }

    /**
     * 将缓冲区的数据写到通道中
     *
     * @param channel 通道
     * @return 写了多少个字节
     */
    public int write(SocketChannel channel) throws IOException {
        return channel.write(buffer);
    }

    /**
     * 将缓冲区中的字节转换成字符串返回
     *
     * @return 缓冲区中的数据，以字符串对象返回
     */
    public String bufferString() {
        // 丢弃换行符
        return new String(byteBuffer, 0, buffer.position() - 1);
    }

    /**
     * 输入输出参数事件监听器
     *
     * @author yangxin
     * 2020/08/12 16:08
     */
    public interface IOArgsEventListener {

        /**
         * IO开始
         */
        void onStarted(IOArgs args);

        /**
         * IO完成
         */
        void onCompleted(IOArgs args);
    }
}
