package com.justsy.vnc.proxy.server.main.connection;

import java.util.Arrays;

import com.justsy.vnc.proxy.server.main.util.StringUtil;

public class ControllerVncConnection extends VncConnection {
    ControllerVncConnection() {
    }

    @Override
    public boolean isValid() {
        return StringUtil.isNotEmpty(getMagic())
                && StringUtil.isNotEmpty(user)
                && StringUtil.isNotEmpty(deviceId)
                && null != needConfirm;
    }

    @Override
    protected void set(byte[] key, byte[] value) {
        super.set(key, value);

        if (Arrays.equals(key, KEY_USER))
            this.user = new String(value);
        else if (Arrays.equals(key, KEY_DEVICE_ID))
            this.deviceId = new String(value);
        else if (Arrays.equals(key, KEY_NEED_CONFIRM))
            this.needConfirm = new String(value).equals("1");
    }

    private static final byte[] KEY_USER         = "USER".getBytes();
    private static final byte[] KEY_DEVICE_ID    = "DEVICE-ID".getBytes();
    private static final byte[] KEY_NEED_CONFIRM = "NEED-CONFIRM".getBytes();

    private String  user;
    private String  deviceId;
    private Boolean needConfirm;

    public String getUser() {
        return user;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Boolean getNeedConfirm() {
        return needConfirm;
    }
}
