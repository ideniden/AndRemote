package com.luoj.airdroid.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.luoj.airdroid.R;
import com.luoj.airdroid.service.ScreenShareService;

public class TestAcquireScreenFrameActivity extends BaseActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 11;
    private static final int REQUEST_CODE_MEDIA_PROJECTION = 12;
    SurfaceView surfaceView;
    ImageView ivPreview;
    TextView tvData, tvFps;
    Button btnShare;

    boolean isProjectionRunning = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_media_injection);

        surfaceView = (SurfaceView) findViewById(R.id.sv);
        ivPreview = (ImageView) findViewById(R.id.iv_preview);
        tvData = (TextView) findViewById(R.id.tv_data);
        tvFps = (TextView) findViewById(R.id.tv_fps);

        btnShare = (Button) findViewById(R.id.btn_share);

        btnShare.setOnClickListener(v -> {
            if (!hasNotificationPermission()) {
                toast("屏幕共享需要访问您的通知栏以便查看状态！");
                requestNotificationPermission();
                return;
            }
            if (isProjectionRunning) {
                stopProjection();
            } else {
                requestProjection();
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ScreenShareService.ACTION_STARTED);
        intentFilter.addAction(ScreenShareService.ACTION_RUNNING);
        intentFilter.addAction(ScreenShareService.ACTION_STOPPED);
        registerReceiver(fpsReceiver, intentFilter);
    }

    BroadcastReceiver fpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ScreenShareService.ACTION_STARTED.equals(intent.getAction())) {
                isProjectionRunning = true;
                btnShare.setText("停止");
            } else if (ScreenShareService.ACTION_RUNNING.equals(intent.getAction())) {
                int fps = intent.getIntExtra("fps", 0);
                tvFps.setText(String.valueOf(fps));
            } else if (ScreenShareService.ACTION_STOPPED.equals(intent.getAction())) {
                isProjectionRunning = false;
                btnShare.setText("开始");
            }
        }
    };

    public void requestProjection() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE_MEDIA_PROJECTION);
    }

    public void startProjection(Intent data) {
        Intent intent = new Intent(this, ScreenShareService.class);
        intent.putExtras(data);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    public void stopProjection() {
        Intent intent = new Intent(this, ScreenShareService.class);
        intent.setAction(ScreenShareService.ACTION_COMMAND_STOP);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (hasNotificationPermission()) {
                toast("授权成功！");
                requestProjection();
            } else {
                toast("还是没有权限");
                //启用免打扰权限（没用）
//                NotificationManager notificationManager = getSystemService(NotificationManager.class);
//                if (!notificationManager.isNotificationPolicyAccessGranted()) {
//                    Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
//                    startActivity(intent);
//                }
            }
        } else if (requestCode == REQUEST_CODE_MEDIA_PROJECTION) {
            if (resultCode != RESULT_OK) {
                toast("您取消了共享");
            } else {
                startProjection(data);
            }
        }
    }

    private void requestNotificationPermission() {
        requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
    }

    private boolean hasNotificationPermission() {
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        return manager.areNotificationsEnabled();
    }

}
