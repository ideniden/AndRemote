package com.luoj.airdroid.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;

import com.luoj.airdroid.RTPParam;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

import jlibrtp.Participant;
import jlibrtp.RTCPAppIntf;
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

    public static final String ACTION_ORDER = "action.rtpprojectionservice.order";
    public static final String KEY_ORDER = "order";
    public static final String ORDER_STOP = "order.stop";

    public static final String ACTION_RTCP = "action.rtpprojectionservice.rtcp";

    boolean init;

    Handler handler = new Handler();

    RTPSessionWrapper rtpSession;

    public void start(final String ip, final int resultCode, final Intent data) {
        if (null == rtpSession) new Thread(new Runnable() {
            @Override
            public void run() {
                initRTP(ip, resultCode, data);
            }
        }).start();
    }

    public void stop() {
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
            rtpSession.RTPSessionRegister(rtpAppIntfWrapper, rtcpAppIntf, null);
            Participant participant = new Participant(ip, RTPParam.RTP_PORT, RTPParam.RTCP_PORT);
            rtpSession.addParticipant(participant);
            rtpSession.payloadType(96);
            rtpSession.packetBufferBehavior();

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

    RTCPAppIntf rtcpAppIntf = new RTCPAppIntf() {
        @Override
        public void SRPktReceived(long ssrc, long ntpHighOrder, long ntpLowOrder, long rtpTimestamp, long packetCount, long octetCount, long[] reporteeSsrc, int[] lossFraction, int[] cumulPacketsLost, long[] extHighSeq, long[] interArrivalJitter, long[] lastSRTimeStamp, long[] delayLastSR) {
            logd("---------------SR pkt info---------------");
            logd("packetCount -> " + packetCount);
            logd("octetCount -> " + octetCount);
            logd("Loss Fraction -> " + (null != lossFraction ? Arrays.toString(lossFraction) : "null"));
            logd("Cumul Packets Lost -> " + (null != cumulPacketsLost ? Arrays.toString(cumulPacketsLost) : "null"));
            logd("Inter Arrival Jitter -> " + (null != interArrivalJitter ? Arrays.toString(interArrivalJitter) : "null"));
        }

        @Override
        public void RRPktReceived(long reporterSsrc, long[] reporteeSsrc, int[] lossFraction, int[] cumulPacketsLost, long[] extHighSeq, long[] interArrivalJitter, long[] lastSRTimeStamp, long[] delayLastSR) {
            logd("---------------RR pkt info---------------");
            logd("Loss Fraction -> " + (null != lossFraction ? Arrays.toString(lossFraction) : "null"));
            if (null != lossFraction && lossFraction.length > 0) {
                logd("Loss Percent -> " + lossPercent(lossFraction[0]) + "%");
            }
            logd("Cumul Packets Lost -> " + (null != cumulPacketsLost ? Arrays.toString(cumulPacketsLost) : "null"));
            logd("Inter Arrival Jitter -> " + (null != interArrivalJitter ? Arrays.toString(interArrivalJitter) : "null"));
            Intent intent = new Intent(ACTION_RTCP);
            if (null != lossFraction) intent.putExtra("lossFraction", lossFraction);
            if (null != cumulPacketsLost) intent.putExtra("cumulPacketsLost", cumulPacketsLost);
            if (null != interArrivalJitter) intent.putExtra("interArrivalJitter", interArrivalJitter);
            sendBroadcast(intent);
        }

        int lossPercent(int lossFraction) {
            int percent = (int) ((float) lossFraction / 256f * 100f);
            return percent;
        }

        @Override
        public void SDESPktReceived(Participant[] relevantParticipants) {

        }

        @Override
        public void BYEPktReceived(Participant[] relevantParticipants, String reason) {

        }

        @Override
        public void APPPktReceived(Participant part, int subtype, byte[] name, byte[] data) {

        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!init) {
            logd("start rtp projection service.");
            registerReceiver(orderReceiver, new IntentFilter(ACTION_ORDER));
            init = true;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    BroadcastReceiver orderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String order = intent.getStringExtra(KEY_ORDER);
            if (TextUtils.equals(order, ORDER_STOP)) {
                stop();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (init) {
            unregisterReceiver(orderReceiver);
        }

        if (null != rtpSession) {
            rtpSession.endSession();
        }

        logd("stop rtp projection service.");
    }

}
