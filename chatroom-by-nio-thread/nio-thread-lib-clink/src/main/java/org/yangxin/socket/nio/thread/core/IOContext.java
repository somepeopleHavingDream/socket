package org.yangxin.socket.nio.thread.core;

import java.io.IOException;
import java.util.Objects;

/**
 * @author yangxin
 * 2020/08/12 16:20
 */
public class IOContext {

    private static IOContext INSTANCE;
    private final IOProvider ioProvider;

    /**
     * 构造方法私有，单例模式
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
     * 设置
     */
    public static StartedBoot setup() {
        return new StartedBoot();
    }

    public static void close() throws IOException {
        if (INSTANCE != null) {
            INSTANCE.callClose();
        }
    }

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

        private StartedBoot() {
        }

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

    public static void main(String[] args) {
        StartedBoot startedBoot1 = new StartedBoot();
        StartedBoot startedBoot2 = new StartedBoot();
        // false
        System.out.println(Objects.equals(startedBoot1, startedBoot2));
        // false
        System.out.println(startedBoot1 == startedBoot2);
        // false
        System.out.println(startedBoot1.equals(startedBoot2));
    }
}
