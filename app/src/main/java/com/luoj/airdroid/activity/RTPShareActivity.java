package com.luoj.airdroid.activity;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.luoj.airdroid.R;
import com.luoj.airdroid.RTPParam;
import com.luoj.airdroid.Util;
import com.luoj.airdroid.service.ProjectionService;
import com.luoj.airdroid.service.RemoteServerLauncher;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

import jlibrtp.Participant;
import jlibrtp.RTPAppIntfWrapper;
import jlibrtp.RTPSessionWrapper;

public class RTPShareActivity extends ProJectionServiceActivity {

    TextView tvIp;
    EditText playIp;

    RTPSessionWrapper rtpSession;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_rtpshare);
        tvIp = (TextView) findViewById(R.id.tv_ip);
        tvIp.setText(Util.getIP(this));
        playIp = (EditText) findViewById(R.id.et_input);
    }

    public void startClick(View v) {
        if (null == rtpSession) new Thread(new Runnable() {
            @Override
            public void run() {
                initRTP();
            }
        }).start();
    }

    public void stopClick(View v) {
        stopEncode();
        if (null != rtpSession) {
            rtpSession.endSession();
            rtpSession = null;
        }
    }

    public void homeClick(View v){
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }

    private void initRTP() {
        try {
            String ip = this.playIp.getText().toString();
            rtpSession = new RTPSessionWrapper(new DatagramSocket(RTPParam.RTP_PORT), new DatagramSocket(RTPParam.RTCP_PORT));
            rtpSession.RTPSessionRegister(rtpAppIntfWrapper, null, null);
            Participant participant = new Participant(ip, RTPParam.RTP_PORT, RTPParam.RTCP_PORT);
            rtpSession.addParticipant(participant);
            rtpSession.payloadType(96);

            startRemoteServer();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        projectionService.startEncode(resultCode, data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (SocketException e) {
            e.printStackTrace();
            toast("初始化RTPSession失败");
        }
    }

    int resultCode;
    Intent data;

    @Override
    protected void onProjectionServiceEnabled(ProjectionService projectionService, int resultCode, Intent data) {
        this.resultCode = resultCode;
        this.data = data;
        tvIp.setTextColor(getResources().getColor(R.color.colorPrimary, getTheme()));
        tvIp.append(" - MediaProjection Ready.");
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
    protected void onDestroy() {
        super.onDestroy();

        if (null != rtpSession) {
            rtpSession.endSession();
        }

        stopRemoteServer();

    }

    private void startRemoteServer() {
        new RemoteServerLauncher(this).start();
//            startService(new Intent(this, RemoteService.class));
    }

    private void stopRemoteServer() {
        //        stopService(new Intent(this, RemoteService.class));
    }

}
