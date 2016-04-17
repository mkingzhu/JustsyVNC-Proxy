package com.justsy.vnc.proxy.server.net.selector;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.justsy.vnc.proxy.server.net.connection.Connection;
import com.justsy.vnc.proxy.server.net.handler.HandlerManager;

public class Server implements Runnable {
    private static final int    MAX_THREADS = 4;
    private static final Server INSTANCE    = new Server();

    private final List<Connection> connectionPool = new LinkedList<Connection>();
    private final HandlerManager   handlerManager = HandlerManager.getInstance();
    private Selector selector;

    public static synchronized Server getInstance() {
        return INSTANCE;
    }

    public void init(int port)
            throws Exception {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);

        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void addConnection(Connection connection) {
        synchronized (connectionPool) {
            if (!connection.isClosed())
                connectionPool.add(connectionPool.size(), connection);
        }
        selector.wakeup();
    }

    @Override
    public void run() {
        for (int i = 0; i < MAX_THREADS; i++) {
            new Reader().start();
            new Writer().start();
        }

        while (true) {
            try {
                if (selector.select() > 0) {
                    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove();
                        if (SelectionKey.OP_ACCEPT == (SelectionKey.OP_ACCEPT & key.readyOps())) {
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

                            SocketChannel socketChannel = serverSocketChannel.accept();
                            socketChannel.configureBlocking(false);

                            registerNewConnection(new Connection(socketChannel));
                        } else {
                            Connection connection = (Connection) key.attachment();
                            connection.resetOps();
                            if (SelectionKey.OP_READ == (SelectionKey.OP_READ & key.readyOps())) {
                                Reader.process(connection);
                            }
                            if (SelectionKey.OP_WRITE == (SelectionKey.OP_WRITE & key.readyOps())) {
                                Writer.process(connection);
                            }
                            key.cancel();
                        }
                    }
                } else {
                    registerConnection();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void registerNewConnection(Connection connection) {
        connection.setReadable();
        handlerManager.handleAccept(connection);
    }

    private void registerConnection() {
        synchronized (connectionPool) {
            while (!connectionPool.isEmpty()) {
                Connection connection = connectionPool.remove(0);
                try {
                    connection.getSocketChannel().register(selector, connection.getOps(), connection);
                } catch (Exception e) {
                    handlerManager.handleClose(connection);
                    handlerManager.handleException(connection, e);
                }
            }
        }
    }

    private Server() {
    }
}
