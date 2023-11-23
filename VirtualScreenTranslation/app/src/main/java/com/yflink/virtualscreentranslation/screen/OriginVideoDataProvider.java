package com.yflink.virtualscreentranslation.screen;

import android.content.Context;
import android.view.Surface;

public abstract class OriginVideoDataProvider {
    protected Context mContext;

    protected OriginVideoDataProvider(Context mContext) {
        this.mContext = mContext;
    }

    public abstract void start(Surface inputSurface, int width, int height);


    public abstract void stop();
}
