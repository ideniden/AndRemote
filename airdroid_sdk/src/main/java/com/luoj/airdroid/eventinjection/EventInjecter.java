package com.luoj.airdroid.eventinjection;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by 京 on 2016/6/22.
 * 参考资料：https://blog.csdn.net/superbigcupid/article/details/52228230
 */
public class EventInjecter {

    public static final String COMMAND_SU = "su";
    public static final String COMMAND_SH = "sh";
    public static final String COMMAND_EXIT = "exit\n";
    public static final String COMMAND_LINE_END = "\n";

    final String SEND_EVENT = "sendevent /dev/input/event%s %s %s %s";
    final String INPUT = "input %s %s";

    public EventInjecter(DeviceInput deviceInput) {
        this.deviceInput = deviceInput;
    }

    DeviceInput deviceInput;

    Process process;
    DataOutputStream os;
    AtomicBoolean initialized = new AtomicBoolean(false);

    public static boolean isSupport() {
        return ShellUtils.checkRootPermission();
    }

    public boolean initialize() throws IOException {
        if (!isSupport() || !initialized.compareAndSet(false, true)) {
            return false;
        }
        process = Runtime.getRuntime().exec(COMMAND_SU);
        os = new DataOutputStream(process.getOutputStream());
        return true;
    }

    public boolean unInitialize() {
        try {
            if (os != null) {
                os.close();
                os = null;
            }
//            if (successResult != null) {
//                successResult.close();
//            }
//            if (errorResult != null) {
//                errorResult.close();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (process != null) {
            process.destroy();
            process = null;
        }
        return true;
    }

    public void exec(String cmd) throws IOException {
        if (null == os) {
            throw new IOException("OutputStream is null");
        }
        os.write(cmd.getBytes());
        os.writeBytes(COMMAND_LINE_END);
        os.flush();
        Log.d("EventInjecter", "cmd->" + cmd);
    }

    public void input(String type, String value) throws IOException {
        exec(String.format(INPUT, type, value));
    }

    public void inputKeyEvent(int keyCode) throws IOException {
        input(Constants.INPUT_KEYEVENT, keyCode + "");
    }

    public void injectEvent(String device, String type, String code, String value) throws IOException {
        exec(String.format(SEND_EVENT, device, type, code, value));
    }

    public void injectEventSync(String device, String type, String code, String value) throws IOException {
        // donnot use os.writeBytes(commmand), avoid chinese charset error
        exec(String.format(SEND_EVENT, device, type, code, value));
        exec(String.format(SEND_EVENT, device, "0", "0", "0"));
    }

    private void injectMotionEvent(Motion motion, String value) throws IOException {
        injectEvent(deviceInput.getTouchScreenDeviceValue() + "", motion.type, motion.code, TextUtils.isEmpty(value) ? motion.value : value);
    }

    private void injectMotionEventSync(Motion motion, String value) throws IOException {
        injectEventSync(deviceInput.getTouchScreenDeviceValue() + "", motion.type, motion.code, TextUtils.isEmpty(value) ? motion.value : value);
    }

    public void injectMotionEvent(int action, int x, int y) throws IOException {
        if (!deviceInput.isSupportTouchScreen()) return;
        String touchScreenDevice = deviceInput.getTouchScreenDeviceValue() + "";
        Motion motionX = deviceInput.getMotionX();
        Motion motionY = deviceInput.getMotionY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                injectEvent(touchScreenDevice, Constants.EV_ABS + "", "57", "1");//ABS_MT_TRACKING_ID
                injectMotionEvent(motionX, "" + x);//ABS_MT_POSITION_X
                injectMotionEvent(motionY, "" + y);//ABS_MT_POSITION_Y
                injectEvent(touchScreenDevice, Constants.EV_ABS + "", Constants.ABS_MT_TOUCH_MAJOR + "", "10");
                injectEvent(touchScreenDevice, Constants.EV_ABS + "", Constants.ABS_MT_WIDTH_MAJOR + "", "10");
                injectEvent(touchScreenDevice, Constants.EV_SYN + "", Constants.SYN_REPORT + "", "0");
                break;
            case MotionEvent.ACTION_MOVE:
                injectEvent(touchScreenDevice, Constants.EV_ABS + "", Constants.ABS_MT_TOUCH_MAJOR + "", "10");
                injectMotionEvent(motionX, "" + x);
                injectMotionEventSync(motionY, "" + y);
                break;
            case MotionEvent.ACTION_UP:
                injectEventSync(touchScreenDevice, Constants.EV_ABS + "", "57", "4294967295");//ABS_MT_TRACKING_ID
                break;
        }
    }

    public void injectMotionTap(int x, int y) throws IOException {
        injectMotionEvent(MotionEvent.ACTION_DOWN, x, y);
        injectMotionEvent(MotionEvent.ACTION_UP, x, y);
    }

    public void injectKeyEvent(KeyEvent event) throws IOException {
        Key key = deviceInput.getKey(event.getKeyCode());
        if (null == key) throw new RuntimeException("not support this key.");
        String eventValue = key.getEventValue(event.getAction());
        if (TextUtils.isEmpty(eventValue))
            throw new RuntimeException("not support this key event.");
        injectEventSync(key.device, key.type, key.code, eventValue);
    }

    public void injectKeyTap(int keyCode) throws IOException {
        injectKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
        injectKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
    }

}
