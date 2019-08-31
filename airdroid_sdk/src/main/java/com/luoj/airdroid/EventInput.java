package com.luoj.airdroid;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.support.v4.view.InputDeviceCompat;
import android.text.TextUtils;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.elvishew.xlog.XLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by omerjerk on 19/9/15.
 * <p>
 * Class to create seamless input/touch events on your Android device without root
 */
public class EventInput {

    Method injectInputEventMethod;
    InputManager im;

    static EventInput eventInput;

    public static EventInput getInstance() throws Exception {
        if (null == eventInput) {
            eventInput = new EventInput();
        }
        return eventInput;
    }

    public EventInput() throws Exception {
        //Get the instance of InputManager class using reflection
        String methodName = "getInstance";
        Object[] objArr = new Object[0];
        im = (InputManager) InputManager.class.getDeclaredMethod(methodName, new Class[0])
                .invoke(null, objArr);

        //Make MotionEvent.obtain() method accessible
        methodName = "obtain";
        MotionEvent.class.getDeclaredMethod(methodName, new Class[0]).setAccessible(true);

        //Get the reference to injectInputEvent method
        methodName = "injectInputEvent";
        injectInputEventMethod = InputManager.class.getMethod(
                methodName, new Class[]{InputEvent.class, Integer.TYPE});
    }

    public void injectMotionEvent(int inputSource, int action, long when, float x, float y,
                                  float pressure) throws InvocationTargetException, IllegalAccessException {
        MotionEvent event = MotionEvent.obtain(when, when, action, x, y, pressure, 1.0f, 0, 1.0f, 1.0f, 0, 0);
        event.setSource(inputSource);
        injectInputEventMethod.invoke(im, new Object[]{event, Integer.valueOf(0)});
    }

    public void injectKeyEvent(int action, int code) throws InvocationTargetException, IllegalAccessException {
        injectKeyEvent(new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), action, code, 0));
    }

    public void injectKeyEvent(KeyEvent event)
            throws InvocationTargetException, IllegalAccessException {
        event.setSource(InputDeviceCompat.SOURCE_KEYBOARD);
        injectInputEventMethod.invoke(im, new Object[]{event, Integer.valueOf(0)});
    }

    public void injectTouchMotionEvent(int action, float x, float y) throws InvocationTargetException, IllegalAccessException {
        injectMotionEvent(InputDeviceCompat.SOURCE_TOUCHSCREEN, action,
                SystemClock.uptimeMillis(), x, y, 1.0f);
    }

    public static final int TYPE_TOUCH = 0;
    public static final int TYPE_KEYCODE = 1;

    /**
     * @param type   0touch 1key
     * @param action
     * @param code
     * @param x
     * @param y
     */
    public static String getString(int type, int action, int code, float x, float y) {
        if (type == TYPE_TOUCH) {
            return type + ";" + action + ";" + x + ";" + y;
        } else if (type == TYPE_KEYCODE) {
            return type + ";" + action + ";" + code + ";" + 0.0;
        } else {
            return null;
        }
    }

    public static int handleString(EventInput eventInput, String order, float deviceWidth, float deviceHeight) {
        if (!TextUtils.isEmpty(order) && order.contains(";")) {

            String[] split = order.split(";");

            if (split.length == 4) {
                try {
                    int type = Integer.parseInt(split[0]);
                    int action = Integer.parseInt(split[1]);

                    if (type == 0) {
                        float x = Float.parseFloat(split[2]) * deviceWidth;
                        float y = Float.parseFloat(split[3]) * deviceHeight;
                        eventInput.injectTouchMotionEvent(action, x, y);
                        logd("inject touch event success. " + order);
                        return type;
                    } else if (type == 1) {
                        int code = Integer.parseInt(split[2]);
                        eventInput.injectKeyEvent(action, code);
                        logd("inject key event success. " + order);
                        return type;
                    }

                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

    private static void logd(String content) {
        XLog.d("[" + EventInput.class.getSimpleName() + "] " + content);
    }

}
