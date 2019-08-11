package com.luoj.airdroid.eventinjection.devices;


import com.luoj.airdroid.eventinjection.DeviceInput;

/**
 * Created by 京 on 2016/6/23.
 */
public abstract class AbsDeviceInputDescriptor {

    DeviceInput deviceInput;

    public AbsDeviceInputDescriptor() {
        this.deviceInput =set();
    }

    public DeviceInput get(){
        return deviceInput;
    }

    protected abstract DeviceInput set();

}
