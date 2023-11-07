package com.luoj.airdroid.activity;

import android.app.Activity;
import android.widget.Toast;

import com.elvishew.xlog.XLog;

public class BaseActivity extends Activity {

    protected void logd(String content) {
        XLog.d("[" + this.getClass().getSimpleName() + "] " + content);
    }

    protected void toast(final String content){
        runOnUiThread(() -> Toast.makeText(BaseActivity.this, content, Toast.LENGTH_SHORT).show());
    }

}
