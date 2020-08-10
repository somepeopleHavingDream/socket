package org.yangxin.socket.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author yangxin
 * 2020/08/03 16:36
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
