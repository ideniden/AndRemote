package com.luoj.airdroid.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.TextView;

import com.luoj.airdroid.CodecParam;
import com.luoj.airdroid.sdk.R;
import com.luoj.airdroid.service.ProjectionService;
import com.luoj.airdroid.service.RTPProjectionService;

public class InitActivity extends ProjectionActivity {

    protected RTPProjectionService rtpProjectionService;
    protected ServiceConnection serviceConnection;

    protected int resultCode = RESULT_OK;
    protected Intent data;

    String ip;

    TextView tvMsg;

    @Override
    protected void mediaProjectionEnabled(final int resultCode, final Intent data) {
        this.resultCode = resultCode;
        this.data = data;

        if (null == rtpProjectionService) {
            bindService(new Intent(this, RTPProjectionService.class), serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    rtpProjectionService = ((RTPProjectionService.MyBinder) service).getInstance();
                    onProjectionServiceEnabled(rtpProjectionService, resultCode, data);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            }, Context.BIND_AUTO_CREATE);
        }
    }

    protected void onProjectionServiceEnabled(RTPProjectionService rtpProjectionService, int resultCode, Intent data) {
        tvMsg.append("\nMediaProjection is Rready.");
        tvMsg.append("\nvideo parameters : " + CodecParam.width + "x" + CodecParam.height + " , " + CodecParam.framerate + " , " + CodecParam.bitrate);
        start();
    }

    BroadcastReceiver projectionStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra("state", 0);
            if (state == ProjectionService.STATE_RUNNING) {
                tvMsg.append("\nMediaProjection is running.");
                tvMsg.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 3000);
            }
        }
    };

    protected boolean start() {
        if (null != rtpProjectionService && null != data) {
            rtpProjectionService.start(ip, resultCode, data);
            return true;
        }
        return false;
    }

    protected void stop() {
        if (null != rtpProjectionService) {
            rtpProjectionService.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(projectionStateReceiver);

        if (null != serviceConnection) {
            unbindService(serviceConnection);
            rtpProjectionService = null;
        }
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_init);
        ip = getIntent().getStringExtra("ip");
        tvMsg = (TextView) findViewById(R.id.tv_msg);

        registerReceiver(projectionStateReceiver, new IntentFilter(ProjectionService.ACTION_STATE));
    }

}
