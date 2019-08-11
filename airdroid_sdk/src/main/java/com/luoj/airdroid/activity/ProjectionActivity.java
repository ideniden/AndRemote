package com.luoj.airdroid.activity;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 *
 */
public abstract class ProjectionActivity extends BaseActivity {

    int REQUEST_CODE_MEDIA_PROJECTION = 1;
//    MediaProjectionManager mediaProjectionManager;
//    MediaProjection mediaProjection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE_MEDIA_PROJECTION);
    }

    protected abstract void initView();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode != REQUEST_CODE_MEDIA_PROJECTION) {
            logd("Unknown request code:" + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "user denied screen sharing permission.", Toast.LENGTH_SHORT).show();
            return;
        }
//        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        mediaProjectionEnabled(resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected abstract void mediaProjectionEnabled(int resultCode, Intent data);

}
