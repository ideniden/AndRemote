package com.luoj.airdroid.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import com.elvishew.xlog.XLog;
import com.koushikdutta.async.AsyncNetworkSocket;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.luoj.airdroid.CodecParam;
import com.luoj.airdroid.EventInput;
import com.luoj.airdroid.SocketParam;
import com.luoj.airdroid.activity.InitActivity;

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

    public static final String SOCKET_ORDER_BEGIN = "BEGIN";
    public static final String SOCKET_ORDER_END = "END";

    Handler handler = new Handler();
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
            for (WebSocket s : sockets) {
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
                String ip = ((AsyncNetworkSocket) webSocket.getSocket()).getRemoteAddress().getAddress().getHostAddress();
                logd("someone(" + ip + ") connected to RemoteServer. path -> " + request.getPath());
                sockets.add(webSocket);
                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        logd("someone disconnected from RemoteServer.");
                        try {
                            if (ex != null) logd("An error occurred\n" + ex.toString());
                        } finally {
                            sockets.remove(webSocket);
                            if (sockets.isEmpty()) {
                                stopProjection();
                                toast("丢失与控制器的连接，远程控制已停止");
                            }
                        }
                    }
                });
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override
                    public void onStringAvailable(String s) {
                        parse(webSocket, s);
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

    private void parse(WebSocket webSocket, String order) {
        if (!TextUtils.isEmpty(order)) {
            if (order.startsWith(SOCKET_ORDER_BEGIN) && order.contains(";")) {
                //width;height;framerate;bitrate
                String[] split = order.split(";");
                if (split.length == 5) {
                    CodecParam.width = Integer.parseInt(split[1]);
                    CodecParam.height = Integer.parseInt(split[2]);
                    CodecParam.framerate = Integer.parseInt(split[3]);
                    CodecParam.bitrate = Integer.parseInt(split[4]);
                }
                Intent intent = new Intent(this, InitActivity.class);
                String ip = ((AsyncNetworkSocket) webSocket.getSocket()).getRemoteAddress().getAddress().getHostAddress();
                intent.putExtra("ip", ip);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return;
            }
            if (order.startsWith(SOCKET_ORDER_END)) {
                stopProjection();
                toast("远程控制已停止");
                return;
            }
        }
        if (null == eventInput) {
            try {
                eventInput = new EventInput();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        EventInput.handleString(eventInput, order, deviceWidth, deviceHeight);
    }

    void stopProjection() {
        Intent intent = new Intent(RTPProjectionService.ACTION_ORDER);
        intent.putExtra(RTPProjectionService.KEY_ORDER, RTPProjectionService.ORDER_STOP);
        sendBroadcast(intent);
    }

    void toast(final String s) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RemoteService.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void logd(String content) {
        XLog.d("[" + this.getClass().getSimpleName() + "] " + content);
    }

}
