package com.yflink.virtualscreentranslation;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.Nullable;

import com.yflink.virtualscreentranslation.connect.ConnectServer;
import com.yflink.virtualscreentranslation.media.VideoEncoder;
import com.yflink.virtualscreentranslation.screen.AutoDrawProvider;
import com.yflink.virtualscreentranslation.screen.OriginVideoDataProvider;

public class VideoTranslationService extends Service implements ConnectServer.OnConnectedListener, VideoEncoder.OnEncoderEventListener{
    private final String TAG = getClass().getSimpleName();
    private final boolean mIsShowFloatVideoView = false;

    private ConnectServer mConnectServer;
    private VideoEncoder mVideoEncoder;
    private OriginVideoDataProvider mVideoDataProvider;
    private FloatMediaplayer mFloatMediaplayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mConnectServer = new ConnectServer();
        mConnectServer.start();
        mConnectServer.setOnConnectedListener(this);
        mVideoEncoder = new VideoEncoder();
        mVideoEncoder.setOnEncoderEventListener(this);
        mVideoDataProvider = new AutoDrawProvider(this);

        if(mIsShowFloatVideoView) {
            mFloatMediaplayer = new FloatMediaplayer();
            mFloatMediaplayer.init(this);
        }

        //for test
        //mVideoEncoder.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mConnectServer.setOnConnectedListener(null);
        mConnectServer.stop();
        mConnectServer = null;
        super.onDestroy();
    }

    @Override
    public void onConnected(boolean isConnected) {
        if(isConnected){
            mVideoEncoder.start();
        }else{
            mVideoEncoder.stop();
        }
    }

    @Override
    public void onInputSurfaceAvailable(Surface inputSurface) {
        mVideoDataProvider.start(inputSurface, 850, 610);
    }

    @Override
    public void onEncoderBufferAvailable(byte[] data, int len) {
        Log.i(TAG, "onEncoderBufferAvailable len:"+len);
        mConnectServer.writeCommand(0x20003, data, len);
        if(mFloatMediaplayer != null) {
            mFloatMediaplayer.onOutputFrame(data, len);
        }
    }
}
