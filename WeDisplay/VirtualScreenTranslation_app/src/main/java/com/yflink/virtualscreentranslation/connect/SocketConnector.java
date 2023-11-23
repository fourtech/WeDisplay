package com.yflink.virtualscreentranslation.connect;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketConnector extends BaseConnector {
    public final String TAG  = getClass().getSimpleName();
    private Thread mConnectThread;
    private ServerSocket mServerSocket;
    private Socket mSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    private class ConnectThread  extends Thread{
        public ConnectThread(){
            super("Connect");
        }
        @Override
        public void run() {
            try {
                mServerSocket = new ServerSocket(5300);
            }catch (IOException e){
                e.printStackTrace();
                Log.e(TAG, "create serverSocket exception : " + e.getMessage());
                return;
            }

            while (true){
                try {
                    Socket socket = mServerSocket.accept();
                    Log.i(TAG, "onNew Socket: " + socket.getRemoteSocketAddress().toString());
                    closeCurSession();
                    mSocket = socket;
                    mSocket.setTcpNoDelay(true);
                    mSocket.setSoTimeout(6000);
                    mInputStream = mSocket.getInputStream();
                    mOutputStream = mSocket.getOutputStream();
                    setCurConnectStatus(CONNECT_STATUS_READY);
                } catch (Exception e) {
                    Log.e(TAG, "mServerSocket.accept() exception : " + e.getMessage());
                }
            }
        }
    };

    @Override
    public void start() {
        mConnectThread = new ConnectThread();
        mConnectThread.start();
    }

    @Override
    public void stop() {
        try {
            closeCurSession();
            if(mServerSocket != null) {
                mServerSocket.close();
                mServerSocket = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "stop exception : " + e.getMessage());
        }
    }

    @Override
    int read(byte[] data, int off, int len) {
        try {
            return mInputStream.read(data, off, len);
        } catch (Exception e) {
            Log.e(TAG, "read exception : " + e.getMessage());
            setCurConnectStatus(CONNECT_STATUS_DEFAULT);
            return -1;
        }
    }

    @Override
    public void write(byte[] data, int len) {
        try {
            mOutputStream.write(data, 0, len);
        } catch (Exception e) {
            Log.e(TAG, "write exception : " + e.getMessage());
            setCurConnectStatus(CONNECT_STATUS_DEFAULT);
        }
    }

    @Override
    public void closeCurSession() {
        try {
            if (mInputStream != null) {
                mInputStream.close();
                mInputStream = null;
            }
            if (mOutputStream != null) {
                mOutputStream.close();
                mOutputStream = null;
            }
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
        }catch (Exception e) {
            Log.e(TAG, "closeCurSession exception : " + e.getMessage());
        }
        Log.i(TAG, "closeCurSession");
        setCurConnectStatus(CONNECT_STATUS_DEFAULT);
    }
}
