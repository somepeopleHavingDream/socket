package org.yangxin.socket.nio.thread.impl;

import org.yangxin.socket.nio.thread.core.IOArgs;
import org.yangxin.socket.nio.thread.core.IOProvider;
import org.yangxin.socket.nio.thread.core.Receiver;
import org.yangxin.socket.nio.thread.core.Sender;
import org.yangxin.socket.nio.thread.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 客户端通道适配器
 *
 * @author yangxin
 * 2020/08/12 16:03
 */
public class SocketChannelAdapter implements Sender, Receiver, Cloneable {

    /**
     * 是否关闭
     */
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    /**
     * 客户端通道
     */
    private final SocketChannel channel;

    /**
     * io提供者
     */
    private final IOProvider ioProvider;

    /**
     * 通道状态改变时监听器
     */
    private final OnChannelStatusChangedListener listener;

    /**
     * 接收IO事件监听器
     */
    private IOArgs.IOArgsEventListener receiveIOEventListener;
    /**
     * 发送IO事件监听器
     */
    private IOArgs.IOArgsEventListener sendIOEventListener;

    public SocketChannelAdapter(SocketChannel channel,
                                IOProvider ioProvider,
                                OnChannelStatusChangedListener listener) throws IOException {
        this.channel = channel;
        this.ioProvider = ioProvider;
        this.listener = listener;

        // 配置通道非阻塞
        channel.configureBlocking(false);
    }

    /**
     * 异步接收
     * 这里其实只做了两件事：
     * 1. 设置接收者的IO事件监听器
     * 2. 向io提供者，为该客户端通道注册输入回调
     *
     * @param listener 输入输出参数事件监听器
     * @return 是否异步接收消息成功
     */
    @Override
    public boolean receiveAsync(IOArgs.IOArgsEventListener listener) throws IOException {
        // 如果通道关闭，抛出io异常
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }

        // 设置接收IO事件监听器
        receiveIOEventListener = listener;

        // 注册输入回调方法
        return ioProvider.registerInput(channel, inputCallback);
    }

    /**
     * 异步发送
     * 这里其实也只做了两件事：
     * 1. 设置发送者的IO事件监听
     * 2. 向io提供者，为该客户端通道注册输出回调
     *
     * @param args IO参数封装对象
     * @param listener 监听对象
     * @return 是否消息发送成功
     */
    @Override
    public boolean sendAsync(IOArgs args, IOArgs.IOArgsEventListener listener) throws IOException {
        // 如果通道关闭，抛出io异常
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }

        sendIOEventListener = listener;
        // 当前发送的数据附加到回调中
        outputCallback.setAttach(args);
        return ioProvider.registerOutput(channel, outputCallback);
    }

    /**
     * 关闭套接字通道适配器
     */
    @Override
    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            // 解除注册回调
            ioProvider.unRegisterInput(channel);
            ioProvider.unRegisterOutput(channel);
            // 关闭
            CloseUtils.close(channel);
            // 回调当前Channel已关闭
            listener.onChannelClosed(channel);
        }
    }

    /**
     * 处理输入事件的回调方法
     */
    private final IOProvider.HandleInputCallback inputCallback = new IOProvider.HandleInputCallback() {

        /**
         * 提供者能够输入
         */
        @Override
        protected void canProviderInput() {
            if (isClosed.get()) {
                return;
            }

            IOArgs args = new IOArgs();
            IOArgs.IOArgsEventListener listener = SocketChannelAdapter.this.receiveIOEventListener;

            if (listener != null) {
                listener.onStarted(args);
            }

            try {
                // 具体的读取操作
                if (args.read(channel) > 0 && listener != null) {
                    // 读取完成回调（回调操作包括：打印、继续读取下一条数据（继续为接收者设置IO事件监听器，注册输入回调方法））
                    listener.onCompleted(args);
                } else {
                    throw new IOException("Cannot read any data!");
                }
            } catch (IOException ignored) {
                // 如果发生异常，则关闭本套接字通道适配器
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }
    };

    private final IOProvider.HandleOutputCallback outputCallback = new IOProvider.HandleOutputCallback() {

        @Override
        protected void canProviderOutput(Object attach) {
            if (isClosed.get()) {
                return;
            }

            sendIOEventListener.onCompleted(null);
        }
    };

    /**
     * 通道状态改变监听
     *
     * @author yangxin
     * 2020/08/12 16:14
     */
    public interface OnChannelStatusChangedListener {

        /**
         * 当通道关闭时
         *
         * @param channel 客户端通道
         */
        void onChannelClosed(SocketChannel channel);
    }
}
