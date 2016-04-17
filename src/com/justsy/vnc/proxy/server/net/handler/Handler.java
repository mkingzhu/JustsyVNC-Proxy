package com.justsy.vnc.proxy.server.net.handler;

import java.nio.ByteBuffer;

import com.justsy.vnc.proxy.server.net.connection.Connection;

interface Handler {
    void onException(Connection connection, Exception e) throws Exception;

    void onAccept(Connection connection) throws Exception;

    void onRead(Connection connection, ByteBuffer data) throws Exception;

    void onWrite(Connection connection) throws Exception;

    void onClose(Connection connection) throws Exception;
}
