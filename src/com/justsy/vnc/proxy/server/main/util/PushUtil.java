package com.justsy.vnc.proxy.server.main.util;

import com.justsy.zeus.api.DefaultZeusClient;
import com.justsy.zeus.api.internal.ApiException;
import com.justsy.zeus.api.request.TelecontrolRequest;
import com.justsy.zeus.api.response.TelecontrolResponse;

public class PushUtil {
    private static final String URL    = "http://192.168.2.12/zeus/rest";
    private static final String APPKEY = "ccb_app_01";
    private static final String SECRET = "3e2d4528127e7942bc4b87aaf643766b";

    public static void push(String serverIp, String manager, String deviceId, boolean needConfirm, String magic) {
        DefaultZeusClient dzc = new DefaultZeusClient(URL, APPKEY, SECRET);

        TelecontrolRequest alg = new TelecontrolRequest();
        alg.setServerUrl(serverIp);
        alg.setManager(manager);
        alg.setDeviceId(deviceId);
        alg.setIsNeedConform(needConfirm ? "Y" : "N");
        alg.setMagic(magic);
        alg.setTimestamp(System.currentTimeMillis());

        TelecontrolResponse appRsp;
        try {
            appRsp = dzc.execute(alg);

            if (appRsp.isSuccess()) {
                // 执行成功
                // 打印API请求结果
                System.out.println("result:" + appRsp.getBody());
            } else {
                // API请求失败
                // 打印业务错误信息
                System.out.println("error code:" + appRsp.getErrCode());
                System.out.println("error msg:" + appRsp.getMsg());
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }
}
