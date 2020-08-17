package org.yangxin.socket.nio.thread.core;

import org.yangxin.socket.nio.thread.impl.SocketChannelAdapter;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

/**
 * 连接器
 *
 * @author yangxin
 * 2020/08/12 16:02
 */
public class Connector implements Closeable, SocketChannelAdapter.OnChannelStatusChangedListener {

    /**
     * uuid
     */
    private final UUID key = UUID.randomUUID();

    /**
     * 接收者
     */
    private Receiver receiver;

    /**
     * 连接器的相关设置
     *
     * @param socketChannel 客户端通道
     */
    public void setup(SocketChannel socketChannel) throws IOException {
        // 获取IO环境
        IOContext context = IOContext.get();
        // 设置接收者
        this.receiver = new SocketChannelAdapter(socketChannel, context.getIoProvider(), this);

        // 读取下一条消息（设置监听、注册输入回调）
        readNextMessage();
    }

    /**
     * 读取下一条消息
     */
    private void readNextMessage() {
        if (receiver != null) {
            try {
                // 接收者异步接收
                receiver.receiveAsync(echoReceiveListener);
            } catch (IOException e) {
                System.out.println("开始接收数据异常：" + e.getMessage());
            }
        }
    }

    /**
     * 关闭连接
     */
    @Override
    public void close() {

    }

    /**
     * 当客户端的通道关闭时做的处理
     *
     * @param channel 客户端通道
     */
    @Override
    public void onChannelClosed(SocketChannel channel) {

    }

    /**
     * 接收事件监听器
     */
    private final IOArgs.IOArgsEventListener echoReceiveListener = new IOArgs.IOArgsEventListener() {

        /**
         * 当启动时
         * @param args io参数
         */
        @Override
        public void onStarted(IOArgs args) {

        }

        /**
         * 当完成时
         * @param args io参数
         */
        @Override
        public void onCompleted(IOArgs args) {
            // 打印
            onReceiveNewMessage(args.bufferString());
            // 读取下一条数据（继续为接收者设置IO事件监听器、注册输入回调方法）
            readNextMessage();
        }
    };

    /**
     * 在接收者接收到新的消息时的处理
     *
     * @param str 新的消息
     */
    protected void onReceiveNewMessage(String str) {
        System.out.println(key.toString() + ":" + str);
    }
}
