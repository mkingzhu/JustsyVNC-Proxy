package com.justsy.vnc.proxy.server.net.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.justsy.vnc.proxy.server.net.selector.Server;

public class Connection {
    private static final Server SERVER = Server.getInstance();

    private final SocketChannel socketChannel;
    private       int           ops;

    public Connection(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public boolean isClosed() {
        return socketChannel.socket().isClosed();
    }

    public void close()
            throws IOException {
        socketChannel.socket().close();
        socketChannel.close();
    }

    public void write(ByteBuffer data)
            throws IOException {
        while (data.remaining() > 0)
            socketChannel.write(data);
    }

    public int getOps() {
        return ops;
    }

    public void resetOps() {
        synchronized (this) {
            this.ops = 0;
        }
    }

    public void setReadable() {
        synchronized (this) {
            ops |= SelectionKey.OP_READ;
            SERVER.addConnection(this);
        }
    }

    public void setWritable() {
        synchronized (this) {
            ops |= SelectionKey.OP_WRITE;
            SERVER.addConnection(this);
        }
    }
}
