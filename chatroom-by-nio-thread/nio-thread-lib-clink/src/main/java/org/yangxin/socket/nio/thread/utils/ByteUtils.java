package org.yangxin.socket.nio.thread.utils;

/**
 * @author yangxin
 * 2020/08/12 16:36
 */
public class ByteUtils {

    public static boolean startsWith(byte[] source, byte[] match) {
        return startsWith(source, 0, match);
    }

    public static boolean startsWith(byte[] source, int offset, byte[] match) {
        if (match.length > (source.length - offset)) {
            return false;
        }

        for (int i = 0; i < match.length; i++) {
            if (source[offset + i] != match[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean equals(byte[] source, byte[] match) {
        if (match.length != source.length) {
            return false;
        }
        return startsWith(source, 0, match);
    }

    public static void getBytes(byte[] source, int srcBegin, int srcEnd, byte[] destination,
                                int dstBegin) {
        System.arraycopy(source, srcBegin, destination, dstBegin, srcEnd - srcBegin);
    }

    public static byte[] subBytes(byte[] source, int srcBegin, int srcEnd) {
        byte[] destination;

        destination = new byte[srcEnd - srcBegin];
        getBytes(source, srcBegin, srcEnd, destination, 0);

        return destination;
    }

    public static byte[] subBytes(byte[] source, int srcBegin) {
        return subBytes(source, srcBegin, source.length);
    }
}
