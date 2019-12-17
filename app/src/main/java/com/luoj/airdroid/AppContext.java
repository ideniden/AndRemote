package com.luoj.airdroid;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.PatternFlattener;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;
import com.luoj.airdroid.eventinjection.ShellUtils;

import java.io.File;

public class AppContext extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        String tag = "AirDroid";
        String logDirPath = Environment.getExternalStorageDirectory() + File.separator + tag + File.separator;
        initLog(tag, logDirPath);

        printCodecList();

        CodecParam.setOritationLandscape();
        CodecParam.framerate = 30;
        CodecParam.bitrate = 1024 * 1024 * 1;
        CodecParam.i_frame_interval = 1;

        XLog.d("has root -> " + ShellUtils.checkRootPermission());
        XLog.d("has system promission -> " + isSystemApp());
    }

    private void printCodecList() {
        XLog.d("-----硬件编解码器-----");
        XLog.d(Util.getSupportCodecLog());
        XLog.d("----------------------");
    }

    void initLog(String tag, String logDirPath) {
        File logDir = new File(logDirPath);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        LogConfiguration config = new LogConfiguration.Builder()
                .logLevel(LogLevel.ALL)
                .tag(tag)                                  // 指定 TAG，默认为 "X-LOG"
//                .t()                                                   // 允许打印线程信息，默认禁止
//                .st(3)                                                 // 允许打印深度为2的调用栈信息，默认禁止
//                .b()                                                   // 允许打印日志边框，默认禁止
                .build();
        Printer filePrinter = new FilePrinter                          // 打印日志到文件的打印器
                .Builder(logDirPath)                       // 指定保存日志文件的路径
                .fileNameGenerator(new DateFileNameGenerator())        // 指定日志文件名生成器，默认为 ChangelessFileNameGenerator("log")
//                .backupStrategy(new MyBackupStrategy())              // 指定日志文件备份策略，默认为 FileSizeBackupStrategy(1024 * 1024)
                .logFlattener(new PatternFlattener("{d yyyy-MM-dd HH:mm:ss} {L}/{t}:{m}"))                  // 指定日志平铺器，默认为 DefaultLogFlattener
                .build();
//        if (BuildConfig.DEBUG) {
        Printer androidPrinter = new AndroidPrinter();
        XLog.init(config, filePrinter, androidPrinter);
//        } else {
//            XLog.init(config, filePrinter);
//        }
    }

    boolean isSystemApp() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

}
