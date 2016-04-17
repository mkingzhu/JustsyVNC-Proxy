package com.justsy.vnc.proxy.server.main;

import com.justsy.vnc.proxy.server.main.handler.VncConnectionHandler;
import com.justsy.vnc.proxy.server.net.handler.HandlerManager;
import com.justsy.vnc.proxy.server.net.selector.Server;

public class Main {
    public static void main(String[] args) {
        try {
            HandlerManager handlerManager = HandlerManager.getInstance();
            handlerManager.addHandler(new VncConnectionHandler());

            Server server = Server.getInstance();
            server.init(5900);
            new Thread(server).start();
        } catch (Exception ignore) {
            System.exit(-1);
        }
    }
}
