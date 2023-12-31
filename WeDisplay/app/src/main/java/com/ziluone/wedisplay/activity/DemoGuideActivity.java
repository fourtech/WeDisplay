/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.ziluone.wedisplay.activity;

import java.util.List;

import com.ziluone.wedisplay.controlwindow.ControlBoardWindow;
import com.ziluone.wedisplay.controlwindow.GuidePop;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRouteGuideManager;
import com.baidu.navisdk.adapter.IBNTTSManager;
import com.baidu.navisdk.adapter.IBNaviListener;
import com.baidu.navisdk.adapter.IBNaviViewListener;
import com.baidu.navisdk.adapter.struct.BNGuideConfig;
import com.baidu.navisdk.adapter.struct.BNHighwayInfo;
import com.baidu.navisdk.adapter.struct.BNRoadCondition;
import com.baidu.navisdk.adapter.struct.BNavLineItem;
import com.baidu.navisdk.adapter.struct.BNaviInfo;
import com.baidu.navisdk.adapter.struct.BNaviLocation;
import com.baidu.navisdk.adapter.struct.BNaviResultInfo;
import com.baidu.navisdk.adapter.struct.RoadEventItem;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;

/**
 * 诱导界面
 */
public class DemoGuideActivity extends FragmentActivity {

    private static final String TAG = DemoGuideActivity.class.getName();

    private IBNRouteGuideManager mRouteGuideManager;
    private IBNaviListener.DayNightMode mMode = IBNaviListener.DayNightMode.DAY;
    private int curRemainDistance;
    private int overSpeedDistance = -1;
    View view;
    GuidePop guidePop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean fullScreen = false; // supportFullScreen();

        /**
         * 包含是否开启新能源模式信息 BNPageConst.SubVehicle.NEW_ENERGY
         *
         */
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        Bundle params = new Bundle();
        if (extras != null) {
            params.putAll(extras);
        }

        params.putBoolean(BNaviCommonParams.ProGuideKey.IS_SUPPORT_FULL_SCREEN, fullScreen);
        mRouteGuideManager = BaiduNaviManagerFactory.getRouteGuideManager();
        BNGuideConfig config = new BNGuideConfig.Builder()
                .params(params)
                .build();
        view = mRouteGuideManager.onCreate(this, config);

        if (view != null) {
            setContentView(view);
        }
        guidePop = new GuidePop(this);
        initTTSListener();
        routeGuideEvent();
    }

    // 导航过程事件监听
    private void routeGuideEvent() {
        BaiduNaviManagerFactory.getRouteGuideManager().setNaviListener(new IBNaviListener() {

            @Override
            public void onRoadNameUpdate(String name) { // 弹窗展示
                ControlBoardWindow.getInstance().showControl("当前路名更新——" + name);
                guidePop.setdata(name);
            }

            @Override
            public void onRemainInfoUpdate(int remainDistance, int remainTime) { // 弹窗展示
                ControlBoardWindow.getInstance()
                        .showControl("距离目的地的剩余——" + remainDistance + "m " + remainTime + "s");
                guidePop.setdata(remainDistance, remainTime);
                DemoGuideActivity.this.curRemainDistance = remainDistance;
            }

            @Override
            public void onViaListRemainInfoUpdate(Message msg) {

            }

            @Override
            public void onGuideInfoUpdate(BNaviInfo naviInfo) { // 弹窗展示
                ControlBoardWindow.getInstance().showControl("诱导信息——" + naviInfo.toString());
                guidePop.setdata(naviInfo);
            }

            @Override
            public void onHighWayInfoUpdate(Action action, BNHighwayInfo info) {
                ControlBoardWindow.getInstance()
                        .showControl("高速信息——" + action + " " + info.toString());
            }

            @Override
            public void onFastExitWayInfoUpdate(Action action, String name, int dist, String id) {
                ControlBoardWindow.getInstance()
                        .showControl("快速路出口信息——" + action + " " + name + " 出口还有" + dist + "米");
            }

            @Override
            public void onEnlargeMapUpdate(Action action, View enlargeMap, String remainDistance,
                                           int progress, String roadName, Bitmap turnIcon) {
                ControlBoardWindow.getInstance().showControl("放大图回调信息");
            }

            @Override
            public void onDayNightChanged(DayNightMode style) {
                ControlBoardWindow.getInstance().showControl("日夜更替信息回调" + style);
            }

            @Override
            public void onRoadConditionInfoUpdate(double progress, List<BNRoadCondition> items) {
                ControlBoardWindow.getInstance().showControl("路况信息更新 进度：" + progress + " 路况：。。。");
            }

            @Override
            public void onMainSideBridgeUpdate(int type) {
                ControlBoardWindow.getInstance().showControl("主辅路、高架桥信息更新:" + type + " 意义不明？？—gb");
            }

            @Override
            public void onLaneInfoUpdate(Action action, List<BNavLineItem> laneItems) {
                if (laneItems != null && laneItems.size() > 0) {
                    ControlBoardWindow.getInstance().showControl("车道线信息更新:" +
                            action + laneItems.get(0).toString() + " ...");
                }
            }

            @Override
            public void onSpeedUpdate(int speed, int overSpeed) {

//                ControlBoardWindow.getInstance()
//                        .showControl("当前车速：" + speed + "限速：" + overSpeed + " 是否超速：" + (speed > overSpeed) );
//                if (overSpeed == -1) {
//                    return;
//                }
//                if (speed > overSpeed && ( (overSpeedDistance == -1 || overSpeedDistance - curRemainDistance > 1000) ) ) {
//                    // 1km内超速回调一次，偏航重置
//                    ControlBoardWindow.getInstance()
//                            .showControl("超速回调！！！");
//                    Toast.makeText(DemoGuideActivity.this, "超速回调！！！",
//                            Toast.LENGTH_SHORT).show();
//                    overSpeedDistance = curRemainDistance;
//                }
            }

            @Override
            public void onOverSpeed(int speed, int speedLimit) {
                Toast.makeText(DemoGuideActivity.this, "超速回调！！！",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onArriveDestination() {
                BNaviResultInfo info =
                        BaiduNaviManagerFactory.getRouteGuideManager().getNaviResultInfo();
                Toast.makeText(DemoGuideActivity.this, "导航结算数据: " + info.toString(),
                        Toast.LENGTH_SHORT).show();
                ControlBoardWindow.getInstance().showControl("抵达目的地：" + info.toString());
            }

            @Override
            public void onArrivedWayPoint(int index) {
                ControlBoardWindow.getInstance().showControl("到达途径点——" + index);
            }

            @Override
            public void onLocationChange(BNaviLocation naviLocation) {
                ControlBoardWindow.getInstance().showControl("GPS位置有更新时的回调:");
            }

            @Override
            public void onMapStateChange(MapStateMode mapStateMode) {
                ControlBoardWindow.getInstance()
                        .showControlOnlyone("底图操作态和导航态的回调:", "onMapStateChange");
            }

            @Override
            public void onStartYawing(String flag) {
                overSpeedDistance = -1;
                // Toast.makeText(DemoGuideActivity.this, flag + "", Toast.LENGTH_SHORT).show();
                ControlBoardWindow.getInstance().showControl("开始偏航的回调");
                List<RoadEventItem> roadEvents = BaiduNaviManagerFactory.getRouteGuideManager()
                        .getForwardRoadEvent();
                Log.i(TAG, "onStartYawing==");
                if (roadEvents != null && roadEvents.size() > 0) {
                    for (int i = 0; i < roadEvents.size(); i++) {
                        RoadEventItem roadEventItem = roadEvents.get(i);
                        Log.i(TAG, "roadEvent eventType==" + roadEventItem.roadEventType +
                                ", eventTypeStr=" + roadEventItem.roadEventTypeStr +
                                ", longitude=" + roadEventItem.longitude + ", latitude=" + roadEventItem.latitude);
                    }
                }
            }

            @Override
            public void onYawingSuccess() {
                ControlBoardWindow.getInstance().showControl("偏航成功的回调");
            }

            @Override
            public void onYawingArriveViaPoint(int index) {

            }

            @Override
            public void onNotificationShow(String msg) {
                Log.e(TAG, msg);
                ControlBoardWindow.getInstance().showControl("导航中通知型消息的回调" + msg);
            }

            @Override
            public void onHeavyTraffic() {
                Log.e(TAG, "onHeavyTraffic");
                ControlBoardWindow.getInstance().showControl("导航中前方一公里出现严重拥堵的回调");
            }

            @Override
            public void onNaviGuideEnd() {
                DemoGuideActivity.this.finish();
            }

            @Override
            public void onPreferChanged(int preferType) {

            }
        });

        BaiduNaviManagerFactory.getRouteGuideManager().setNaviViewListener(
                new IBNaviViewListener() {
                    @Override
                    public void onMainInfoPanCLick() {
                        ControlBoardWindow.getInstance().showControl("诱导面板的点击");
                        guidePop.showAtLocation(view,
                                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                    }

                    @Override
                    public void onNaviTurnClick() {
                        ControlBoardWindow.getInstance().showControl("界面左上角转向操作的点击");
                    }

                    @Override
                    public void onFullViewButtonClick(boolean show) {
                        ControlBoardWindow.getInstance().showControl("全览按钮的点击");
                    }

                    @Override
                    public void onFullViewWindowClick(boolean show) {
                        ControlBoardWindow.getInstance().showControl("全览小窗口的点击");
                    }

                    @Override
                    public void onNaviBackClick() {
                        Log.e(TAG, "onNaviBackClick");
                        ControlBoardWindow.getInstance().showControl("导航页面左下角退出按钮点击");
                    }

                    @Override
                    public void onBottomBarClick(Action action) {
                        ControlBoardWindow.getInstance().showControl("底部中间部分点击");
                    }

                    @Override
                    public void onNaviSettingClick() {
                        Log.e(TAG, "onNaviSettingClick");
                        ControlBoardWindow.getInstance().showControl("底部右边更多设置按钮点击");
                    }

                    @Override
                    public void onRefreshBtnClick() {
                        ControlBoardWindow.getInstance().showControl("刷新按钮");
                    }

                    @Override
                    public void onZoomLevelChange(int level) {
                        ControlBoardWindow.getInstance().showControl("地图缩放等级:" + level);
                    }

                    @Override
                    public void onMapClicked(double x, double y) {
                        ControlBoardWindow.getInstance().showControl("地图点击的回调(国测局GCJ02坐标):x="
                                + x + " y=" + y, "onMapClicked");
                    }

                    @Override
                    public void onMapMoved() {
                        Log.e(TAG, "onMapMoved");
                        ControlBoardWindow.getInstance().showControlOnlyone("移动地图的回调", "onMapMoved");
                    }

                    @Override
                    public void onFloatViewClicked() {
                        ControlBoardWindow.getInstance().showControl("后台诱导悬浮窗的点击");
                        try {
                            Intent intent = new Intent();
                            intent.setPackage(getPackageName());
                            intent.setClass(DemoGuideActivity.this,
                                    Class.forName(DemoGuideActivity.class.getName()));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                            startActivity(intent);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void initTTSListener() {
        // 注册同步内置tts状态回调
        BaiduNaviManagerFactory.getTTSManager().setOnTTSStateChangedListener(
                new IBNTTSManager.IOnTTSPlayStateChangedListener() {
                    @Override
                    public void onPlayStart() {
                        Log.e("BNSDKDemo", "ttsCallback.onPlayStart");
                        ControlBoardWindow.getInstance().showControl("ttsCallback.onPlayStart");
                    }

                    @Override
                    public void onPlayEnd(String speechId) {
                        Log.e("BNSDKDemo", "ttsCallback.onPlayEnd");
                        ControlBoardWindow.getInstance().showControl("ttsCallback.onPlayEnd");
                    }

                    @Override
                    public void onPlayError(int code, String message) {
                        Log.e("BNSDKDemo", "ttsCallback.onPlayError");
                        ControlBoardWindow.getInstance().showControl("ttsCallback.onPlayError");
                    }
                }
        );

        // 注册内置tts 异步状态消息
        BaiduNaviManagerFactory.getTTSManager().setOnTTSStateChangedHandler(
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        Log.e("BNSDKDemo", "ttsHandler.msg.what=" + msg.what);
                        ControlBoardWindow.getInstance()
                                .showControl("ttsHandler.msg.what=" + msg.what);
                    }
                }
        );
    }

    private void unInitTTSListener() {
        BaiduNaviManagerFactory.getTTSManager().setOnTTSStateChangedListener(null);
        BaiduNaviManagerFactory.getTTSManager().setOnTTSStateChangedHandler(null);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mRouteGuideManager.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mRouteGuideManager.onResume();
    }

    protected void onPause() {
        super.onPause();
        mRouteGuideManager.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRouteGuideManager.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRouteGuideManager.onDestroy(false);
        unInitTTSListener();
        mRouteGuideManager = null;
//        BaiduNaviManagerFactory.getRouteResultManager().onDestroy();
    }

    @Override
    public void onBackPressed() {
        mRouteGuideManager.onBackPressed(false, true);
    }

    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mRouteGuideManager.onConfigurationChanged(newConfig);
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {

    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (!mRouteGuideManager.onKeyDown(keyCode, event)) {
            return super.onKeyDown(keyCode, event);
        }
        return true;

    }

    private boolean supportFullScreen() {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            int color;
            if (Build.VERSION.SDK_INT >= 23) {
                color = Color.TRANSPARENT;
            } else {
                color = 0x2d000000;
            }
            window.setStatusBarColor(color);

            if (Build.VERSION.SDK_INT >= 23) {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                int uiVisibility = window.getDecorView().getSystemUiVisibility();
                if (mMode == IBNaviListener.DayNightMode.DAY) {
                    uiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                window.getDecorView().setSystemUiVisibility(uiVisibility);
            } else {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            return true;
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mRouteGuideManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mRouteGuideManager.onActivityResult(requestCode, resultCode, data);
    }
}
