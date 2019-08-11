package com.luoj.airdroid.service;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.elvishew.xlog.XLog;

import java.io.DataOutputStream;
import java.io.IOException;

public class RemoteServerLauncher extends Thread{

    private static final String COMMAND = "su -c \"CLASSPATH=%s /system/bin/app_process32 " +
            "/system/bin com.luoj.airdroid.service.RemoteServer\"\n";

    private Context context;

    public RemoteServerLauncher(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        //        Shell.SU.run(String.format(COMMAND, getApkLocation()));
        try {
            String cmd = String.format(COMMAND, getApkLocation());
            logd("start remote server -> " + cmd);
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes(cmd);
            outputStream.flush();
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getApkLocation() {
        PackageManager pm = context.getPackageManager();
        for (ApplicationInfo app : pm.getInstalledApplications(0)) {
            if (app.packageName.equals(context.getPackageName())) {
                return app.sourceDir;
            }
        }
        return null;
    }

    private static void logd(String content) {
        XLog.d("[" + RemoteService.class.getSimpleName() + "] " + content);
    }

}
