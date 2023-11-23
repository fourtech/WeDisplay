/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.ziluone.wedisplay.fragment;

import com.ziluone.wedisplay.R;
import com.ziluone.wedisplay.activity.BNDemoLightNaviActivity;
import com.ziluone.wedisplay.listener.BNDemoNaviViewListener;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRouteGuideManager;
import com.baidu.navisdk.adapter.IBNaviListener;
import com.baidu.navisdk.adapter.struct.BNGuideConfig;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class BNProNaviFragment extends BNBaseFragment {

    private static final String TAG = "BNProNaviFragment";

    private View contentView = null;

    private IBNaviListener.DayNightMode mMode = IBNaviListener.DayNightMode.DAY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.onsdk_fragment_pro_light_navi, container, false);
        initView();
        initListener();
        return contentView;
    }

    private void initListener() {
        BaiduNaviManagerFactory.getRouteGuideManager().setNaviListener(new IBNaviListener() {
            @Override
            public void onStartYawing(String flag) {

            }

            @Override
            public void onNaviGuideEnd() {
                // 退出专业导航之后重新开启轻导航
                BaiduNaviManagerFactory.getLightNaviManager().startLightNavi();
                if (getActivity() != null) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    ((BNDemoLightNaviActivity) getActivity()).goBack();
                }
            }
        });

        BaiduNaviManagerFactory.getRouteGuideManager()
                .setNaviViewListener(new BNDemoNaviViewListener());
    }

    private void initView() {
        FrameLayout contentContainer = contentView.findViewById(R.id.content_container);
        Bundle bundle = new Bundle();
        bundle.putBoolean(BNaviCommonParams.ProGuideKey.ADD_MAP, false);
        bundle.putBoolean(BNaviCommonParams.ProGuideKey.IS_SUPPORT_FULL_SCREEN, true);
        BNGuideConfig config = new BNGuideConfig.Builder()
                .addBottomViewCallback(new IBNRouteGuideManager.NaviAddViewCallback() {

                    @Override
                    public int getViewHeight() {
                        return 150;
                    }

                    @Override
                    public View getAddedView() {
                        TextView textView = new Button(getActivity());
                        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.height = 150;
                        textView.setText("这是个自定义view");
                        textView.setGravity(Gravity.CENTER);
                        textView.setLayoutParams(layoutParams);
                        textView.setBackground(getActivity().getResources()
                                .getDrawable(R.drawable.bnav_setting_btn_bg_selector));
                        return textView;
                    }
                })
                .params(bundle)
                .build();
        View view = BaiduNaviManagerFactory.getRouteGuideManager().onCreate(getActivity(), config);
        if (view != null) {
            if (view.getParent() != null) {
                ViewGroup parent = (ViewGroup) view.getParent();
                parent.removeView(view);
            }
            contentContainer.addView(view);
        }
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
        BaiduNaviManagerFactory.getRouteGuideManager().onDestroy(true);
    }

    @Override
    public void goBack() {
        ((BNDemoLightNaviActivity) getActivity()).finish();
    }
}
