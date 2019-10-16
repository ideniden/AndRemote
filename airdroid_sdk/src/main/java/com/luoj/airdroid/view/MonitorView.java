package com.luoj.airdroid.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.TextView;

import com.luoj.airdroid.sdk.R;
import com.luoj.android.window.DraggableSuspendLayout;

public class MonitorView {

    Context ctx;

    DraggableSuspendLayout dsl;

    private TextView tv;

    public MonitorView(Context ctx) {
        this.ctx = ctx;
        dsl = new DraggableSuspendLayout(ctx);

        tv = new TextView(ctx);
        tv.setBackgroundColor(ctx.getColor(R.color.transparent_40));
        tv.setTextColor(ctx.getColor(android.R.color.white));
        tv.setTextSize(18);
        tv.setPadding(24,24,24,24);

        dsl.addView(tv, -2, -2);
    }

    public boolean show() {
        if (checkAlertWindowPermission(ctx)) {
            dsl.show();
            return true;
        }
        return false;
    }

    public void dismiss() {
        dsl.dismiss();
    }

    public boolean isShowing() {
        return dsl.isShow();
    }

    public void setText(String content) {
        tv.setText(content);
    }

    @TargetApi(23)
    public boolean checkAlertWindowPermission(Context context) {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(context)) {
//            Toast.makeText(context, "当前无权限，请授权！", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
            return false;
        }
        return true;
    }

}


