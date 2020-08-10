package org.yangxin.socket;

import org.yangxin.socket.handle.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yangxin
 * 2020/08/03 16:56
 */
public class TCPServer implements ClientHandler.ClientHandlerCallback {

    private final int port;
    private ClientListener mListener;
    private final List<ClientHandler> clientHandlerList = new ArrayList<>();
    private final ExecutorService forwardingThreadPoolExecutor;

    public TCPServer(int port) {
        this.port = port;
        // 转发线程池
        this.forwardingThreadPoolExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * tcp服务端开启对客户端的监听
     */
    public boolean start() {
        try {
            ClientListener listener = new ClientListener(port);
            mListener = listener;
            Thread thread = new Thread(listener);
            listener.setThread(thread);
            thread.start();
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

        synchronized (TCPServer.this) {
            for (ClientHandler clientHandler : clientHandlerList) {
                clientHandler.exit();
            }

            clientHandlerList.clear();
        }

        // 停止线程池
        forwardingThreadPoolExecutor.shutdownNow();
    }

    public synchronized void broadcast(String str) {
        for (ClientHandler clientHandler : clientHandlerList) {
            // 每个处理器实际上也是用线程完成发送的
            clientHandler.send(str);
        }
    }

    @Override
    public synchronized void onSelfClosed(ClientHandler handler) {
        clientHandlerList.remove(handler);
    }

    @Override
    public void onNewMessageArrived(ClientHandler handler, String msg) {
        // 打印到屏幕
        System.out.println("Received-" + handler.getClientInfo() + ":" + msg);
        // 异步提交转发任务
        forwardingThreadPoolExecutor.execute(() -> {
            synchronized (TCPServer.this) {
                for (ClientHandler clientHandler : clientHandlerList) {
                    if (clientHandler.equals(handler)) {
                        // 跳过自己
                        continue;
                    }
                    // 对其他客户端发送消息
                    clientHandler.send(msg);
                }
            }
        });
    }

    /**
     * 在服务端的客户端监听程序
     *
     * @author yangxin
     * 2020/08/03 16:57
     */
    private class ClientListener implements Runnable {

        private final ServerSocket server;
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
                    ClientHandler clientHandler = new ClientHandler(client, TCPServer.this);
                    // 读取数据并打印，开启对读事件的处理线程
                    clientHandler.readToPrint();
                    // 添加同步处理
                    synchronized (TCPServer.this) {
                        clientHandlerList.add(clientHandler);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("客户端连接异常：" + e.getMessage());
                }
            } while (!Thread.interrupted());

            System.out.println("服务器已关闭！");
        }

        void exit() {
            thread.interrupt();
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
