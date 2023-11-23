package com.yflink.virtualscreentranslation.screen;

import android.app.Presentation;
import android.content.Context;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.Surface;
import android.view.ViewGroup;
import android.widget.TextView;


public class AutoDrawProvider extends OriginVideoDataProvider{
    private VirtualDisplay mVirtualDisplay;
    private Handler mHandler = new Handler();
    private TextView mTextView;
    private Runnable mSetTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mTextView != null){
                mTextView.setText(""+ SystemClock.elapsedRealtime());
            }
            mHandler.postDelayed(this, 500);
        }
    };

    public AutoDrawProvider(Context context) {
        super(context);
    }

    @Override
    public void start(Surface inputSurface, int width, int height) {
        DisplayManager dm = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
        mVirtualDisplay = dm.createVirtualDisplay("map", width, height, 160, inputSurface,  DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
                | DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION);

//        //for test
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Presentation presentation = new Presentation(mContext, mVirtualDisplay.getDisplay());
//                mTextView = new TextView(mContext);
//                mTextView.setTextColor(Color.BLUE);
//                mTextView.setTextSize(32);
//                mTextView.setGravity(Gravity.CENTER);
//                mTextView.setText("hello world");
//                ((ViewGroup) presentation.getWindow().getDecorView()).addView(mTextView);
//                presentation.show();
//                mHandler.post(mSetTimeRunnable);
//            }
//        }, 5000);
    }

    @Override
    public void stop() {
        DisplayManager dm = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
    }
}
