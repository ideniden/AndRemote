package com.luoj.airdroid.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.Surface;

import com.elvishew.xlog.XLog;
import com.luoj.airdroid.CodecParam;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ProjectionService extends Service {

    IBinder mBinder = new MyBinder();

    public class MyBinder extends Binder {
        public ProjectionService getInstance() {
            return ProjectionService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    protected MediaProjectionManager mediaProjectionManager;
    protected MediaProjection mediaProjection;
    protected VirtualDisplay virtualDisplay;

    protected MediaCodec encoder;

    public void start(Surface surface, int resultCode, Intent data) {
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        mediaProjection.registerCallback(mediaProjectionCallback, null);
        virtualDisplay = createVirtualDisplay(surface);
    }

    public void startEncode(int resultCode, Intent data) throws IOException {
        start(createEncoder(), resultCode, data);
        encodeThread = new Thread(encodeRunable);
        encodeThread.start();
    }

    public void stopEncode() {
        logd("encode stoped.");
        stopMirror();
        if (null != encodeThread) {
//            encodeThread.interrupt();
            encodeThread = null;
        }
        if (null != encoder) {
            encoder.signalEndOfInputStream();
            encoder.stop();
            encoder.release();
            encoder = null;
        }
    }

    public void setFrameHandler(FrameHandler frameHandler) {
        this.frameHandler = frameHandler;
    }

    protected FrameHandler frameHandler;
    protected Thread encodeThread;
    protected Runnable encodeRunable = new Runnable() {
        int count;
//        final int threshold = 10;
//        byte[] sps = null;

        @Override
        public void run() {
            logd("encode started.");
            encoder.start();

            ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();

            boolean encoderDone = false;

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            while (!encoderDone) {
                int encoderStatus;
                try {
                    encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, CodecParam.TIMEOUT_USEC);
                } catch (IllegalStateException e) {
//                    e.printStackTrace();
                    break;
                }

                if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    encoderOutputBuffers = encoder.getOutputBuffers();
                    logd("encoder output buffers changed");
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newFormat = encoder.getOutputFormat();
                    logd("encoder output format changed: " + newFormat);
                } else if (encoderStatus < 0) {
                    break;
                } else {
                    ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                    if (encodedData == null) {
                        logd("============It's NULL. BREAK!=============");
                        return;
                    }
                    if (bufferInfo.size != 0) {
                        encodedData.position(bufferInfo.offset);
                        encodedData.limit(bufferInfo.offset + bufferInfo.size);
                    }
//                    logd("encode frame,buf->" + encodedData);
                    if (null != frameHandler) {
                        byte[] data = new byte[bufferInfo.size];
                        try {
                            encodedData.get(data);
                            if (data[4] == 0x67) {
                                logd("found sps in encoding. find time->" + count + " , length->" + bufferInfo.size);
                                logd("sps:" + Arrays.toString(data));
//                                sps = new byte[bufferInfo.size];
//                                System.arraycopy(data, 0, sps, 0, sps.length);
                            } else if (data[4] == 0x68) {
                                logd("found pps in encoding." + count);
                            } else if (data[4] == 0x65 || data[4] == 0x25) {
                                logd("found i_frame in encoding." + count);
//                                if (null != sps && sps.length > 0) {
//                                    frameHandler.handle(sps, sps.length);
//                                    logd("append sps at i_frame");
//                                }
                            }
//                            if (null == sps && count > threshold) {
//                                sps = new byte[]{0, 0, 0, 1, 103, 100, 64, 41, -84, 44, -88, 5, 0, 91, -112};
//                            }

//                            if (null != sps && sps.length > 0 && (count % (threshold * 10) == 0)) {
//                                frameHandler.handle(sps, sps.length);
//                                logd("append sps at almost.");
//                            }

//                            byte[] b = new byte[10];
//                            System.arraycopy(data, 0, b, 0, b.length);
//                            logd("data:" + Arrays.toString(b));

                            frameHandler.handle(data, bufferInfo.size);
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
//                    videoWindow.setData(CodecUtils.clone(encodedData), info);

                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        logd("config flag received");
                    }

                    encoderDone = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;

                    try {
                        encoder.releaseOutputBuffer(encoderStatus, false);
                    } catch (IllegalStateException e) {
//                        e.printStackTrace();
                    }
                }
                if (count >= 65535) {
                    count = 0;
                }
                count += 1;
            }
        }
    };

    ByteBuffer cloneBuf(ByteBuffer original) {
        ByteBuffer clone = ByteBuffer.allocate(original.capacity());
        original.rewind();//copy from the beginning
        clone.put(original);
        original.rewind();
        clone.flip();
        return clone;
    }

    public interface FrameHandler {
        void handle(byte[] data, int length);
    }

    public void stopMirror() {
        if (null != virtualDisplay) {
            virtualDisplay.release();
        }
        if (null != mediaProjection) {
            mediaProjection.stop();
        }
    }

    protected VirtualDisplay createVirtualDisplay(Surface surface) {
        return mediaProjection.createVirtualDisplay("AirDroid",
                CodecParam.width,
                CodecParam.height,
                CodecParam.dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface,
                null,
                null);
    }

    protected MediaProjection.Callback mediaProjectionCallback = new MediaProjection.Callback() {
        @Override
        public void onStop() {
            logd("MediaProjection stoped.");
        }
    };

    protected Surface createEncoder() throws IOException {
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(CodecParam.mime, CodecParam.width, CodecParam.height);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, CodecParam.bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, CodecParam.framerate);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, CodecParam.i_frame_interval);

//        String jfEndoerName = findJfEncoder();
        String jfEndoerName = null;
        if (!TextUtils.isEmpty(jfEndoerName)) {
            encoder = MediaCodec.createByCodecName(jfEndoerName);
        } else {
            encoder = MediaCodec.createEncoderByType(CodecParam.mime);
        }
        logd("create encoder -> " + encoder.getName());

        encoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        return encoder.createInputSurface();
    }

//    private void updateNotification(String message) {
//        Intent intent = new Intent(this, ProjectionService.class);
//        intent.setAction("STOP");
//        PendingIntent stopServiceIntent = PendingIntent.getService(this, 0, intent, 0);
//        Notification.Builder mBuilder =
//                new Notification.Builder(this)
//                        .setSmallIcon(android.R.drawable.ic_media_play)
//                        .setOngoing(true)
//                        .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopServiceIntent)
//                        .setContentTitle(message)
//                        .setContentText(":");
//        startForeground(6000, mBuilder.build());
//    }

    public String findJfEncoder() {
        String encoderName = null;
        int codecCount = MediaCodecList.getCodecCount();
        for (int i = 0; i < codecCount; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (TextUtils.equals("OMX.jf.h264.encoder", codecInfo.getName())) {
                encoderName = codecInfo.getName();
            }
        }
        return encoderName;
    }

    protected void logd(String content) {
        XLog.d("[" + this.getClass().getSimpleName() + "] " + content);
    }


}
