package com.justsy.vnc.proxy.server.net.handler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.justsy.vnc.proxy.server.net.connection.Connection;

public class HandlerManager {
    private static final HandlerManager INSTANCE = new HandlerManager();

    private final List<Handler> handlers = new ArrayList<Handler>();

    public static synchronized HandlerManager getInstance() {
        return INSTANCE;
    }

    public void addHandler(Handler handler) {
        synchronized (handlers) {
            if (!handlers.contains(handler))
                handlers.add(handler);
        }
    }

    public void handleException(Connection connection, Exception e) {
        for (int i = handlers.size() - 1; i >= 0; i--)
            try {
                handlers.get(i).onException(connection, e);
            } catch (Exception ignore) {
            }
    }

    public void handleAccept(Connection connection) {
        for (int i = handlers.size() - 1; i >= 0; i--)
            try {
                handlers.get(i).onAccept(connection);
            } catch (Exception ignore) {
            }
    }

    public void handleRead(Connection connection, ByteBuffer data) {
        for (int i = handlers.size() - 1; i >= 0; i--)
            try {
                handlers.get(i).onRead(connection, data);
            } catch (Exception ignore) {
            }
    }

    public void handleWrite(Connection connection) {
        for (int i = handlers.size() - 1; i >= 0; i--)
            try {
                handlers.get(i).onWrite(connection);
            } catch (Exception ignore) {
            }
    }

    public void handleClose(Connection connection) {
        for (int i = handlers.size() - 1; i >= 0; i--)
            try {
                handlers.get(i).onClose(connection);
            } catch (Exception ignore) {
            }
    }

    private HandlerManager() {
    }
}
