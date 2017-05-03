package net.raffy.mp3alarmclock.bluetooth;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

public class SocketApplicationBluetooth extends Application {

    private BluetoothDevice device = null;

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

}
