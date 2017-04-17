package com.example.android.smarthome;

/**
 * Created by elite on 21/2/2017.
 */

/**
 * Device model.
 */
public class Device {


    private Double mCPUTemperature;
    private boolean mWriteInProgress;
    private String mError;
    private Integer mToggleReleCMD;
    private Integer mRele1status;

    public Device() {
        mCPUTemperature = null;
        mRele1status = null;
        mWriteInProgress = false;
        mToggleReleCMD = null;
        mError = "";
    }

    public Double getCPUTemperature() {
        return mCPUTemperature;
    }
    public void setCPUTemperature(Double temperature) { mCPUTemperature = temperature; }

    public Integer getmRele1status() {
        return mRele1status;
    }
    public void setmRele1status(Integer input) { mRele1status = input; }

    public boolean getWriteInProgress() {
        return mWriteInProgress;
    }
    public void setWriteInProgress(boolean writeInProgress) {
        mWriteInProgress = writeInProgress;
    }

    public String getError() { return mError; }
    public void setError(String error) { mError = error; }

    public Integer getToggleReleCMD() {
        return mToggleReleCMD;
    }
    public void setToggleReleCMD(Integer value) {
        if (!this.getWriteInProgress()) {
            mToggleReleCMD = value;
        }
    }


}