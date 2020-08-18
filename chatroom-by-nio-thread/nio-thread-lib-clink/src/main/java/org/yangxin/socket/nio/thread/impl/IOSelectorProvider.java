package org.yangxin.socket.nio.thread.impl;

import org.yangxin.socket.nio.thread.core.IOProvider;
import org.yangxin.socket.nio.thread.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * IO选择器提供者
 *
 * @author yangxin
 * 2020/08/12 16:27
 */
public class IOSelectorProvider implements IOProvider {

    /**
     * 是否关闭
     */
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    /**
     * 是否处于某个过程（处于注册的输入回调）
     */
    private final AtomicBoolean inRegInput = new AtomicBoolean(false);
    /**
     * （处于注册的输出回调）
     */
    private final AtomicBoolean inRegOutput = new AtomicBoolean(false);

    /**
     * 读选择器
     */
    private final Selector readSelector;
    /**
     * 写选择器
     */
    private final Selector writeSelector;

    /**
     * 输入回调map
     */
    private final Map<SelectionKey, Runnable> inputCallbackMap = new HashMap<>();
    /**
     * 输出回调map
     */
    private final Map<SelectionKey, Runnable> outputCallbackMap = new HashMap<>();

    /**
     * 读事件处理线程池
     */
    private final ExecutorService inputHandlePool;
    /**
     * 写事件处理线程池
     */
    private final ExecutorService outputHandlePool;

    public IOSelectorProvider() throws IOException {
        // 打开读写选择器
        readSelector = Selector.open();
        writeSelector = Selector.open();

//        inputHandlePool = Executors.newFixedThreadPool(4,
//                new IOProviderThreadFactory("IoProvider-Input-Thread-"));
//        outputHandlePool = Executors.newFixedThreadPool(4,
//                new IOProviderThreadFactory("IoProvider-Output-Thread-"));
        // 创建读写处理线程池
        inputHandlePool = Executors.newFixedThreadPool(4);
        outputHandlePool = Executors.newFixedThreadPool(4);

        // 开始输出输入的监听
        startRead();
        startWrite();
    }

    /**
     * 开启对输入事件的监听
     */
    private void startRead() {
        Thread thread = new Thread("Clink IoSelectorProvider ReadSelector Thread") {

            @Override
            public void run() {
                while (!isClosed.get()) {
                    try {
                        /*
                            Q: select()方法不是要阻塞直到至少有一个channel被selected，或者线程被中断才返回吗？
                            A: select()可以被直接唤醒（selector.wakeup()），退出等待状态，此时得到的值可能是0
                         */
                        if (readSelector.select() == 0) {
                            waitSelection(inRegInput);
                            continue;
                        }

                        // 处理读事件
                        Set<SelectionKey> selectionKeys = readSelector.selectedKeys();
                        for (SelectionKey selectionKey : selectionKeys) {
                            if (selectionKey.isValid()) {
                                handleSelection(selectionKey, SelectionKey.OP_READ, inputCallbackMap, inputHandlePool);
                            }
                        }
                        selectionKeys.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        };
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    /**
     * 开启对输出事件的监听
     */
    private void startWrite() {
        Thread thread = new Thread("Clink IoSelectorProvider WriteSelector Thread") {

            @Override
            public void run() {
                while (!isClosed.get()) {
                    try {
                        if (writeSelector.select() == 0) {
                            waitSelection(inRegOutput);
                            continue;
                        }

                        // 处理事件
                        Set<SelectionKey> selectionKeys = writeSelector.selectedKeys();
                        for (SelectionKey selectionKey : selectionKeys) {
                            if (selectionKey.isValid()) {
                                handleSelection(selectionKey,
                                        SelectionKey.OP_WRITE,
                                        outputCallbackMap,
                                        outputHandlePool);
                            }
                        }
                        selectionKeys.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    /**
     * 注册输入
     *
     * @param channel 通道
     * @param callback 回调
     * @return 是否注册输入成功
     */
    @Override
    public boolean registerInput(SocketChannel channel, HandleInputCallback callback) {
        return registerSelection(channel,
                readSelector,
                SelectionKey.OP_READ,
                inRegInput,
                inputCallbackMap,
                callback) != null;
    }

    /**
     * 注册输出
     *
     * @param channel 通道
     * @param callback 回调
     * @return 是否注册输出成功
     */
    @Override
    public boolean registerOutput(SocketChannel channel, HandleOutputCallback callback) {
        return registerSelection(channel,
                writeSelector,
                SelectionKey.OP_WRITE,
                inRegOutput,
                outputCallbackMap, callback) != null;
    }

    /**
     * 取消注册输入
     *
     * @param channel 通道
     */
    @Override
    public void unRegisterInput(SocketChannel channel) {
        unRegisterSelection(channel, readSelector, inputCallbackMap);
    }

    /**
     * 取消注册输出
     *
     * @param channel 通道
     */
    @Override
    public void unRegisterOutput(SocketChannel channel) {
        unRegisterSelection(channel, writeSelector, outputCallbackMap);
    }

    /**
     * IO选择器提供者关闭
     */
    @Override
    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            // 输入输出事件处理线程池关闭
            inputHandlePool.shutdown();
            outputHandlePool.shutdown();

            // 清空输入输出回调
            inputCallbackMap.clear();
            outputCallbackMap.clear();

            readSelector.wakeup();
            writeSelector.wakeup();

            CloseUtils.close(readSelector, writeSelector);
        }
    }

    /**
     * 等待选择
     */
    private static void waitSelection(final AtomicBoolean locker) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (locker) {
            if (locker.get()) {
                try {
                    locker.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 注册选择
     *
     * @param channel 套接字通道
     * @param selector 选择器
     * @param registerOps 注册操作
     * @param locker 锁
     * @param map map
     * @param runnable 可运行
     */
    private static SelectionKey registerSelection(SocketChannel channel,
                                                  Selector selector,
                                                  int registerOps,
                                                  AtomicBoolean locker,
                                                  Map<SelectionKey, Runnable> map,
                                                  Runnable runnable) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (locker) {
            // 设置锁定状态
            locker.set(true);

            try {
                // 唤醒当前的selector，让selector不处于select()状态
                selector.wakeup();

                SelectionKey key = null;
                if (channel.isRegistered()) {
                    // 查询是否已经注册过
                    key = channel.keyFor(selector);
                    if (key != null) {
                        key.interestOps(key.readyOps() | registerOps);
                    }
                }

                if (key == null) {
                    // 注册selector得到Key
                    key = channel.register(selector, registerOps);
                    // 注册回调（也许在这里并不需要通过通道的某一时间来确定需要执行哪一个回调操作，这也就是为什么key类型是SelectionKey）
                    // 并且SelectionKey在注册到channel时，不会因为感兴趣的事件的不同而产生不同的SelectionKey。
                    map.put(key, runnable);
                }

                return key;
            } catch (ClosedChannelException e) {
                return null;
            } finally {
                // 解除锁定状态
                locker.set(false);
                try {
                    // 通知
                    locker.notify();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * 取消注册选择
     *
     * @param channel 客户端通道
     * @param selector 选择器
     * @param map map
     */
    private static void unRegisterSelection(SocketChannel channel, Selector selector,
                                            Map<SelectionKey, Runnable> map) {
        if (channel.isRegistered()) {
            SelectionKey key = channel.keyFor(selector);
            if (key != null) {
                // 取消监听的方法
                key.cancel();
                map.remove(key);
                selector.wakeup();
            }
        }
    }

    /**
     * 处理读事件
     */
    private static void handleSelection(SelectionKey key, int keyOps,
                                        Map<SelectionKey, Runnable> map,
                                        ExecutorService pool) {
        // 重点
        // 取消继续对keyOps的监听
        // 对于键来说感兴趣的操作
        key.interestOps(key.interestOps() & ~keyOps);

        Runnable runnable = null;
        try {
            runnable = map.get(key);
        } catch (Exception ignored) {
        }

        if (runnable != null && !pool.isShutdown()) {
            // 异步调度
            pool.execute(runnable);
        }
    }

    /**
     * @author yangxin
     * 2020/08/12 16:30
     */
    static class IOProviderThreadFactory implements ThreadFactory {

        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        IOProviderThreadFactory(String namePrefix) {
            SecurityManager s = System.getSecurityManager();
            this.group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
