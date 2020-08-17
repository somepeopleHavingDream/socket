package org.yangxin.socket.nio.thread.core;

import java.io.Closeable;
import java.nio.channels.SocketChannel;

/**
 * IO提供者，定义了需要实现的通道与回调处理方法
 *
 * @author yangxin
 * 2020/08/12 16:11
 */
public interface IOProvider extends Closeable {

    /**
     * 注册输入回调
     *
     * @param channel 通道
     * @param callback 回调
     * @return 注册是否成功
     */
    boolean registerInput(SocketChannel channel, HandleInputCallback callback);

    /**
     * 注册输出回调
     *
     * @param channel 通道
     * @param callback 回调
     * @return 注册是否成功
     */
    boolean registerOutput(SocketChannel channel, HandleOutputCallback callback);

    /**
     * 取消注册输入回调
     *
     * @param channel 通道
     */
    void unRegisterInput(SocketChannel channel);

    /**
     * 取消注册输出回调
     *
     * @param channel 通道
     */
    void unRegisterOutput(SocketChannel channel);

    /**
     * 处理输入回调，实现了线程
     *
     * @author yangxin
     * 2020/08/12 16:12
     */
    abstract class HandleInputCallback implements Runnable {

        @Override
        public final void run() {
            canProviderInput();
        }

        /**
         * 能否提供者输入
         */
        protected abstract void canProviderInput();
    }

    /**
     * 处理输出回调，实现了线程
     *
     * @author yangxin
     * 2020/08/12 16:12
     */
    abstract class HandleOutputCallback implements Runnable {

        /**
         * 附加物，包含了需要输出的信息
         */
        private Object attach;

        @Override
        public final void run() {
            canProviderOutput(attach);
        }

        /**
         * 设置附加物
         *
         * @param attach 附加物/附带信息
         */
        public final void setAttach(Object attach) {
            this.attach = attach;
        }

        /**
         * 是否能够提供者输出
         *
         * @param attach 附加物/附加信息
         */
        protected abstract void canProviderOutput(Object attach);
    }
}
