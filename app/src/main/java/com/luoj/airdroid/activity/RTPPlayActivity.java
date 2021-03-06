package com.luoj.airdroid.activity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.luoj.airdroid.EventInput;
import com.luoj.airdroid.R;
import com.luoj.airdroid.RTPParam;
import com.luoj.airdroid.SocketParam;
import com.luoj.airdroid.Util;
import com.luoj.airdroid.decoder.VideoDecoder2;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;

import jlibrtp.DataFrame;
import jlibrtp.Participant;
import jlibrtp.RTPAppIntf;
import jlibrtp.RTPSession;

public class RTPPlayActivity extends BaseFullScreenActivity {

    TextView tvIp;
    EditText playIp;

    SurfaceView surfaceView;
    VideoDecoder2 videoDecoder2;

    RTPSession rtpSession;

    WebSocket remoteSocketServer;
    boolean connectingRemoteServer;
    private float deviceWidth;
    private float deviceHeight;

    @Override
    protected int setScreenWakeLock() {
        return 1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtpplay);
        tvIp = (TextView) findViewById(R.id.tv_ip);
        tvIp.setText(Util.getIP(this));
        playIp = (EditText) findViewById(R.id.et_input);

        surfaceView = (SurfaceView) findViewById(R.id.sv);
//        surfaceView.setOnTouchListener(this);
//        videoDecoder = new VideoDecoder(surfaceView.getHolder());
        videoDecoder2 = new VideoDecoder2(surfaceView.getHolder().getSurface());
        surfaceView.getHolder().addCallback(videoDecoder2);

        Button btnBack = (Button) findViewById(R.id.btn_back);
        btnBack.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (null != remoteSocketServer) {
                    String order = EventInput.getString(EventInput.TYPE_KEYCODE, event.getAction(), KeyEvent.KEYCODE_BACK, 0, 0);
                    remoteSocketServer.send(order);
                }
                return false;
            }
        });

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        deviceWidth = dm.widthPixels;
        deviceHeight = dm.heightPixels;
        logd(deviceWidth + " x " + deviceHeight);

        startClick(null);
    }

    public void startClick(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initRTP();
            }
        }).start();
    }

    private void initRTP() {
        try {
            String ip = this.playIp.getText().toString();
            rtpSession = new RTPSession(new DatagramSocket(RTPParam.RTP_PORT), new DatagramSocket(RTPParam.RTCP_PORT));
            rtpSession.naivePktReception(true);
            rtpSession.RTPSessionRegister(rtpAppIntf, null, null);
//            Participant participant = new Participant(ip, 8002, 8003);
//            rtpSession.addParticipant(participant);
            rtpSession.payloadType(96);

            tvIp.post(new Runnable() {
                @Override
                public void run() {
                    tvIp.append(" - Ready.");
                    tvIp.setTextColor(getResources().getColor(R.color.colorPrimary, getTheme()));
                }
            });
        } catch (SocketException e) {
            e.printStackTrace();
            toast("init RTPSession failed.");
        }
    }

    RTPAppIntf rtpAppIntf = new RTPAppIntf() {
        byte[] buf;

        @Override
        public void receiveData(DataFrame frame, Participant participant) {
//            InetSocketAddress rtpReceivedFromAddress = participant.getRtpReceivedFromAddress();
//            logd("ip:" + (null != rtpReceivedFromAddress ? rtpReceivedFromAddress.getAddress() : null));
            if (buf == null) {
                buf = frame.getConcatenatedData();
            } else {
                buf = merge(buf, frame.getConcatenatedData());
            }
            if (frame.marked()) {

                connectRemoteServer(participant.getRtpReceivedFromAddress());

                offerDecoder(buf, buf.length);
                buf = null;
            }
        }

        private void offerDecoder(byte[] buf, int length) {
//            logd("get complete data." + length);
            videoDecoder2.setVideoData(buf);
        }

        @Override
        public void userEvent(int type, Participant[] participant) {
            logd("userEvent->" + type + "," + participant);
        }

        @Override
        public int frameSize(int payloadType) {
            return 0;
        }

        public byte[] merge(byte[] first, byte[] second) {
            byte[] result = Arrays.copyOf(first, first.length + second.length);
            System.arraycopy(second, 0, result, first.length, second.length);
            return result;
        }
    };

    private void connectRemoteServer(InetSocketAddress addr) {
        if (null != addr && null == remoteSocketServer && !connectingRemoteServer) {
            connectingRemoteServer = true;
            //getAddress() start with /
            final String uri = "ws:/" + addr.getAddress() + ":" + SocketParam.REMOTE_PORT;
            AsyncHttpClient.getDefaultInstance().websocket(uri, null, new AsyncHttpClient.WebSocketConnectCallback() {
                @Override
                public void onCompleted(Exception ex, WebSocket webSocket) {
                    if (null != ex) {
                        logd("connect to remote server failed. " + uri);
                        connectingRemoteServer = false;
                        return;
                    }
                    logd("connect to remote server success. " + uri);
                    remoteSocketServer = webSocket;
                    remoteSocketServer.setClosedCallback(new CompletedCallback() {
                        @Override
                        public void onCompleted(Exception ex) {
                            if (null != remoteSocketServer) {
                                remoteSocketServer.end();
                                remoteSocketServer = null;
                            }
                        }
                    });
                    connectingRemoteServer = false;
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != rtpSession) {
            rtpSession.endSession();
        }

        if (null != remoteSocketServer) {
            remoteSocketServer.end();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != remoteSocketServer) {
            String order = EventInput.getString(EventInput.TYPE_TOUCH, event.getAction(), 0, event.getX() / deviceWidth, event.getY() / deviceHeight);
            remoteSocketServer.send(order);
        }
        return super.onTouchEvent(event);
    }

}
