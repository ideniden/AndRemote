package com.luoj.airdroid.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.luoj.airdroid.service.RTPProjectionService;
import com.luoj.airdroid.service.RemoteService;

public abstract class RTPProjectionServiceActivity extends ProjectionActivity {

    protected RTPProjectionService rtpProjectionService;
    protected ServiceConnection serviceConnection;

    protected int resultCode = RESULT_OK;
    protected Intent data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void mediaProjectionEnabled(final int resultCode, final Intent data) {
        this.resultCode = resultCode;
        this.data = data;

        if (null == rtpProjectionService) {
            bindService(new Intent(this, RTPProjectionService.class), serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    rtpProjectionService = ((RTPProjectionService.MyBinder) service).getInstance();
                    onProjectionServiceEnabled(rtpProjectionService, resultCode, data);
//                    try {
//                        projectionService.startEncode(resultCode, data);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            }, Context.BIND_AUTO_CREATE);
        }
    }

    protected abstract void onProjectionServiceEnabled(RTPProjectionService rtpProjectionService, int resultCode, Intent data);

    protected boolean start(String ip) {
        if (null != rtpProjectionService && null != data) {
            startService(new Intent(this, RemoteService.class));
            startService(new Intent(this, RTPProjectionService.class));
            rtpProjectionService.start(ip, resultCode, data);
            return true;
        }
        return false;
    }

    protected void stop() {
        if (null != rtpProjectionService) {
            rtpProjectionService.stop();
        }
        stopService(new Intent(this, RemoteService.class));
        stopService(new Intent(this, RTPProjectionService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != serviceConnection) {
            unbindService(serviceConnection);
            rtpProjectionService = null;
        }
    }

}
