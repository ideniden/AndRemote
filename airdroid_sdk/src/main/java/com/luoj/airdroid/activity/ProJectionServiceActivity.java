package com.luoj.airdroid.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.luoj.airdroid.service.ProjectionService;

public abstract class ProJectionServiceActivity extends ProjectionActivity implements ProjectionService.FrameHandler {

    protected ProjectionService projectionService;
    protected ServiceConnection serviceConnection;

    @Override
    protected void mediaProjectionEnabled(final int resultCode, final Intent data) {
        if (null == projectionService) {
            bindService(new Intent(this, ProjectionService.class), serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    projectionService = ((ProjectionService.MyBinder) service).getInstance();
//                    CodecParam.setOritationLandscape();
                    projectionService.setFrameHandler(ProJectionServiceActivity.this);
                    onProjectionServiceEnabled(projectionService, resultCode, data);
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

    protected abstract void onProjectionServiceEnabled(ProjectionService projectionService, int resultCode, Intent data);

    protected void stopEncode(){
        if (null != projectionService) {
            projectionService.stopEncode();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopEncode();

        if (null != serviceConnection) {
            unbindService(serviceConnection);
            projectionService = null;
        }
    }

}
