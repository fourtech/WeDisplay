package com.baidu.mapclient.liteapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.ViewGroup;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapclient.liteapp.controlwindow.MapAddviewPop;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.map.BNItemOverlay;

/**
 * Author: v_duanpeifeng
 * Time: 2020-01-16
 * Description:
 */
public class DemoDrawRectActivity extends Activity {

    private BNItemOverlay mItemOverlay;
    MapView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = BaiduNaviManagerFactory.getMapManager().getMapView();
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeAllViews();
        }
        setContentView(view);
        initAddview();
    }

    private void initAddview() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MapAddviewPop pop = new MapAddviewPop(DemoDrawRectActivity.this, view);
                pop.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            }
        }, 1000);

    }

    @Override
    protected void onResume() {
        super.onResume();
        BaiduNaviManagerFactory.getMapManager().onResume();
        // 自定义图层
        // showOverlay();
        initOverlayEvent();
    }

    private void initOverlayEvent() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        BaiduNaviManagerFactory.getMapManager().onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
