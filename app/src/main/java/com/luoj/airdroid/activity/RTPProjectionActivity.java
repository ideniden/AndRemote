package com.luoj.airdroid.activity;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.luoj.airdroid.AirDroid;
import com.luoj.airdroid.R;
import com.luoj.airdroid.Util;
import com.luoj.airdroid.service.RTPProjectionService;

public class RTPProjectionActivity extends RTPProjectionServiceActivity {

    TextView tvIp;
    EditText playIp;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_rtpshare);
        tvIp = (TextView) findViewById(R.id.tv_ip);
        tvIp.setText(Util.getIP(this));
        playIp = (EditText) findViewById(R.id.et_input);
    }

    public void startClick(View v) {
        start(playIp.getText().toString());
    }

    public void stopClick(View v) {
        stop();
    }

    public void homeClick(View v) {
//        Intent home = new Intent(Intent.ACTION_MAIN);
//        home.addCategory(Intent.CATEGORY_HOME);
//        startActivity(home);
        AirDroid.init(this);
    }

    @Override
    protected void onProjectionServiceEnabled(RTPProjectionService rtpProjectionService, int resultCode, Intent data) {
        tvIp.setTextColor(getResources().getColor(R.color.colorPrimary, getTheme()));
        tvIp.append(" - MediaProjection Ready.");
    }

}
