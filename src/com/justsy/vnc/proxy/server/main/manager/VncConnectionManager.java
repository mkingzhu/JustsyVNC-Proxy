package com.justsy.vnc.proxy.server.main.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.justsy.vnc.proxy.server.main.connection.ControllerVncConnection;
import com.justsy.vnc.proxy.server.main.connection.VncConnection;
import com.justsy.vnc.proxy.server.net.connection.Connection;

public class VncConnectionManager {
    private static final long                 TIME_INTERVAL = 60000;
    private static final VncConnectionManager INSTANCE      = new VncConnectionManager();

    private final ReentrantReadWriteLock lock      = new ReentrantReadWriteLock();
    private final WriteLock              writeLock = lock.writeLock();
    private final ReadLock               readLock  = lock.readLock();

    private final Map<Connection, VncConnection>       vncConnectionMap        = new HashMap<Connection, VncConnection>();
    private final Map<String, ControllerVncConnection> controllerConnectionMap = new HashMap<String, ControllerVncConnection>();
    private final Map<VncConnection, VncConnection>    pairedConnectionMap     = new HashMap<VncConnection, VncConnection>();

    public static synchronized VncConnectionManager getInstance() {
        return INSTANCE;
    }

    public boolean containsKey(Connection connection) {
        readLock.lock();
        try {
            return vncConnectionMap.containsKey(connection);
        } finally {
            readLock.unlock();
        }
    }

    public void put(Connection connection, VncConnection vncConnection) {
        writeLock.lock();
        try {
            vncConnectionMap.put(connection, vncConnection);
        } finally {
            writeLock.unlock();
        }
    }

    public VncConnection get(Connection connection) {
        readLock.lock();
        try {
            return vncConnectionMap.get(connection);
        } finally {
            readLock.unlock();
        }
    }

    public void addControllerVncConnection(ControllerVncConnection controllerVncConnection) {
        writeLock.lock();
        try {
            controllerConnectionMap.put(controllerVncConnection.getMagic(), controllerVncConnection);
        } finally {
            writeLock.unlock();
        }
    }

    public ControllerVncConnection removeControllerVncConnection(String magic) {
        writeLock.lock();
        try {
            return controllerConnectionMap.remove(magic);
        } finally {
            writeLock.unlock();
        }
    }

    public void addPairedConnection(VncConnection vncConnection1, VncConnection vncConnection2) {
        writeLock.lock();
        try {
            pairedConnectionMap.put(vncConnection1, vncConnection2);
            pairedConnectionMap.put(vncConnection2, vncConnection1);
        } finally {
            writeLock.unlock();
        }
    }

    public VncConnection getPairedVncConnection(VncConnection vncConnection) {
        readLock.lock();
        try {
            return pairedConnectionMap.get(vncConnection);
        } finally {
            readLock.unlock();
        }
    }

    public void close(Connection connection) {
        VncConnection vncConnection = get(connection);
        if (null != vncConnection) {
            VncConnection pairedVncConnection = getPairedVncConnection(vncConnection);
            if (null != pairedVncConnection)
                close_(pairedVncConnection.getConnection());
        }

        close_(connection);
    }

    private void close_(Connection connection) {
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        clear(connection);
    }

    private void clear(Connection connection) {
        writeLock.lock();
        try {
            VncConnection vncConnection = vncConnectionMap.remove(connection);
            if (null != vncConnection) {
                if (vncConnection instanceof ControllerVncConnection)
                    controllerConnectionMap.remove(vncConnection.getMagic());
                pairedConnectionMap.remove(vncConnection);
            }
        } finally {
            writeLock.unlock();
        }
    }

    private VncConnectionManager() {
        startTimer();
    }

    private void startTimer() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                final long now = System.currentTimeMillis();
                writeLock.lock();
                try {
                    for (Connection connection : vncConnectionMap.keySet()) {
                        VncConnection vncConnection = vncConnectionMap.get(connection);
                        if (null == vncConnection)
                            continue;

                        long lastActiveTime = vncConnection.getLastActiveTime();
                        if (0 != lastActiveTime && now - lastActiveTime > TIME_INTERVAL)
                            close(connection);
                    }
                } finally {
                    writeLock.unlock();
                }
            }
        }, TIME_INTERVAL, TIME_INTERVAL);
    }
}
