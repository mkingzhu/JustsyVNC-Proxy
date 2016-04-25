package com.justsy.vnc.proxy.server.main.handler;

import java.nio.ByteBuffer;

import com.justsy.vnc.proxy.server.main.connection.ControlledVncConnection;
import com.justsy.vnc.proxy.server.main.connection.ControllerVncConnection;
import com.justsy.vnc.proxy.server.main.connection.VncConnection;
import com.justsy.vnc.proxy.server.main.manager.VncConnectionManager;
import com.justsy.vnc.proxy.server.main.util.PushUtil;
import com.justsy.vnc.proxy.server.net.connection.Connection;
import com.justsy.vnc.proxy.server.net.handler.AbstractHandler;

public class VncConnectionHandler extends AbstractHandler {
    private final VncConnectionManager vncConnectionManager = VncConnectionManager.getInstance();

    private final String ip;

    public VncConnectionHandler(String ip) {
        this.ip = ip;
    }

    @Override
    public void onException(Connection connection, Exception e)
            throws Exception {
        e.printStackTrace();
    }

    @Override
    public void onClose(Connection connection)
            throws Exception {
        vncConnectionManager.close(connection);
    }

    @Override
    public void onRead(Connection connection, ByteBuffer data)
            throws Exception {
        boolean isOK = true;
        try {
            if (!vncConnectionManager.containsKey(connection))
                isOK = onReadNewConnection(connection, data);
            else
                isOK = onReadExistConnection(connection, data);
        } catch (Exception e) {
            isOK = false;

            e.printStackTrace();
        } finally {
            if (!isOK)
                vncConnectionManager.close(connection);
        }
    }

    private boolean onReadNewConnection(Connection connection, ByteBuffer data) {
        VncConnection vncConnection = VncConnection.parseHead(connection, data);
        if (null == vncConnection || !vncConnection.isValid())
            return false;

        vncConnectionManager.put(connection, vncConnection);
        if (vncConnection instanceof ControllerVncConnection)
            return onReadNewControllerConnection((ControllerVncConnection) vncConnection);
        else
            return onReadNewControlledConnection((ControlledVncConnection) vncConnection);
    }

    private boolean onReadNewControllerConnection(ControllerVncConnection controllerVncConnection) {
        vncConnectionManager.addControllerVncConnection(controllerVncConnection);
        responseAndSetWritable(controllerVncConnection, VncConnection.STATUS_ESTABLISHING, VncConnection.RESPONSE_OK);

        PushUtil.push(
                ip,
                controllerVncConnection.getUser(),
                controllerVncConnection.getDeviceId(),
                controllerVncConnection.getNeedConfirm(),
                controllerVncConnection.getMagic());
        return true;
    }

    private boolean onReadNewControlledConnection(ControlledVncConnection controlledVncConnection) {
        ControllerVncConnection controllerVncConnection = vncConnectionManager.removeControllerVncConnection(controlledVncConnection.getMagic());
        if (null == controllerVncConnection || VncConnection.STATUS_ESTABLISHED != controllerVncConnection.getStatus())
            return false;

        if (controlledVncConnection.getConfirmed()) {
            vncConnectionManager.addPairedConnection(controllerVncConnection, controlledVncConnection);

            responseAndSetWritable(controllerVncConnection, VncConnection.STATUS_PAIRING, VncConnection.RESPONSE_OK);
            responseAndSetWritable(controlledVncConnection, VncConnection.STATUS_PAIRING, VncConnection.RESPONSE_OK);
        } else {
            responseAndSetWritable(controllerVncConnection, VncConnection.STATUS_NOT_CONFIRM, VncConnection.RESPONSE_NOT_CONFIRM);
            responseAndSetWritable(controlledVncConnection, VncConnection.STATUS_NOT_CONFIRM, VncConnection.RESPONSE_OK);
        }

        return true;
    }

    private boolean onReadExistConnection(Connection connection, ByteBuffer data) {
        VncConnection vncConnection = vncConnectionManager.get(connection);
        if (null == vncConnection || VncConnection.STATUS_PAIRED != vncConnection.getStatus())
            return false;

        VncConnection pairedVncConnection = vncConnectionManager.getPairedVncConnection(vncConnection);
        if (null == pairedVncConnection)
            return false;

        offerAndSetWritable(pairedVncConnection, data);
        setReadable(vncConnection);

        return true;
    }

    @Override
    public void onWrite(Connection connection)
            throws Exception {
        boolean isOK = true;
        try {
            VncConnection vncConnection = vncConnectionManager.get(connection);

            ByteBuffer data;
            while (null != (data = vncConnection.poll()))
                connection.write(data);

            setReadable(vncConnection);

            switch (vncConnection.getStatus()) {
                case VncConnection.STATUS_ESTABLISHING:
                    vncConnection.setStatus(VncConnection.STATUS_ESTABLISHED);
                    break;
                case VncConnection.STATUS_PAIRING:
                    vncConnection.setStatus(VncConnection.STATUS_PAIRED);
                    break;
                case VncConnection.RESPONSE_NOT_CONFIRM:
                    vncConnectionManager.close(connection);
                    break;
            }
        } catch (Exception e) {
            isOK = false;

            e.printStackTrace();
        } finally {
            if (!isOK)
                vncConnectionManager.close(connection);
        }
    }

    private void setReadable(VncConnection vncConnection) {
        vncConnection.setReadable();
        vncConnection.setLastActiveTime(System.currentTimeMillis());
    }

    private void responseAndSetWritable(VncConnection vncConnection, int status, int responseCode) {
        vncConnection.setStatus(status);
        offerAndSetWritable(vncConnection, genResponse(responseCode));
    }

    private void offerAndSetWritable(VncConnection vncConnection, ByteBuffer data) {
        vncConnection.offer(data);
        vncConnection.setWritable();
        vncConnection.setLastActiveTime(System.currentTimeMillis());
    }

    private ByteBuffer genResponse(int responseCode) {
        ByteBuffer data = ByteBuffer.allocate(8);
        data.putInt(8);
        data.putInt(responseCode);
        data.flip();
        return data;
    }
}
