package com.justsy.vnc.proxy.server.net.handler;

import java.nio.ByteBuffer;

import com.justsy.vnc.proxy.server.net.connection.Connection;

public abstract class AbstractHandler implements Handler {
    @Override
    public void onException(Connection connection, Exception e)
            throws Exception {
    }

    @Override
    public void onAccept(Connection connection)
            throws Exception {
    }

    @Override
    public void onRead(Connection connection, ByteBuffer data)
            throws Exception {
    }

    @Override
    public void onWrite(Connection connection)
            throws Exception {
    }

    @Override
    public void onClose(Connection connection)
            throws Exception {
    }
}
