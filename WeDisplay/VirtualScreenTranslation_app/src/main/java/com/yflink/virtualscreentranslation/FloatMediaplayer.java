package com.yflink.virtualscreentranslation;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.yflink.virtualscreentranslation.media.MediaPlayer;

import static android.content.Context.WINDOW_SERVICE;

public class FloatMediaplayer {
    private final String TAG = getClass().getSimpleName();

    private WindowManager mWindow;
    private TextureView mSurfaceView;
    private HandlerThread mHandlerThread;
    private Handler workHandler;

    private MediaPlayer mMediaPlayer;
    private final int width = 660;
    private final int height = 335;
    private Surface mSurface;

    public void onOutputFrame(byte[] bytes, final int len) {
        final byte[] temp = new byte[len];
        if(len > 0) {
            System.arraycopy(bytes, 0, temp, 0, len);
        }
        workHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mMediaPlayer != null) {
                    if ((temp[4] & 0x1F) == 7) {
                        mMediaPlayer.startDisplay(mSurface, 850, 610);
                    }
                    mMediaPlayer.play(temp, len);
                }
            }
        });
    }

    public void init(Context context) {
        mWindow = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        mSurfaceView = new TextureView(context);

        mSurfaceView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                Log.i(TAG, "onSurfaceTextureAvailable");
                mSurface = new Surface(surfaceTexture);
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                Log.i(TAG, "onSurfaceTextureDestroyed");
                mMediaPlayer.stop();
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

            }
        });
        mHandlerThread = new HandlerThread("workthread");
        mHandlerThread.start();
        workHandler = new Handler(mHandlerThread.getLooper());
        try {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            ViewGroup.LayoutParams pl = new ViewGroup.LayoutParams(width, height);
            mSurfaceView.setLayoutParams(pl);
            layoutParams.gravity = Gravity.TOP;
            layoutParams.width = width;
            layoutParams.height = height;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            mWindow.addView(mSurfaceView, layoutParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
