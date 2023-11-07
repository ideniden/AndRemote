package com.luoj.airdroid.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.Surface;
import android.widget.Toast;

import com.elvishew.xlog.XLog;
import com.luoj.airdroid.R;

import java.nio.ByteBuffer;

public class ScreenShareService extends Service implements ImageReader.OnImageAvailableListener {
    public static final String ACTION_COMMAND_STOP = "STOP";
    public static final String ACTION_STARTED = "action.screen_share.started";
    public static final String ACTION_RUNNING = "action.screen_share.running";
    public static final String ACTION_STOPPED = "action.screen_share.stopped";
    MediaProjection mediaProjection;
    VirtualDisplay virtualDisplay;
    ImageReader imageReader;
    public static final String CHANNEL_ID = "ScreenShareChannel";
    public static final String CHANNEL_NAME = "ScreenShare";
    String virtualDisplayName = "AirDroid";
    int width = 1080, height = 1920, dip = 1;
    int count = 0, fps = 0;
    long lastTimeStamp = System.currentTimeMillis();

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
                logd("Notification channel created");
            } else {
                logd("NotificationManager not found");
            }
        } else {
            logd("Not necessary to create notification channel");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        logd("TestAcquireFrameService onCreate");
        createNotificationChannel();
    }

//    private void startForeground() {
//        // Before starting the service as foreground check that the app has the
//        // appropriate runtime permissions. In this case, verify that the user
//        // has granted the CAMERA permission.
//        int cameraPermission =
//                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
//        if (cameraPermission == PackageManager.PERMISSION_DENIED) {
//            // Without camera permissions the service cannot run in the
//            // foreground. Consider informing user or updating your app UI if
//            // visible.
//            stopSelf();
//            return;
//        }
//
//        try {
//            Notification notification =
//                    new NotificationCompat.Builder(this, CHANNEL_ID)
//                            // Create the notification to display while the service
//                            // is running
//                            .build();
//            int type = 0;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                type = ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION;
//            }
//            ServiceCompat.startForeground(
//                    /* service = */ this,
//                    /* id = */ 100, // Cannot be 0
//                    /* notification = */ notification,
//                    /* foregroundServiceType = */ type
//            );
//        } catch (Exception e) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
//                    e instanceof ForegroundServiceStartNotAllowedException
//            ) {
//                // App not in a valid state to start foreground service
//                // (e.g started from bg)
//            }
//            // ...
//        }
//    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 如果接收到停止服务的操作
        if (ACTION_COMMAND_STOP.equals(intent.getAction())) {
            stopForeground(true);
            stopSelfResult(startId);
        } else {
            Bundle extras = intent.getExtras();
            if (null == extras) {
                logd("启动了，但是没有参数，无法共享。");
                stopSelf();
                return START_NOT_STICKY;
            }
            if (null != virtualDisplay) {
                logd("已经启动，不能重复启动");
                stopSelf();
                return START_NOT_STICKY;
            }
            startForegroundService();
            startProjection(intent);
            notifyStarted();
        }

        return START_STICKY;
    }

    @SuppressLint("WrongConstant")
    void startForegroundService() {
        // 创建停止服务的PendingIntent
        Intent stopSelf = new Intent(this, ScreenShareService.class);
        stopSelf.setAction(ACTION_COMMAND_STOP);
        PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("屏幕共享中...")
                .setContentText("点击展开后以停止共享")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .addAction(R.drawable.ic_launcher_round, "停止共享", pStopSelf) // 添加停止按钮
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        } else {
            startForeground(1, notification);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        toast("屏幕共享服务已关闭");
        StopProjection();

        imageReader = null;
        virtualDisplay = null;
        mediaProjection = null;
    }

    @SuppressLint("WrongConstant")
    public void startProjection(Intent data) {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, data);
        mediaProjection.registerCallback(mediaProjectionCallback, null);
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        imageReader.setOnImageAvailableListener(this, null);
        Surface surface = imageReader.getSurface();
        virtualDisplay = createVirtualDisplay(surface);
    }

    public void StopProjection() {
        if (null != imageReader) imageReader.close();
        if (null != virtualDisplay) virtualDisplay.release();
        if (null != mediaProjection) mediaProjection.stop();
    }

    private VirtualDisplay createVirtualDisplay(Surface surface) {
        return mediaProjection.createVirtualDisplay(virtualDisplayName,
                width,
                height,
                dip,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface,
                null,
                null);
    }

    @Override
    public void onImageAvailable(ImageReader imageReader) {
        Image image = null;
        try {
            image = imageReader.acquireLatestImage();
            if (image != null) {
                // 这里处理原始图像帧数据
                Image.Plane[] planes = image.getPlanes();
                ByteBuffer buffer = planes[0].getBuffer();
                // 从buffer创建Bitmap
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * width;
                // 创建空的Bitmap
                Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                //
                calculateFPS();
                //
                notifyRunning();
            }
        } finally {
            if (image != null) {
                image.close();
            }
        }
    }

    private void calculateFPS() {
        fps++;
        long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp - lastTimeStamp > 1000) {
            fps = 0;
            lastTimeStamp = currentTimestamp;
        }
    }

    MediaProjection.Callback mediaProjectionCallback = new MediaProjection.Callback() {
        @Override
        public void onStop() {
            toast("共享停止了");
            notifyStopped();
        }
    };

    public void notifyStarted() {
        Intent intent = new Intent(ACTION_STARTED);
        sendBroadcast(intent);
    }

    public void notifyRunning() {
        long current = System.currentTimeMillis();
        if (current - lastTimeStamp > 1000) {
            Intent intent = new Intent(ACTION_RUNNING);
            intent.putExtra("fps", fps);
            sendBroadcast(intent);
            lastTimeStamp = current;
        }
    }

    public void notifyStopped() {
        Intent intent = new Intent(ACTION_STOPPED);
        sendBroadcast(intent);
    }

    public void toast(String msg) {
        Toast.makeText(ScreenShareService.this, msg, Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void logd(String content) {
        XLog.d(content);
    }

}
