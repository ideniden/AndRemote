package com.luoj.airdroid.activity;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.view.SurfaceView;

import com.luoj.airdroid.R;


public class TestActivityPreview extends ProjectionActivity {

    SurfaceView surfaceView;

    MediaProjectionManager mediaProjectionManager;
    MediaProjection mediaProjection;
    VirtualDisplay virtualDisplay;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_test_media_injection);
        surfaceView = (SurfaceView)findViewById(R.id.sv);
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    @Override
    protected void mediaProjectionEnabled(int resultCode, Intent data) {
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        mediaProjection.registerCallback(new MediaProjectionCallback(), null);
        virtualDisplay = createVirtualDisplay();
    }

    private VirtualDisplay createVirtualDisplay() {
        int width = 1080;
        int height = 1920;
        int dpi = 1;
        return mediaProjection.createVirtualDisplay("AirDroid",
                width,
                height,
                dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surfaceView.getHolder().getSurface(),
                null,
                null);
    }

    class MediaProjectionCallback extends MediaProjection.Callback {

    }

}
