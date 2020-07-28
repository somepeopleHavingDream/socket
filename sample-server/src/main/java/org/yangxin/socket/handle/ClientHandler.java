package org.yangxin.socket.handle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @author yangxin
 * 2020/07/28 16:46
 */
public class ClientHandler {

    private final Socket socket;
    private final ClientReadHandler readHandler;
    private final ClientWriteHandler writeHandler;
    private final ClientHandlerCallback clientHandlerCallback;

    private void exitBySelf() {
//        exit();
    }

    class ClientWriteHandler {

    }

    /**
     * @author yangxin
     * 2020/07/28 16:57
     */
    public interface ClientHandlerCallback {

        /**
         * 自身关闭通知
         */
        void onSelfClosed(ClientHandler handler);

        /**
         * 收到消息时通知
         */
        void onNewMessageArrived(ClientHandler handler, String msg);
    }

    /**
     * @author yangxin
     * 2020/07/28 16:48
     */
    class ClientReadHandler implements Runnable {

        private boolean done = false;
        private final InputStream inputStream;

        ClientReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try {
                // 得到输入流，用于接收数据
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));

                do {
                    // 客户端拿到一条数据
                    String str = socketInput.readLine();
                    if (str == null) {
                        System.out.println("客户端已无法读取数据！");

                        // 退出当前客户端
                        exitBySelf();
                        break;
                    }

                    // 通知到TCPServer
                    clientHandlerCallback.onNewMessageArrived(ClientHandler.this, str);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
