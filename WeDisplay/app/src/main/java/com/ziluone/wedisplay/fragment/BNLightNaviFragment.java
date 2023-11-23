/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.ziluone.wedisplay.fragment;

import java.util.ArrayList;
import java.util.List;

import com.ziluone.wedisplay.BNDemoFactory;
import com.ziluone.wedisplay.R;
import com.ziluone.wedisplay.activity.BNDemoLightNaviActivity;
import com.ziluone.wedisplay.controlwindow.ControlBoardWindow;
import com.baidu.navisdk.adapter.BNLightNaviListener;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNLightNaviManager;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.struct.BNRoutePlanInfos;
import com.baidu.navisdk.adapter.struct.BNRoutePlanItem;
import com.baidu.navisdk.adapter.struct.BNaviInfo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

/**
 * 轻导航首页及路线页（BNRouteResultFragment）均在轻导航中
 * 轻导航显示单路线，路线页显示多路线
 */
public class BNLightNaviFragment extends BNBaseFragment {

    private View mContentView = null;
    private Button newEnergyBtn;
    private boolean isNewEnergyBtn = false;

    private boolean mIsSwitchToProNavi = false;

    // 轻导航控制器，控制器为单例
    private final IBNLightNaviManager mLightNaviManager = BaiduNaviManagerFactory.getLightNaviManager();
    // 路线控制器，控制器为单例
    private final IBNRoutePlanManager mRoutePlanManager = BaiduNaviManagerFactory.getRoutePlanManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.onsdk_fragment_light_navi, container, false);
        // 返回的View是一个空的FrameLayout，可以不用装载.
        mLightNaviManager.onCreate(getActivity());
        initView();
        initLightNavi();

        // 这里主要是恢复轻导航，业务逻辑应该发生在从其他页面回到轻导航中，如果还没进行算路，可以不用调用里面的逻辑
        restoreLightNavi();
        return mContentView;
    }

    public void initView() {
        mContentView.findViewById(R.id.pronavi).setOnClickListener(onClickListener);
        mContentView.findViewById(R.id.route_result).setOnClickListener(onClickListener);
        mContentView.findViewById(R.id.truck_route_plan).setOnClickListener(onClickListener);
        mContentView.findViewById(R.id.route_plan).setOnClickListener(onClickListener);
        newEnergyBtn = mContentView.findViewById(R.id.new_energy);
        newEnergyBtn.setOnClickListener(onClickListener);
        newEnergyBtn.setSelected(isNewEnergyBtn);
    }

    public void refreshGuideView(BNaviInfo info) {
        ((TextView) mContentView.findViewById(R.id.distance)).setText(info.distance + "米");
        ((TextView) mContentView.findViewById(R.id.route_name)).setText(info.roadName);
        ((ImageView) mContentView.findViewById(R.id.turnIcon)).setImageBitmap(info.turnIcon);
    }

    public void refreshTopView(BNRoutePlanItem infos, int remainDistance, int remainTime) {
        if (infos != null) {
            ((TextView) mContentView.findViewById(R.id.all_distance))
                    .setText("全程：" + infos.getLengthStr());
            ((TextView) mContentView.findViewById(R.id.all_time))
                    .setText("全程：" + infos.getPassTimeStr());
            ((TextView) mContentView.findViewById(R.id.lights))
                    .setText("红绿灯：" + infos.getLights());
        }

        if (remainDistance > 0 && remainTime > 0) {
            ((TextView) mContentView.findViewById(R.id.remain_distance)).setText("剩余：" + remainDistance + "米");
            ((TextView) mContentView.findViewById(R.id.remain_time)).setText("剩余：" + remainTime + "秒");
        }
    }

    // 这里主要是恢复的逻辑，比如从专业导航回轻导航，需要重新开启轻导航
    private void restoreLightNavi() {
        // 获取路线信息面板展示
        BNRoutePlanInfos routePlaneInfos = mRoutePlanManager.getRoutePlanInfo();
        if (routePlaneInfos != null) {
            refreshTopView(routePlaneInfos.getRouteTabInfos().get(0), -1, -1);
        }
    }

    private void initLightNavi() {
        // 关闭提示音
        BaiduNaviManagerFactory.getLightNaviSettingManager().setLightQuiet(true);
        // 设置路线距离屏幕的padding值，单位为像素
        BaiduNaviManagerFactory.getLightNaviSettingManager().setRouteMargin(100, 200, 100, 200);
        // 显示单路线轻导航
        BaiduNaviManagerFactory.getCommonSettingManager().setMultiRouteEnable(false);
        // 设置轻导航导航监听
        mLightNaviManager.setLightNaviListener(
                new BNLightNaviListener() {
                    @Override
                    public void onRoadNameUpdate(String name) {
                        ControlBoardWindow.getInstance().showControlTop("当前路名：" + name, "onRoadNameUpdate");
                    }

                    @Override
                    public void updateGuideInfo(BNaviInfo info) {
                        refreshGuideView(info);
                    }

                    @Override
                    public void onRemainInfoUpdate(int remainDistance, int remainTime) {
                        refreshTopView(null, remainDistance, remainTime);
                        ControlBoardWindow.getInstance().showControlTop("剩余距离：" + remainDistance
                                + "m 剩余时间：" + remainTime + "s", "onRemainInfoUpdate");
                    }

                    @Override
                    public void onMainRouteChanged() {
                        // 主线变化时，获取当前的路线数据
                        BNRoutePlanInfos infos = mRoutePlanManager.getRoutePlanInfo();
                        if (infos.getRouteTabInfos() != null) {
                            // 注意！这里需要使用算路模块的RouteId，而不是轻导航模块的RouteIndex.
                            int currentSelectId = mRoutePlanManager.getSelectRouteId();
                            // 需要判断一下路线size，防止数组越界;
                            if (currentSelectId < infos.getRouteTabInfos().size()) {
                                refreshTopView(infos.getRouteTabInfos().get(currentSelectId), -1, -1);
                            }
                        }
                    }

                    @Override
                    public void onStartYawing() {
                        ControlBoardWindow.getInstance().showControl("偏航");
                    }

                    @Override
                    public void onArriveDest() {
                        Toast.makeText(getActivity(), "到达目的地", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mLightNaviManager.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mLightNaviManager.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mLightNaviManager.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mLightNaviManager.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLightNaviManager.onDestroy(mIsSwitchToProNavi);
        // 显示多路线
        BaiduNaviManagerFactory.getCommonSettingManager().setMultiRouteEnable(true);
    }

    @Override
    public void goBack() {
        // 退出页面停止轻导航
        // 如果退出页面还想后台导航，该接口可以不用调用。
        mLightNaviManager.stopLightNavi(false);
    }

    /**
     * 货车 - 汽车 算路切换时 务必 退出轻导航（stopLightNavi），算路成功后重启（startLightNavi）
     * 否则影响多路线显示
     * 货车路线页算路只有一条路线，汽车有多条
     * Tip1:stopLightNavi 后算路失败则无路线显示
     *
     * @param value
     */
    public void routePlan(int value) {
        mLightNaviManager.stopLightNavi(false);
        List<BNRoutePlanNode> list = new ArrayList<>();
        BNRoutePlanNode startNode = BNDemoFactory.getInstance().getCurrentNode(getContext(), 0);
        if (startNode == null) {
            startNode = BNDemoFactory.getInstance().getStartNode(getContext());
        }
        list.add(startNode);
        list.add(BNDemoFactory.getInstance().getEndNode(getContext()));

        Bundle bundle = new Bundle();
        bundle.putInt(BNaviCommonParams.RoutePlanKey.VEHICLE_TYPE, value);
        mRoutePlanManager.routePlan(list,
                IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT,
                bundle, handler);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.pronavi:
                    if (mLightNaviManager.startProfessionalNavi()) {
                        mIsSwitchToProNavi = true;
                        mLightNaviManager.stopLightNavi(true);
                        // 开启多路线
                        BaiduNaviManagerFactory.getCommonSettingManager().setMultiRouteEnable(true);
                        ((BNDemoLightNaviActivity) getActivity()).jumpTo("BNProNaviFragment");
                    }
                    break;
                case R.id.route_result:
                    ((BNDemoLightNaviActivity) getActivity()).jumpTo("BNRouteResultFragment");
                    break;
                case R.id.truck_route_plan:
                    routePlan(IBNRoutePlanManager.Vehicle.TRUCK);
                    break;
                case R.id.route_plan:
                    routePlan(IBNRoutePlanManager.Vehicle.CAR);
                    break;
                case R.id.new_energy:
                    newEnergyBtn.setSelected(!newEnergyBtn.isSelected());
                    if (newEnergyBtn.isSelected()) {
                        BNDemoFactory.getInstance().initCarInfoNewEnergy();
                    } else {
                        BNDemoFactory.getInstance().initCarInfo();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                    Toast.makeText(getActivity(), "算路开始", Toast.LENGTH_SHORT).show();
                    break;
                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                    Toast.makeText(getActivity(), "算路成功", Toast.LENGTH_SHORT).show();

                    int targetIndex = 0;
                    // 算路成功获取路线信息
                    BNRoutePlanInfos routePlanInfos = mRoutePlanManager.getRoutePlanInfo();

                    // 用第0条路线开启轻导航，直接调用即可
                    mLightNaviManager.startLightNavi();

                    // 自己选路进行轻导航：
//                    targetIndex = startLightNaviBySelect(routePlanInfos);

                    refreshTopView(routePlanInfos.getRouteTabInfos().get(targetIndex), -1, -1);
                    break;
                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                    Toast.makeText(getActivity(),
                            "算路失败", Toast.LENGTH_SHORT).show();

                    break;
                default:
                    // nothing
                    break;
            }
        }
    };

    /**
     * 自己选择路线进行导航
     */
    private int startLightNaviBySelect(BNRoutePlanInfos routePlanInfos) {
        int targetIndex;
        // 选择第2条路线导航
        if (routePlanInfos.getRouteTabInfos().size() > 1) {
            targetIndex = 1;
        } else {
            targetIndex = 0;
        }


        mRoutePlanManager.selectRoute(targetIndex, new IBNRoutePlanManager.SelectRouteListener() {
            @Override
            public void onSelectComplete() {
                mLightNaviManager.startLightNavi();

            }
        });

        return targetIndex;
    }
}
