package com.justsy.vnc.proxy.server.main.connection;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import com.justsy.vnc.proxy.server.net.connection.Connection;

public abstract class VncConnection {
    public static final int RESPONSE_OK          = 200;
    public static final int RESPONSE_NOT_CONFIRM = 201;

    public static final int STATUS_ESTABLISHING = 110;
    public static final int STATUS_ESTABLISHED  = 111;
    public static final int STATUS_PAIRING      = 120;
    public static final int STATUS_PAIRED       = 121;
    public static final int STATUS_NOT_CONFIRM  = 130;

    private int  status;
    private long lastActiveTime = 0L;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void setReadable() {
        connection.setReadable();
    }

    public void setWritable() {
        connection.setWritable();
    }

    public abstract boolean isValid();

    private static final int    PREFIX_LENGTH     = 21;
    private static final byte[] PREFIX_CONTROLLER = "JUSTSY-VNC-CONTROLLER".getBytes();
    private static final byte[] PREFIX_CONTROLLED = "JUSTSY-VNC-CONTROLLED".getBytes();

    public static VncConnection parseHead(Connection connection, ByteBuffer head) {
        VncConnection vncConnection = null;
        do {
            if (null == head)
                break;

            int totalLength = head.getInt();
            if (totalLength != head.limit() || totalLength < PREFIX_LENGTH + 4)
                break;

            byte[] prefix = new byte[PREFIX_LENGTH];
            head.get(prefix);

            if (Arrays.equals(prefix, PREFIX_CONTROLLED))
                vncConnection = new ControlledVncConnection();
            else if (Arrays.equals(prefix, PREFIX_CONTROLLER))
                vncConnection = new ControllerVncConnection();
            else
                break;
            vncConnection.connection = connection;

            while (head.position() < totalLength) {
                int keyLength = head.getInt();
                if (head.position() + keyLength > totalLength)
                    break;
                byte[] key = new byte[keyLength];
                head.get(key);

                int valueLength = head.getInt();
                if (head.position() + valueLength > totalLength)
                    break;
                byte[] value = new byte[valueLength];
                head.get(value);

                vncConnection.set(key, value);
            }

            if (head.position() != totalLength)
                vncConnection = null;
        } while (false);
        return vncConnection;
    }

    protected void set(byte[] key, byte[] value) {
        if (Arrays.equals(key, KEY_MAGIC))
            this.magic = new String(value);
    }

    private static final byte[] KEY_MAGIC = "MAGIC".getBytes();

    private String magic;

    public String getMagic() {
        return magic;
    }

    private final Queue<ByteBuffer> dataQueue = new LinkedList<ByteBuffer>();

    public void offer(ByteBuffer byteBuffer) {
        synchronized (this) {
            dataQueue.offer(byteBuffer);
        }
    }

    public ByteBuffer poll() {
        synchronized (this) {
            return dataQueue.poll();
        }
    }
}
