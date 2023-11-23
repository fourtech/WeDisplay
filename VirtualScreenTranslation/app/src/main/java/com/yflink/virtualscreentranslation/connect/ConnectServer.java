package com.yflink.virtualscreentranslation.connect;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import com.yflink.virtualscreentranslation.media.VideoEncoder;


public class ConnectServer implements BaseConnector.ConnectStatusReporter, Runnable{
    public final String TAG = getClass().getSimpleName();
    private BaseConnector mConnector;
    private volatile boolean mIsConnected = false;
    private final boolean mDumpVideo = false;

    private Handler mMainHandler = new Handler();
    private HandlerThread mWriteCommandThread;
    private Handler mWriteCommandHandler;
    private Thread mReadCommandThread;

    private OnConnectedListener mOnConnectedListener;
    public interface OnConnectedListener{
        void onConnected(boolean isConnected);
    }

    public void setOnConnectedListener(OnConnectedListener listener){
        mOnConnectedListener = listener;
    }

    public boolean isConnected(){
        return mIsConnected;
    }

    public ConnectServer(){
        mConnector = new SocketConnector();
    }

    public void start(){
        Log.i(TAG,"start");
        mConnector.start();
        mConnector.setConnectStatusReporter(this);
    }

    public void stop(){
        Log.i(TAG,"stop");
        mConnector.stop();
        mConnector.setConnectStatusReporter(null);
    }

    @Override
    public void onConnectStatus(int connectStatus) {
        Log.i(TAG, "onConnectStatus connectStatus:"+connectStatus);
        if(connectStatus == BaseConnector.CONNECT_STATUS_READY){
            mWriteCommandThread = new HandlerThread("writeThread", Process.THREAD_PRIORITY_VIDEO);
            mWriteCommandThread.start();
            mWriteCommandHandler = new Handler(mWriteCommandThread.getLooper());
            mReadCommandThread = new Thread( this, "readThread");
            mReadCommandThread.start();
        }else{
            mWriteCommandThread.quit();
            mConnector.closeCurSession();
            if (mOnConnectedListener != null) {
                mOnConnectedListener.onConnected(false);
            }
        }
    }

    public boolean isReady(){
        return mIsConnected;
    }

    public void writeCommand(final int command, final byte[] data, final int len){
        if(!isReady()){
            Log.i(TAG, "writeCommand failed command:0x"+Integer.toHexString(command)+", len:"+len);
            return;
        }
        Log.i(TAG, "writeCommand  command:0x"+Integer.toHexString(command)+", len:"+len);
        final byte[] temp = new byte[len];
        if(len > 0) {
            System.arraycopy(data, 0, temp, 0, len);
        }
        mWriteCommandHandler.post(new Runnable() {
            @Override
            public void run() {
                byte[] head = new byte[8];
                head[0] = (byte) ((command >> 24) & 0xff);
                head[1] = (byte) ((command >> 16) & 0xff);
                head[2] = (byte) ((command >> 8) & 0xff);
                head[3] = (byte) ((command) & 0xff);
                head[4] = (byte) ((len >> 24) & 0xff);
                head[5] = (byte) ((len >> 16) & 0xff);
                head[6] = (byte) ((len >> 8) & 0xff);
                head[7] = (byte) ((len) & 0xff);
                mConnector.write(head, 8);
                if(len > 0) {
                    mConnector.write(temp, len);
                    if(mDumpVideo && command == 0x20003){
                        VideoEncoder.createFileFromBytes(temp, len, "/data/screen.h264", false);
                    }
                }
            }
        });
    }

    private int readData(byte[] data, int len){
        int cnt = 0;
        while (len > cnt) {
            int l = mConnector.read(data, cnt, len);
            if (l > 0) {
                cnt += l;
            } else {
                return -1;
            }
        }

        return len;
    }

    private Runnable mHeartBeatRunnable= new Runnable() {
        @Override
        public void run() {
            writeCommand(0x10003, null, 0);
            mMainHandler.postDelayed(this, 1000);
        }
    };

    private Runnable mCheckHeartBeatTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "mCheckHeartBeat timeout");
            notifyConnected(false);
        }
    };

    private void startHeartBeat(){
        mMainHandler.postDelayed(mHeartBeatRunnable, 1000);
    }

    private void stopHeartBeat(){
        mMainHandler.removeCallbacks(mHeartBeatRunnable);
        mMainHandler.removeCallbacks(mCheckHeartBeatTimeOutRunnable);
    }

    private void checkHeartBeatTimeOut(){
        mMainHandler.removeCallbacks(mCheckHeartBeatTimeOutRunnable);
        mMainHandler.postDelayed(mCheckHeartBeatTimeOutRunnable, 5000);
    }

    private void notifyConnected(final boolean isConnected){
        Log.i(TAG, "notifyConnected:"+isConnected+", mIsConnected:"+mIsConnected);
        if(mIsConnected != isConnected) {
            mIsConnected = isConnected;
            if(mIsConnected){
                VideoEncoder.createFileFromBytes(null, 0, "/data/screen.h264", true);
                startHeartBeat();
            }else{
                stopHeartBeat();
            }
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mOnConnectedListener != null) {
                        mOnConnectedListener.onConnected(isConnected);
                    }
                }
            });
        }
    }

    @Override
    public void run() {
        Log.i(TAG, "read  thread start");
        while(mConnector.getCurConnectStatus()==BaseConnector.CONNECT_STATUS_READY){
            byte[] head = new byte[8];
            if (-1 == readData(head, 8)) {
                Log.i(TAG, "readData exception");
                break;
            }
            int command = ((head[0] & 0xff) << 24) | ((head[1] & 0xff) << 16) | ((head[2] & 0xff) << 8) | (head[3] & 0xff);
            int len = ((head[4] & 0xff) << 24) | ((head[5] & 0xff) << 16) | ((head[6] & 0xff) << 8) | (head[7] & 0xff);
            byte[] data = null;
            if (len > 0) {
                data = new byte[len];
                if (-1 == readData(data, len)) {
                    Log.i(TAG, "readData exception");
                    break;
                }
            }
            Log.i(TAG, "read command:0x"+Integer.toHexString(command)+", len:"+len);
            if(command == 0x10001){
                notifyConnected(true);
            }else if(command == 0x10003){
                checkHeartBeatTimeOut();
            }
        }
        notifyConnected(false);
        Log.i(TAG, "read thread exit");
    }
}
