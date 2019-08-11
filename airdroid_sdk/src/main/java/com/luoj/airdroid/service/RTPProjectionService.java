package com.luoj.airdroid.service;

import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.luoj.airdroid.RTPParam;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

import jlibrtp.Participant;
import jlibrtp.RTPAppIntfWrapper;
import jlibrtp.RTPSessionWrapper;

public class RTPProjectionService extends ProjectionService implements ProjectionService.FrameHandler {

    IBinder mBinder = new MyBinder();

    public class MyBinder extends Binder {

        public RTPProjectionService getInstance() {
            return RTPProjectionService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    Handler handler = new Handler();

    RTPSessionWrapper rtpSession;

    public void start(final String ip, final int resultCode,final Intent data){
        if (null == rtpSession) new Thread(new Runnable() {
            @Override
            public void run() {
                initRTP(ip, resultCode, data);
            }
        }).start();
    }

    public void stop(){
        stopEncode();
        if (null != rtpSession) {
            rtpSession.endSession();
            rtpSession = null;
        }
    }

    private void initRTP(String ip, final int resultCode, final Intent data) {
        try {
            setFrameHandler(this);
            rtpSession = new RTPSessionWrapper(new DatagramSocket(RTPParam.RTP_PORT), new DatagramSocket(RTPParam.RTCP_PORT));
            rtpSession.RTPSessionRegister(rtpAppIntfWrapper, null, null);
            Participant participant = new Participant(ip, RTPParam.RTP_PORT, RTPParam.RTCP_PORT);
            rtpSession.addParticipant(participant);
            rtpSession.payloadType(96);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        RTPProjectionService.super.startEncode(resultCode, data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (SocketException e) {
            e.printStackTrace();
            logd("初始化RTPSession失败");
        }
    }

    @Override
    public void handle(byte[] data, int length) {
        if (null != rtpSession) {
//            logd("send " + length);
            rtpSession.sendDataByMutiPkt(data);
        }
    }

    RTPAppIntfWrapper rtpAppIntfWrapper = new RTPAppIntfWrapper() {
        @Override
        protected void offerDecoder(byte[] buf, int length) {

        }

        @Override
        public void userEvent(int type, Participant[] participant) {
            logd("userEvent->" + type + "," + participant);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logd("start rtp projection service.");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (null != rtpSession) {
            rtpSession.endSession();
        }

        logd("stop rtp projection service.");
    }

}
