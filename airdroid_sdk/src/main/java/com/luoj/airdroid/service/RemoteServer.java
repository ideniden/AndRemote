package com.luoj.airdroid.service;

import android.os.Looper;
import android.os.Process;

import com.elvishew.xlog.XLog;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.luoj.airdroid.EventInput;
import com.luoj.airdroid.SocketParam;

import java.util.ArrayList;

/**
 * Created by omerjerk on 5/10/15.
 */
public class RemoteServer {

    static EventInput eventInput;

    static AsyncHttpServer httpServer;
    static ArrayList<WebSocket> sockets = new ArrayList<>();

    static Looper looper;

    public static void main(String[] args) {

        Looper.prepare();
        looper = Looper.myLooper();

        logd("current process id = " + Process.myPid());
        logd("current process uid = " + Process.myUid());
        try {
            eventInput = new EventInput();
        } catch (Exception e) {
            e.printStackTrace();
        }

        initSocketServer();

        if (eventInput == null) {
            logd("THIS SHIT IS NULL");
        } else {
            logd("THIS SHIT NOT NULL");
        }

        logd("Waiting for main to finish");
        Looper.loop();
        logd("Returning from MAIN");
    }

    public static void initSocketServer() {
        logd("init socket server. " + SocketParam.REMOTE_PORT);
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

    public static void parse(String s) {
        EventInput.handleString(eventInput, s, 1920, 1080);
    }

    private static void logd(String content) {
        XLog.d("[" + RemoteService.class.getSimpleName() + "] " + content);
    }

}
