package com.luoj.airdroid.decoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.elvishew.xlog.XLog;
import com.luoj.airdroid.CodecParam;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoDecoder implements SurfaceHolder.Callback {

    MediaCodec decoder;

    Surface surface;

    public VideoDecoder(SurfaceHolder surfaceHolder) {
        surface = surfaceHolder.getSurface();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            decoder = MediaCodec.createDecoderByType(CodecParam.mime);
            MediaFormat format = MediaFormat.createVideoFormat(CodecParam.mime, CodecParam.width, CodecParam.height);
            format.setInteger(MediaFormat.KEY_BIT_RATE, CodecParam.bitrate);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, CodecParam.framerate);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, CodecParam.i_frame_interval);

            byte[] header_sps = {0, 0, 0, 1, 103, 66, 0, 41, -115, -115, 64, 80, 30, -48, 15, 8, -124, 83, -128};
            byte[] header_pps = {0, 0, 0, 1, 104, -54, 67, -56};

//            format.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
//            format.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));

            decoder.configure(format, holder.getSurface(), null, 0);
            decoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (null != decoder) {
//            decoder.signalEndOfInputStream();
            decoder.stop();
            decoder.release();
        }
    }

    public void putData(byte[] buf, int length) {
        if (null != decoder) {
            logd("start decode");
            ByteBuffer[] inputBuffers = decoder.getInputBuffers();
            int inputBuffersIndex = 0;
            try {
                inputBuffersIndex = decoder.dequeueInputBuffer(0);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            if (inputBuffersIndex >= 0) {
                ByteBuffer inputBuf = inputBuffers[inputBuffersIndex];
                inputBuf.clear();
                try {
                    inputBuf.put(buf, 0, length);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                try {
                    decoder.queueInputBuffer(inputBuffersIndex, 0, length, 0, 0);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int outputBufferIndex = 0;
            try {
                outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            while (outputBufferIndex >= 0) {
                try {
                    decoder.releaseOutputBuffer(outputBufferIndex, true);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                try {
                    outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void logd(String content) {
        XLog.d("[VideoDecoder]" + content);
    }

}
