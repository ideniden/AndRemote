package com.luoj.airdroid.eventinjection;

/**
 * Created by 京 on 2016/6/22.
 */
public class Motion {

    public int action;
    public String device;
    public String type;
    public String code;
    public String value;

    public Motion(int action, String device, String type, String code,String value) {
        this.action = action;
        this.device = device;
        this.type = type;
        this.code = code;
        this.value = value;
    }

    /**
     * MoveMotion构造
     * @param action
     * @param type
     * @param code
     */
    public Motion(int action, String type, String code) {
        this.action = action;
        this.type = type;
        this.code = code;
    }
}
