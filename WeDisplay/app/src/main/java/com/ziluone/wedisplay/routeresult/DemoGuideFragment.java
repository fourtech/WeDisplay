package com.ziluone.wedisplay.routeresult;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ziluone.wedisplay.activity.DemoDrivingActivity;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNaviListener;
import com.baidu.navisdk.adapter.struct.BNGuideConfig;
import com.baidu.navisdk.adapter.struct.BNHighwayInfo;
import com.baidu.navisdk.adapter.struct.BNRoadCondition;
import com.baidu.navisdk.adapter.struct.BNavLineItem;
import com.baidu.navisdk.adapter.struct.BNaviInfo;
import com.baidu.navisdk.adapter.struct.BNaviLocation;

import java.util.List;

public class DemoGuideFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DemoDrivingActivity) getActivity()).isGuideFragment = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(BNaviCommonParams.ProGuideKey.ADD_MAP, false);
        BNGuideConfig config = new BNGuideConfig.Builder()
                .params(bundle)
                .build();
        initListener();

        return BaiduNaviManagerFactory.getRouteGuideManager().onCreate(getActivity(), config);
    }

    private void initListener() {
        BaiduNaviManagerFactory.getRouteGuideManager().setNaviListener(new IBNaviListener() {
            @Override
            public void onRoadNameUpdate(String name) {

            }

            @Override
            public void onRemainInfoUpdate(int remainDistance, int remainTime) {

            }

            @Override
            public void onViaListRemainInfoUpdate(Message msg) {

            }

            @Override
            public void onGuideInfoUpdate(BNaviInfo naviInfo) {

            }

            @Override
            public void onHighWayInfoUpdate(Action action, BNHighwayInfo info) {

            }

            @Override
            public void onFastExitWayInfoUpdate(Action action, String name, int dist, String id) {

            }

            @Override
            public void onEnlargeMapUpdate(Action action, View enlargeMap, String remainDistance,
                                           int progress, String roadName, Bitmap turnIcon) {

            }

            @Override
            public void onDayNightChanged(DayNightMode style) {

            }

            @Override
            public void onRoadConditionInfoUpdate(double progress, List<BNRoadCondition> items) {

            }

            @Override
            public void onMainSideBridgeUpdate(int type) {

            }

            @Override
            public void onLaneInfoUpdate(Action action, List<BNavLineItem> laneItems) {

            }

            @Override
            public void onSpeedUpdate(int speed, int overSpeed) {

            }

            @Override
            public void onOverSpeed(int speed, int speedLimit) {

            }

            @Override
            public void onArriveDestination() {

            }

            @Override
            public void onArrivedWayPoint(int index) {

            }

            @Override
            public void onLocationChange(BNaviLocation naviLocation) {

            }

            @Override
            public void onMapStateChange(MapStateMode mapStateMode) {

            }

            @Override
            public void onStartYawing(String flag) {

            }

            @Override
            public void onYawingSuccess() {

            }

            @Override
            public void onYawingArriveViaPoint(int index) {

            }

            @Override
            public void onNotificationShow(String msg) {

            }

            @Override
            public void onHeavyTraffic() {

            }

            @Override
            public void onNaviGuideEnd() {
                getActivity().getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onPreferChanged(int preferType) {

            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        BaiduNaviManagerFactory.getRouteGuideManager().onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BaiduNaviManagerFactory.getRouteGuideManager().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        BaiduNaviManagerFactory.getRouteGuideManager().onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        BaiduNaviManagerFactory.getRouteGuideManager().onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        BaiduNaviManagerFactory.getRouteGuideManager().onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        BaiduNaviManagerFactory.getRouteGuideManager().onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((DemoDrivingActivity) getActivity()).isGuideFragment = false;
        BaiduNaviManagerFactory.getRouteGuideManager().onDestroy(false);
    }
}
