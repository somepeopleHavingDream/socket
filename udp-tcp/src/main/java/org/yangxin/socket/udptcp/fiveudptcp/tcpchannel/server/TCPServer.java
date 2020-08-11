package org.yangxin.socket.udptcp.fiveudptcp.tcpchannel.server;

import org.yangxin.socket.udptcp.fiveudptcp.tcpchannel.server.handle.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yangxin
 * 2020/07/16 21:03
 */
public class TCPServer {

    private final int port;
    private ClientListener mListener;
    private final List<ClientHandler> clientHandlerList = new ArrayList<>();

    public TCPServer(int port) {
        this.port = port;
    }

    public void broadcast(String str) {
        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.send(str);
        }
    }

    public boolean start() {
        try {
            ClientListener listener = new ClientListener(port);
            mListener = listener;
            Thread thread = new Thread(listener);
            listener.setThread(thread);
            thread.start();
//            new Thread(listener).start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop() {
        if (mListener != null) {
            mListener.exit();
        }

        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.exit();
        }

        clientHandlerList.clear();
    }

    /**
     * 负责对客户端进行监听
     *
     * @author yangxin
     * 2020/07/16 21:03
     */
    private class ClientListener implements Runnable {

        private final ServerSocket server;
//        private boolean done = false;
        private Thread thread;

        private ClientListener(int port) throws IOException {
            server = new ServerSocket(port);
            System.out.println("服务器信息：" + server.getInetAddress() + " P:" + server.getLocalPort());
        }

        public void setThread(Thread thread) {
            this.thread = thread;
        }

        @Override
        public void run() {
            System.out.println("服务器准备就绪～");

            // 等待客户端连接
            do {
                // 得到客户端
                Socket client;
                try {
                    client = server.accept();
                } catch (IOException e) {
                    continue;
                }

                try {
                    // 客户端构建异步线程
                    ClientHandler clientHandler = new ClientHandler(client,
                            clientHandlerList::remove);
                    // 读取数据并打印
                    clientHandler.readToPrint();
                    clientHandlerList.add(clientHandler);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("客户端连接异常：" + e.getMessage());
                }
            } while (!Thread.interrupted());
//            } while (!done);

            System.out.println("服务器已关闭！");
        }

        void exit() {
//            done = true;
            thread.interrupt();
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
