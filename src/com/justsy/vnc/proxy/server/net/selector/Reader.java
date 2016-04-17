package com.justsy.vnc.proxy.server.net.selector;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import com.justsy.vnc.proxy.server.net.connection.Connection;
import com.justsy.vnc.proxy.server.net.handler.HandlerManager;

class Reader extends Thread {
    private static final int              BUFFER_SIZE     = 1024 * 1024;
    private static final List<Connection> CONNECTION_POOL = new LinkedList<Connection>();

    private final HandlerManager handlerManager = HandlerManager.getInstance();

    static void process(Connection connection) {
        synchronized (CONNECTION_POOL) {
            CONNECTION_POOL.add(CONNECTION_POOL.size(), connection);
            CONNECTION_POOL.notifyAll();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Connection connection;
                synchronized (CONNECTION_POOL) {
                    while (CONNECTION_POOL.isEmpty())
                        CONNECTION_POOL.wait();

                    connection = CONNECTION_POOL.remove(0);
                }
                doRead(connection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doRead(Connection connection) {
        try {
            ByteBuffer data = ByteBuffer.allocate(BUFFER_SIZE);
            if (-1 == connection.getSocketChannel().read(data)) {
                handlerManager.handleClose(connection);
            } else {
                data.flip();
                handlerManager.handleRead(connection, data);
            }
        } catch (Exception e) {
            handlerManager.handleClose(connection);
            handlerManager.handleException(connection, e);
        }
    }
}
