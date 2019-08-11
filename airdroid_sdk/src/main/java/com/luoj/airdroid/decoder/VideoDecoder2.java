package com.luoj.airdroid.decoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.elvishew.xlog.XLog;
import com.luoj.airdroid.CodecParam;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class VideoDecoder2 implements SurfaceHolder.Callback {

    //处理音视频的编解码的类MediaCodec
    private MediaCodec video_decoder;
    //显示画面的Surface
    private Surface surface;
    // 0: live, 1: playback, 2: local file
    private int state = 0;
    //视频数据
    private BlockingQueue<byte[]> video_data_Queue = new ArrayBlockingQueue<byte[]>(10000);
    //音频数据
    private BlockingQueue<byte[]> audio_data_Queue = new ArrayBlockingQueue<byte[]>(10000);

    private boolean isReady = false;
    private int fps = 0;

    private ByteBuffer[] inputBuffers;
    private ByteBuffer[] outputBuffers;
    private MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
    private int frameCount = 0;
    private long deltaTime = 0;
    private long counterTime = System.currentTimeMillis();
    private boolean isRuning = false;
    private boolean isInitCodec = false;
//    private boolean withoutSPS = false;//OMX.rk.video_decoder.avc

    public VideoDecoder2(Surface surface) {
        this.surface = surface;
//        isSupportSPS();
    }

    public VideoDecoder2(Surface surface, int playerState) {
        this.surface = surface;
        this.state = playerState;
//        isSupportSPS();
    }

//    private void isSupportSPS() {
//        try {
//            video_decoder = MediaCodec.createDecoderByType(CodecParam.mime);
//            this.withoutSPS = (null != video_decoder ? TextUtils.equals(video_decoder.getName(), "OMX.rk.video_decoder.avc") : false);
//            logd("decode without sps -> " + withoutSPS);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    public void stopRunning() {
        video_data_Queue.clear();
        audio_data_Queue.clear();
    }

    //添加视频数据
    public void setVideoData(byte[] data) {
        //0x67是SPS的NAL头，0x68是PPS的NAL头
//        if (!withoutSPS) {
            if (data[4] == 0x67 && !isInitCodec) {
                logd("found SPS." + data.length);
                byte[] tmp = new byte[15];
                //把video中索引0开始的15个数字复制到tmp中索引为0的位置上
                System.arraycopy(data, 0, tmp, 0, 15);
                try {
                    initial(tmp);
                } catch (Exception e) {
                    return;
                }
            } else if (data[4] == 0x68) {
                logd("found PPS.");
                return;
            }
//        } else {
//            if (!isInitCodec) {
//                try {
//                    MediaFormat format = MediaFormat.createVideoFormat(CodecParam.mime, CodecParam.width, CodecParam.height);
////                    format.setByteBuffer("csd-0", ByteBuffer.wrap(data));
//                    byte[] header_sps = {0, 0, 0, 1, 103, 100, 64, 41, -84, 44, -88, 5, 0, 91, -112};
//                    byte[] header_pps = {0, 0, 0, 1, 104, -18, 56, -128};
//                    format.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
//                    format.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
//                    start(format);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        try {
            video_data_Queue.put(data);
//            logd("put data ok.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //添加音频数据
    public void setAudioData(byte[] data) {
        try {
            audio_data_Queue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public int getFPS() {
        return fps;
    }


    public void initial(byte[] sps) throws IOException {
        MediaFormat format = null;
        boolean isVGA = true;
        //使用sps数据格式判断是否是VGA
        byte[] video_sps = {0, 0, 0, 1, 103, 100, 64, 41, -84, 44, -88, 10, 2, -1, -107};
        for (int i = 0; i < sps.length; i++) {
            if (video_sps[i] != sps[i]) {
                //判断是否是VGA视频传输标准
                isVGA = false;
                break;
            }
        }
        if (isVGA) {
            format = MediaFormat.createVideoFormat(CodecParam.mime, CodecParam.width, CodecParam.height);
            byte[] header_pps = {0, 0, 0, 1, 104, -18, 56, -128};
            format.setByteBuffer("csd-0", ByteBuffer.wrap(video_sps));
            format.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 640 * 360);
        } else {
            format = MediaFormat.createVideoFormat(CodecParam.mime, CodecParam.width, CodecParam.height);
            //                  [0, 0, 0, 1, 103, 66, -128, 31, -38, 1, 64, 22, -23, 72, 40, 48, 48, 54, -123, 9, -88, 0, 0, 0, 1, 104, -50, 6, -30] s8+
            //                  [0, 0, 0, 1, 103, 66, -64, 41, -115, 104, 5, 0, 91, 32, 30, 17, 8, -44, 0, 0, 0, 1, 104, -50, 1, -88, 53, -56]  emulator
            byte[] header_sps = {0, 0, 0, 1, 103, 100, 64, 41, -84, 44, -88, 5, 0, 91, -112};
            byte[] header_pps = {0, 0, 0, 1, 104, -18, 56, -128};
            format.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
            format.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
            //      format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1280 * 720);
        }
        logd("got SPS success. isVGA->" + isVGA);
        start(format);
    }

    public void start() throws IOException {
        start(MediaFormat.createVideoFormat(CodecParam.mime, CodecParam.width, CodecParam.height));
    }

    public void start(MediaFormat format) throws IOException {
        if (video_decoder != null) {
            try {
                video_decoder.stop();
                video_decoder.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            video_decoder = null;
        }

        video_decoder = MediaCodec.createDecoderByType(CodecParam.mime);

        if (video_decoder == null) {
            logd("current device not support " + CodecParam.mime + " codec.");
            return;
        }

        video_decoder.configure(format, surface, null, 0);
        video_decoder.start();
        inputBuffers = video_decoder.getInputBuffers();
        outputBuffers = video_decoder.getOutputBuffers();
        frameCount = 0;
        deltaTime = 0;
        isRuning = true;
        isInitCodec = true;
        runDecodeVideoThread();
        logd("start decode by " + video_decoder.getName());
    }

    /**
     * @description 解码视频流数据
     * @author ldm
     * @time 2016/12/20
     */
    Thread thread;

    private void runDecodeVideoThread() {
        thread = new Thread() {
            public void run() {

                while (isRuning && !Thread.interrupted()) {
//                    logd("decoding");
                    int inIndex = -1;
                    try {
                        inIndex = video_decoder.dequeueInputBuffer(-1);
                    } catch (Exception e) {
                        return;
                    }
                    try {

                        if (inIndex >= 0) {
                            ByteBuffer buffer = inputBuffers[inIndex];
                            buffer.clear();

                            if (!video_data_Queue.isEmpty()) {
                                byte[] data;
                                data = video_data_Queue.take();
                                buffer.put(data);
                                if (state == 0) {
                                    video_decoder.queueInputBuffer(inIndex, 0, data.length, 66, 0);
                                } else {
                                    video_decoder.queueInputBuffer(inIndex, 0, data.length, 33, 0);
                                }
                            } else {
                                if (state == 0) {
                                    video_decoder.queueInputBuffer(inIndex, 0, 0, 66, 0);
                                } else {
                                    video_decoder.queueInputBuffer(inIndex, 0, 0, 33, 0);
                                }
                            }
                        } else {
                            video_decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        }

                        int outIndex = video_decoder.dequeueOutputBuffer(info, 0);
                        switch (outIndex) {
                            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                                outputBuffers = video_decoder.getOutputBuffers();
                                break;
                            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                                isReady = true;
                                break;
                            case MediaCodec.INFO_TRY_AGAIN_LATER:
                                break;
                            default:

                                video_decoder.releaseOutputBuffer(outIndex, true);
                                frameCount++;
                                deltaTime = System.currentTimeMillis() - counterTime;
                                if (deltaTime > 1000) {
                                    fps = (int) (((float) frameCount / (float) deltaTime) * 1000);
                                    counterTime = System.currentTimeMillis();
                                    frameCount = 0;
                                }
                                break;
                        }

                        //所有流数据解码完成，可以进行关闭等操作
                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }
        };
        thread.start();
    }

    protected void logd(String content) {
        XLog.d("[" + this.getClass().getSimpleName() + "] " + content);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        MediaFormat format = MediaFormat.createVideoFormat(CodecParam.mime, CodecParam.width, CodecParam.height);
////                    format.setByteBuffer("csd-0", ByteBuffer.wrap(data));
//        byte[] header_sps = {0, 0, 0, 1, 103, 100, 64, 41, -84, 44, -88, 5, 0, 91, -112};
//        byte[] header_pps = {0, 0, 0, 1, 104, -18, 56, -128};
//        format.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
//        format.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
        try {
            start(format);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRuning = false;
        if (null != thread) {
            thread.interrupt();
        }
        if (video_decoder != null) {
            video_decoder.stop();
            video_decoder.release();
            video_decoder = null;
        }
        logd("on decode stop.");
    }

}
