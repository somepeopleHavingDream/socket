package org.yangxin.socket.nio.thread.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author yangxin
 * 2020/08/12 16:17
 */
public class CloseUtils {

    public static void close(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
