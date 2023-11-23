package com.ziluone.wedisplay;

import java.io.File;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.baidu.navisdk.adapter.struct.BNTTsInitConfig;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

public class BNInitHelper {
    public static final String TAG = ONApplication.TAG;
    public static final String APP_FOLDER_NAME = ONApplication.APP_FOLDER_NAME;

    private final Context mContext;

    public BNInitHelper(Context context) {
        mContext = context.getApplicationContext();
    }

    public void init() {
        initSdk();
        initNavi();
    }

    public void initSdk() {
        SDKInitializer.initialize(mContext);
        SDKInitializer.setCoordType(CoordType.BD09LL);
    }

    public void initNavi() {
        // 针对单次有效的地图设置项 - DemoNaviSettingActivity
        BNDemoUtils.setBoolean(mContext, BNDemoUtils.KEY_gb_routeSort, true);
        BNDemoUtils.setBoolean(mContext, BNDemoUtils.KEY_gb_routeSearch, true);
        BNDemoUtils.setBoolean(mContext, BNDemoUtils.KEY_gb_moreSettings, true);

        if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
            return;
        }

        BaiduNaviManagerFactory.getBaiduNaviManager().enableOutLog(true);

        BaiduNaviManagerFactory.getBaiduNaviManager().init(mContext,
                mContext.getExternalFilesDir(null).getPath(),
                APP_FOLDER_NAME, new IBaiduNaviManager.INaviInitListener() {

                    @Override
                    public void onAuthResult(int status, String msg) {
                        String result;
                        if (0 == status) {
                            result = "key校验成功!";
                        } else {
                            result = "key校验失败, " + msg;
                        }
                        Log.e(TAG, result);
                    }

                    @Override
                    public void initStart() {
                        Log.e(TAG, "initStart");
                    }

                    @Override
                    public void initSuccess() {
                        Log.e(TAG, "initSuccess");
                        String cuid = BaiduNaviManagerFactory.getBaiduNaviManager().getCUID();
                        Log.e(TAG, "cuid = " + cuid);
                        // 初始化tts
                        initTTS();
                        mContext.sendBroadcast(new Intent("com.navi.ready"));
                    }

                    @Override
                    public void initFailed(int errCode) {
                        Log.e(TAG, "initFailed-" + errCode);
                    }
                });
    }

    private void initTTS() {
        // 使用内置TTS
        BNTTsInitConfig config = new BNTTsInitConfig.Builder()
                .context(mContext)
                .sdcardRootPath(getSdcardDir())
                .appFolderName(APP_FOLDER_NAME)
                .appId(BNDemoUtils.getTTSAppID())
                .appKey(BNDemoUtils.getTTSAppKey())
                .secretKey(BNDemoUtils.getTTSsecretKey())
                .authSn("8092f102-684cde5d-01-0050-006d-0091-01")
                .build();
        BaiduNaviManagerFactory.getTTSManager().initTTS(config);
    }

    private String getSdcardDir() {
        if (Build.VERSION.SDK_INT >= 29) {
            // 如果外部储存可用 ,获得外部存储路径
            File file = mContext.getExternalFilesDir(null);
            if (file != null && file.exists()) {
                return file.getPath();
            } else {
                return mContext.getFilesDir().getPath();
            }
        } else {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
    }

}