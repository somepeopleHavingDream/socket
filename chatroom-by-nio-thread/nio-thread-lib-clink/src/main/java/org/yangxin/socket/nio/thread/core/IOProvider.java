package org.yangxin.socket.nio.thread.core;

import java.io.Closeable;
import java.nio.channels.SocketChannel;

/**
 * @author yangxin
 * 2020/08/12 16:11
 */
public interface IOProvider extends Closeable {

    boolean registerInput(SocketChannel channel, HandleInputCallback callback);

    boolean registerOutput(SocketChannel channel, HandleOutputCallback callback);

    void unRegisterInput(SocketChannel channel);

    void unRegisterOutput(SocketChannel channel);

    /**
     * @author yangxin
     * 2020/08/12 16:12
     */
    abstract class HandleInputCallback implements Runnable {

        @Override
        public final void run() {
            canProviderInput();
        }

        protected abstract void canProviderInput();
    }

    /**
     * @author yangxin
     * 2020/08/12 16:12
     */
    abstract class HandleOutputCallback implements Runnable {

        private Object attach;

        @Override
        public final void run() {
            canProviderOutput(attach);
        }

        public final void setAttach(Object attach) {
            this.attach = attach;
        }

        protected abstract void canProviderOutput(Object attach);
    }
}
