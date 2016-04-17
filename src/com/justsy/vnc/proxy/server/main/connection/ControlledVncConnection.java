package com.justsy.vnc.proxy.server.main.connection;

import com.justsy.vnc.proxy.server.main.util.StringUtil;

import java.util.Arrays;

public class ControlledVncConnection extends VncConnection {
    ControlledVncConnection() {
    }

    @Override
    public boolean isValid() {
        return StringUtil.isNotEmpty(getMagic())
                && null != confirmed;
    }

    @Override
    protected void set(byte[] key, byte[] value) {
        super.set(key, value);

        if (Arrays.equals(key, KEY_CONFIRMED))
            this.confirmed = new String(value).equals("1");
    }

    private static final byte[] KEY_CONFIRMED = "CONFIRMED".getBytes();

    private Boolean confirmed;

    public Boolean getConfirmed() {
        return confirmed;
    }
}
