package com.luoj.airdroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.luoj.airdroid.CodecParam;
import com.luoj.airdroid.R;
import com.luoj.airdroid.view.IPEditText;

public class InputActivity extends BaseFullScreenActivity {

    IPEditText ipEditText;

    EditText etWidth, etHeight, etFramerate, etBitrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        ipEditText = (IPEditText) findViewById(R.id.et_addrsss);

        etWidth = (EditText) findViewById(R.id.et_width);
        etWidth.setText(String.valueOf(CodecParam.width));
        etHeight = (EditText) findViewById(R.id.et_height);
        etHeight.setText(String.valueOf(CodecParam.height));
        etFramerate = (EditText) findViewById(R.id.et_framerate);
        etFramerate.setText(String.valueOf(CodecParam.framerate));
        etBitrate = (EditText) findViewById(R.id.et_bitrate);
        etBitrate.setText(String.valueOf(CodecParam.bitrate));
    }

    public void startClick(View v) {
        String ip = ipEditText.getText();
        if (!TextUtils.isEmpty(ip)) {
            setCodecParam();
            Intent intent = new Intent(this, AutoConnectActivity.class);
            intent.putExtra("ip", ip);
            startActivity(intent);
            finish();
        } else {
            toast("IP地址输入有误");
        }
    }

    private void setCodecParam() {
        CodecParam.width = Integer.parseInt(etWidth.getText().toString());
        CodecParam.height = Integer.parseInt(etHeight.getText().toString());
        CodecParam.framerate = Integer.parseInt(etFramerate.getText().toString());
        CodecParam.bitrate = Integer.parseInt(etBitrate.getText().toString());
    }

}
