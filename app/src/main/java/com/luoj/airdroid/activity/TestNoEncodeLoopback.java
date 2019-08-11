package com.luoj.airdroid.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.SurfaceView;

import com.luoj.airdroid.R;
import com.luoj.airdroid.service.ProjectionService;

public class TestNoEncodeLoopback extends ProjectionActivity {

    SurfaceView surfaceView;

    ProjectionService projectionService;
    ServiceConnection serviceConnection;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_test_media_injection);
        surfaceView = (SurfaceView)findViewById(R.id.sv);
    }

    @Override
    protected void mediaProjectionEnabled(final int resultCode, final Intent data) {
        if (null == projectionService) {
            bindService(new Intent(this, ProjectionService.class), serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    projectionService = ((ProjectionService.MyBinder) service).getInstance();
                    projectionService.start(surfaceView.getHolder().getSurface(), resultCode, data);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            }, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != projectionService) {
            projectionService.stopEncode();
        }

        if (null != serviceConnection) {
            unbindService(serviceConnection);
            projectionService = null;
        }
    }

}
