package org.yangxin.socket.nio.thread.core;

import java.io.IOException;
import java.util.Objects;

/**
 * IO全局环境
 *
 * @author yangxin
 * 2020/08/12 16:20
 */
public class IOContext {

    /**
     * 全局实例
     */
    private static IOContext INSTANCE;

    /**
     * io提供者
     */
    private final IOProvider ioProvider;

    /**
     * 构造方法私有，单例模式，只有一个io提供者对象
     */
    private IOContext(IOProvider ioProvider) {
        this.ioProvider = ioProvider;
    }

    public IOProvider getIoProvider() {
        return ioProvider;
    }

    public static IOContext get() {
        return INSTANCE;
    }

    /**
     * io环境设置/安装，返回启动引导对象
     */
    public static StartedBoot setup() {
        return new StartedBoot();
    }

    /**
     * 关闭，实质是调用callClose方法
     */
    public static void close() throws IOException {
        if (INSTANCE != null) {
            INSTANCE.callClose();
        }
    }

    /**
     * 实质是调用io提供者的关闭方法
     */
    private void callClose() throws IOException {
        ioProvider.close();
    }

    /**
     * 启动引导类
     *
     * @author yangxin
     * 2020/08/12 16:22
     */
    public static class StartedBoot {

        private IOProvider ioProvider;

        /**
         * 此处构造方法私有化，使得只有在外部类中才能创建此内部静态实例
         */
        private StartedBoot() {
        }

        /**
         * io复用提供者
         *
         * @param ioProvider 要设置的IOProvider对象
         * @return 启动引导类对象
         */
        public StartedBoot ioProvider(IOProvider ioProvider) {
            this.ioProvider = ioProvider;
            return this;
        }

        /**
         * 设置并返回IO环境变量
         *
         * @return 自定义全局io环境变量
         */
        @SuppressWarnings("UnusedReturnValue")
        public IOContext start() {
            INSTANCE = new IOContext(ioProvider);
            return INSTANCE;
        }
    }
}
