package com.ifmo.graficreport;

public class MultiDeviceReport {
    private int numberOfDevice;
    private int numberOfDevices;
    private float durationMs;

    public MultiDeviceReport(int numberOfDevice, int numberOfDevices, float durationMs) {
        this.numberOfDevice = numberOfDevice;
        this.durationMs = durationMs;
    }

    public int getNumberOfDevice() {
        return numberOfDevice;
    }

    public void setNumberOfDevice(int numberOfDevice) {
        this.numberOfDevice = numberOfDevice;
    }

    public int getNumberOfDevices() {
        return numberOfDevices;
    }

    public void setNumberOfDevices(int numberOfDevices) {
        this.numberOfDevices = numberOfDevices;
    }

    public float getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(float durationMs) {
        this.durationMs = durationMs;
    }
}
