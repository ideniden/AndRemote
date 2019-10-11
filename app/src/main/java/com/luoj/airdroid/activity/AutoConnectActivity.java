package com.luoj.airdroid.activity;

import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.luoj.airdroid.AirDroid;
import com.luoj.airdroid.EventInput;
import com.luoj.airdroid.R;
import com.luoj.airdroid.RTPParam;
import com.luoj.airdroid.SocketParam;
import com.luoj.airdroid.Util;
import com.luoj.airdroid.decoder.VideoDecoder2;
import com.luoj.airdroid.service.RemoteService;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;

import jlibrtp.DataFrame;
import jlibrtp.DebugAppIntf;
import jlibrtp.Participant;
import jlibrtp.RTCPAppIntf;
import jlibrtp.RTPAppIntf;
import jlibrtp.RTPSession;

public class AutoConnectActivity extends BaseFullScreenActivity {

    String ip;

    ProgressBar progressBar;

    TextView tvIp;
    EditText etAddress;

    SurfaceView surfaceView;
    VideoDecoder2 videoDecoder2;

    RTPSession rtpSession;

    WebSocket remoteSocketServer;
    boolean connectingRemoteServer;
    private float deviceWidth;
    private float deviceHeight;

    @Override
    protected int setScreenWakeLock() {
        return PowerManager.FULL_WAKE_LOCK;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_auto_connect);

        ip = getIntent().getStringExtra("ip");

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        tvIp = (TextView) findViewById(R.id.tv_ip);
        tvIp.setTextColor(getResources().getColor(R.color.colorPrimary, getTheme()));
        tvIp.setText("local ip -> " + Util.getIP(this));

        etAddress = (EditText) findViewById(R.id.et_addrsss);
        if (!TextUtils.isEmpty(ip)) {
            etAddress.setText(ip);
            etAddress.setVisibility(View.GONE);
            findViewById(R.id.btn_connect).setVisibility(View.GONE);
        }

        surfaceView = (SurfaceView) findViewById(R.id.sv);
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
        logd("screen size -> " + deviceWidth + " x " + deviceHeight);

        new Thread(new Runnable() {
            @Override
            public void run() {
                initRTP();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        etAddress.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startClick(findViewById(R.id.btn_connect));
                            }
                        }, 1000);
                    }
                });
            }
        }).start();
    }

    public void startClick(View v) {
        String ip = this.etAddress.getText().toString();
        boolean result = connectServer(ip);
        if (!result) {
            toast("already connected.");
        }
    }

    private void initRTP() {
        try {
            rtpSession = new RTPSession(new DatagramSocket(RTPParam.RTP_PORT), new DatagramSocket(RTPParam.RTCP_PORT));
//            rtpSession.naivePktReception(true);
            rtpSession.RTPSessionRegister(rtpAppIntf, rtcpAppIntf, debugAppIntf);
            rtpSession.addParticipant(new Participant(ip, RTPParam.RTP_PORT, RTPParam.RTCP_PORT));
            rtpSession.payloadType(96);
            rtpSession.packetBufferBehavior(0);
            logd("rtp receiver is ready.");
        } catch (SocketException e) {
            logd("init RTPSession failed.");
            e.printStackTrace();
        }
    }

    private boolean connectServer(String ip) {
        if (!TextUtils.isEmpty(ip) && null == remoteSocketServer && !connectingRemoteServer) {
            connectingRemoteServer = true;
            final String uri = "ws://" + ip + ":" + SocketParam.REMOTE_PORT;
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
                                logd("disconnect from remote server.");
                            }
                        }
                    });
                    connectingRemoteServer = false;

                    onConnectedServer();
                }
            });
            return true;
        }
        return false;
    }

    private void onConnectedServer() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                tvIp.setVisibility(View.GONE);
                etAddress.setVisibility(View.GONE);
                findViewById(R.id.btn_connect).setVisibility(View.GONE);
                findViewById(R.id.btn_back).setVisibility(View.VISIBLE);
            }
        });
        sendBeginOrder();
    }

    private void sendBeginOrder() {
        remoteSocketServer.send(AirDroid.getBeginOrder());
    }

    RTPAppIntf rtpAppIntf = new RTPAppIntf() {
        byte[] buf;

        @Override
        public void receiveData(DataFrame frame, Participant participant) {
            logd("sequenceNumbers->"+Arrays.toString(frame.sequenceNumbers()));
            if (buf == null) {
                buf = frame.getConcatenatedData();
            } else {
                buf = merge(buf, frame.getConcatenatedData());
            }
            if (frame.marked()) {
                offerDecoder(buf, buf.length);
                buf = null;
            }
        }

        private void offerDecoder(byte[] buf, int length) {
            videoDecoder2.setVideoData(buf);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                    progressBar.removeCallbacks(visiblePb);
                    progressBar.postDelayed(visiblePb, 2000);
                }
            });
//            logd(""+rtpSession.naivePktReception(););
        }

        Runnable visiblePb = new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        };

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
            logd("Cumul Packets Lost -> " + (null != cumulPacketsLost ? Arrays.toString(cumulPacketsLost) : "null"));
            logd("Inter Arrival Jitter -> " + (null != interArrivalJitter ? Arrays.toString(interArrivalJitter) : "null"));
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

    DebugAppIntf debugAppIntf = new DebugAppIntf() {
        @Override
        public void packetReceived(int type, InetSocketAddress socket, String description) {
            logd("debug packetReceived type -> " + type + " , " + description);
        }

        @Override
        public void packetSent(int type, InetSocketAddress socket, String description) {
            logd("debug packetSent type -> " + type + " , " + description);
        }

        @Override
        public void importantEvent(int type, String description) {
            logd("debug importantEvent type -> " + type + " , " + description);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != rtpSession) {
            rtpSession.endSession();
        }

        if (null != remoteSocketServer) {
            remoteSocketServer.send(RemoteService.SOCKET_ORDER_END);
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

    @Override
    protected void logd(final String content) {
        super.logd(content);
        tvIp.post(new Runnable() {
            @Override
            public void run() {
                tvIp.append("\n" + content);
            }
        });
    }

}
