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
 * @author yangxin
 * 2020/08/12 16:03
 */
public class SocketChannelAdapter implements Sender, Receiver, Cloneable{

    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final SocketChannel channel;
    private final IOProvider ioProvider;
    private final OnChannelStatusChangedListener listener;

    private IOArgs.IOArgsEventListener receiveIoEventListener;
    private IOArgs.IOArgsEventListener sendIoEventListener;

    public SocketChannelAdapter(SocketChannel channel, IOProvider ioProvider,
                                OnChannelStatusChangedListener listener) throws IOException {
        this.channel = channel;
        this.ioProvider = ioProvider;
        this.listener = listener;

        channel.configureBlocking(false);
    }

    @Override
    public boolean receiveAsync(IOArgs.IOArgsEventListener listener) throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }

        receiveIoEventListener = listener;

        return ioProvider.registerInput(channel, inputCallback);
    }

    @Override
    public boolean sendAsync(IOArgs args, IOArgs.IOArgsEventListener listener) throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }

        sendIoEventListener = listener;
        // 当前发送的数据附加到回调中
        outputCallback.setAttach(args);
        return ioProvider.registerOutput(channel, outputCallback);
    }

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

    private final IOProvider.HandleInputCallback inputCallback = new IOProvider.HandleInputCallback() {

        @Override
        protected void canProviderInput() {
            if (isClosed.get()) {
                return;
            }

            IOArgs args = new IOArgs();
            IOArgs.IOArgsEventListener listener = SocketChannelAdapter.this.receiveIoEventListener;

            if (listener != null) {
                listener.onStarted(args);
            }

            try {
                // 具体的读取操作
                if (args.read(channel) > 0 && listener != null) {
                    // 读取完成回调
                    listener.onCompleted(args);
                } else {
                    throw new IOException("Cannot read any data!");
                }
            } catch (IOException ignored) {
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

            sendIoEventListener.onCompleted(null);
        }
    };

    /**
     * @author yangxin
     * 2020/08/12 16:14
     */
    public interface OnChannelStatusChangedListener {

        void onChannelClosed(SocketChannel channel);
    }
}
