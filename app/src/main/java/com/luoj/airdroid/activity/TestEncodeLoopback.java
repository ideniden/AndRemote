package com.luoj.airdroid.activity;

import android.content.Intent;
import android.view.SurfaceView;

import com.luoj.airdroid.R;
import com.luoj.airdroid.decoder.VideoDecoder2;
import com.luoj.airdroid.service.ProjectionService;

import java.io.IOException;

public class TestEncodeLoopback extends ProJectionServiceActivity {

    SurfaceView surfaceView;
    VideoDecoder2 videoDecoder2;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_test_media_injection);
        surfaceView = (SurfaceView) findViewById(R.id.sv);
        videoDecoder2 = new VideoDecoder2(surfaceView.getHolder().getSurface());
        surfaceView.getHolder().addCallback(videoDecoder2);
    }

    @Override
    protected void onProjectionServiceEnabled(ProjectionService projectionService, int resultCode, Intent data) {
        try {
            projectionService.startEncode(resultCode, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle(byte[] data, int length) {
        videoDecoder2.setVideoData(data);
    }

}
