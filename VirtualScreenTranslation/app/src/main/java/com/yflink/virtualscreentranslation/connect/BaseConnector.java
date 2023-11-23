package com.yflink.virtualscreentranslation.connect;

import android.os.Handler;

public abstract class BaseConnector {
    public static final int CONNECT_STATUS_DEFAULT = 0;
    public static final int CONNECT_STATUS_READY = 1;
    private volatile int mCurConnectStatus = CONNECT_STATUS_DEFAULT;
    private ConnectStatusReporter mConnectStatusReporter;
    private Handler mHandler = new Handler();

    public interface ConnectStatusReporter{
        public void onConnectStatus(int connectStatus);
    }

    abstract void start();
    abstract void stop();
    abstract int read(byte[] data, int off, int len);
    abstract void write(byte[] data, int len);
    abstract void closeCurSession();
    void setConnectStatusReporter(ConnectStatusReporter connectStatusReporter){
        mConnectStatusReporter = connectStatusReporter;
    }

    protected void setCurConnectStatus(final int status){
        if(mCurConnectStatus != status) {
            mCurConnectStatus = status;
            if (mConnectStatusReporter != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mConnectStatusReporter.onConnectStatus(status);
                    }
                });
            }
        }
    }

    int getCurConnectStatus(){
        return mCurConnectStatus;
    }

}
