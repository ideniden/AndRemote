package com.luoj.airdroid;

import android.content.Context;
import android.content.Intent;

import com.luoj.airdroid.service.RTPProjectionService;
import com.luoj.airdroid.service.RemoteService;

public class AirDroid {

    public static void init(Context context) {
        context.startService(new Intent(context, RemoteService.class));
        context.startService(new Intent(context, RTPProjectionService.class));
    }

    public static String getBeginOrder() {
        return RemoteService.SOCKET_ORDER_BEGIN + ";" + CodecParam.width + ";" + CodecParam.height + ";" + CodecParam.framerate + ";" + CodecParam.bitrate;
    }

}
