package com.luoj.airdroid.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.elvishew.xlog.XLog;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.luoj.airdroid.EventInput;
import com.luoj.airdroid.SocketParam;

import java.util.ArrayList;

public class RemoteService extends Service {

    IBinder mBinder = new MyBinder();

    public class MyBinder extends Binder {
        public RemoteService getInstance() {
            return RemoteService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    boolean init;

    AsyncHttpServer httpServer;
    ArrayList<WebSocket> sockets = new ArrayList<>();

    EventInput eventInput;
    private float deviceWidth;
    private float deviceHeight;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!init) {
            initSocketServer();
            acquireDeviceScreen();
            init = true;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void acquireDeviceScreen() {
        try {
            WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(dm);
            deviceWidth = dm.widthPixels;
            deviceHeight = dm.heightPixels;
            logd(deviceWidth + " x " + deviceHeight);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != httpServer) {
            httpServer.stop();
            httpServer = null;
        }

        if (!sockets.isEmpty()) {
            for (WebSocket s: sockets) {
                s.end();
            }
            sockets.clear();
        }

        eventInput = null;

        init = false;

        logd("stop remote service.");
    }

    private void initSocketServer() {
        logd("start remote service. port -> " + SocketParam.REMOTE_PORT);
        httpServer = new AsyncHttpServer();
        httpServer.websocket(SocketParam.REMOTE_URI, null, new AsyncHttpServer.WebSocketRequestCallback() {
            @Override
            public void onConnected(final WebSocket webSocket, AsyncHttpServerRequest request) {
                logd("someone connected to RemoteServer.");
                sockets.add(webSocket);
                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        logd("someone disconnected from RemoteServer.");
                        try {
                            if (ex != null) logd("An error occurred\n" + ex.toString());
                        } finally {
                            sockets.remove(webSocket);
                        }
                    }
                });
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override
                    public void onStringAvailable(String s) {
                        parse(s);
                    }
                });
            }
        });
        httpServer.setErrorCallback(new CompletedCallback() {
            @Override
            public void onCompleted(Exception ex) {
                if (ex != null) logd("An error occurred\n" + ex.toString());
            }
        });
        httpServer.listen(SocketParam.REMOTE_PORT);
    }

    private void parse(String s) {
        if (null == eventInput) {
            try {
                eventInput = new EventInput();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        EventInput.handleString(eventInput, s, deviceWidth, deviceHeight);
    }

    protected void logd(String content) {
        XLog.d("[" + this.getClass().getSimpleName() + "] " + content);
    }

}
