package com.ziluone.wedisplay.routeresult;

import static com.ziluone.wedisplay.future.FutureTripController.Config.ENTRY_TYPE_DEPART;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ziluone.wedisplay.BNDemoFactory;
import com.ziluone.wedisplay.BNDemoUtils;
import com.ziluone.wedisplay.R;
import com.ziluone.wedisplay.custom.BNRecyclerView;
import com.ziluone.wedisplay.custom.BNScrollLayout;
import com.ziluone.wedisplay.custom.BNScrollView;
import com.ziluone.wedisplay.future.FutureTripController;
import com.ziluone.wedisplay.future.FutureTripDateTimePickerView;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.IBNRouteResultManager;
import com.baidu.navisdk.adapter.struct.BNRouteDetail;
import com.baidu.navisdk.adapter.struct.BNRoutePlanItem;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DemoFutureFragment extends Fragment implements View.OnClickListener,
        PreferItemsAdapter.ClickPreferListener {

    private static final String TAG = "DemoRouteResultFragment";
    public static final int DELAY_MILLIS = 1000;

    private LinearLayout mLayoutTab0;
    private LinearLayout mLayoutTab1;
    private LinearLayout mLayoutTab2;
    private RelativeLayout mRlButton;
    private FrameLayout mFlRetry;
    private LinearLayout mLDLayout;
    private Button mPreferBtn;
    private RouteResultAdapter mResultAdapter;
    private BNRecyclerView mRecyclerView;
    private RecyclerView mPreferRecyclerView;
    private PreferItemsAdapter mItemsAdapter;
    private PopupWindow mPopWindow;
    private ArrayList<RouteSortModel> mRouteSortList;
    private ArrayList<BNRoutePlanItem> mRoutePlanItems;
    private ArrayList<BNRouteDetail> mRouteList = new ArrayList<>();
    private Bundle mRouteDetails = new Bundle();
    private Bundle mRoutePoints = new Bundle();
    private ArrayList<String> mLimitInfos = new ArrayList<>();
    private View mRootView;
    private FutureTripController mFutureDialog;
    private int currentPrefer = IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                    Toast.makeText(getContext(), "算路开始", Toast.LENGTH_SHORT).show();
                    mFlRetry.setVisibility(View.GONE);
                    mLayoutTab0.setSelected(false);
                    mLayoutTab1.setSelected(false);
                    mLayoutTab2.setSelected(false);
                    break;
                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                    Toast.makeText(getContext(), "算路成功", Toast.LENGTH_SHORT).show();
                    mFlRetry.setVisibility(View.GONE);
                    mRlButton.setVisibility(View.VISIBLE);
                    updateBtnText(currentPrefer);
                    initData();
                    break;
                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                    Toast.makeText(getContext(), "算路失败", Toast.LENGTH_SHORT).show();
                    mFlRetry.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        BaiduNaviManagerFactory.getRouteResultManager().onCreate(getActivity());
        mRootView = inflater.inflate(R.layout.fragment_route_result_future, container, false);
        mLayoutTab0 = mRootView.findViewById(R.id.route_0);
        mLayoutTab0.setOnClickListener(this);
        mLayoutTab1 = mRootView.findViewById(R.id.route_1);
        mLayoutTab1.setOnClickListener(this);
        mLayoutTab2 = mRootView.findViewById(R.id.route_2);
        mLayoutTab2.setOnClickListener(this);
        mRlButton = mRootView.findViewById(R.id.rl_button);
        mRecyclerView = mRootView.findViewById(R.id.rv);
        mPreferBtn = mRootView.findViewById(R.id.btn_prefer);
        mPreferBtn.setOnClickListener(this);
        mLDLayout = mRootView.findViewById(R.id.ld_container);
        mFlRetry = mRootView.findViewById(R.id.fl_retry);
        mFlRetry.setOnClickListener(this);
        mRootView.findViewById(R.id.btn_road).setOnClickListener(this);
        mRootView.findViewById(R.id.btn_fullView).setOnClickListener(this);

        BaiduNaviManagerFactory.getRouteResultSettingManager().setRouteMargin(
                100, 100, 100, 500);

        initFutureWindow((ViewGroup) mRootView.findViewById(R.id.future_dialog));
        initPreferPopWindow();
        initListener();
        return mRootView;
    }

    private void initFutureWindow(ViewGroup viewGroup) {
        mFutureDialog = new FutureTripController(getActivity(), viewGroup,
                new FutureTripDateTimePickerView.ActionListener() {
                    @Override
                    public void onClickCancelBtn(String time, Date date, int... args) {
                        mFutureDialog.hide();
                        getActivity().getSupportFragmentManager().popBackStack();
                    }

                    @Override
                    public void onClickConfirmBtn(String time, Date date, int... args) {
                        mFutureDialog.hide();
                        BaiduNaviManagerFactory.getRoutePlanManager().setRoutePlanTime(date);
                        routePlan();
                    }

                    @Override
                    public void onShow() {

                    }

                    @Override
                    public void onHide() {

                    }
                });
        FutureTripController.Config config = new FutureTripController.Config();
        config.defaultValidDate = BNDemoUtils.change(new Date(System.currentTimeMillis()));
        config.titleStrResId = R.string.nsdk_future_trip_time_picker_depart_title;
        config.entryType = ENTRY_TYPE_DEPART;
        config.entryDate = BNDemoUtils.change(new Date(System.currentTimeMillis()));
        mFutureDialog.config(config);
        mFutureDialog.show();
    }

    private void initListener() {
        BaiduNaviManagerFactory.getRouteResultManager().setCalcRouteByViaListener(
                new IBNRouteResultManager.ICalcRouteByViaListener() {
                    @Override
                    public void onStart() {
                        Log.e(TAG, "开始算路");
                    }

                    @Override
                    public void onSuccess() {
                        mFlRetry.setVisibility(View.GONE);
                        mRlButton.setVisibility(View.VISIBLE);
                        updateBtnText(currentPrefer);
                        initData();
                    }

                    @Override
                    public void onFailed(int errorCode) {
                        mFlRetry.setVisibility(View.VISIBLE);
                    }
                });
        BaiduNaviManagerFactory.getRouteResultManager().setRouteClickedListener(
                new IBNRouteResultManager.IRouteClickedListener() {
                    @Override
                    public void routeClicked(int index) {
                        BaiduNaviManagerFactory.getRouteResultManager().selectRoute(index);
                        switch (index) {
                            case 0:
                                mLayoutTab0.setSelected(true);
                                mLayoutTab1.setSelected(false);
                                mLayoutTab2.setSelected(false);
                                break;
                            case 1:
                                mLayoutTab0.setSelected(false);
                                mLayoutTab1.setSelected(true);
                                mLayoutTab2.setSelected(false);
                                break;
                            case 2:
                                mLayoutTab0.setSelected(false);
                                mLayoutTab1.setSelected(false);
                                mLayoutTab2.setSelected(true);
                                break;
                        }
                    }
                });
    }

    private void initPreferPopWindow() {
        View popView =
                LayoutInflater.from(getContext()).inflate(R.layout.dialog_pop_prefer, null, false);
        mPreferRecyclerView = popView.findViewById(R.id.nsdk_route_sort_gv);
        initPreferView();
        mPopWindow = new PopupWindow(popView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mPopWindow.setOutsideTouchable(true);
        mPopWindow.setTouchable(true);
    }

    private void initPreferView() {
        initRouteSortList();
        mPreferRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mPreferRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        if (mItemsAdapter == null) {
            mItemsAdapter = new PreferItemsAdapter(getContext(), mRouteSortList);
            mItemsAdapter.setClickPreferListener(this);
        }
        mPreferRecyclerView.setAdapter(mItemsAdapter);
    }

    private void initRouteSortList() {
        mRouteSortList = new ArrayList<>();
        mRouteSortList.add(new RouteSortModel("智能推荐", IBNRoutePlanManager.RoutePlanPreference
                .ROUTE_PLAN_PREFERENCE_DEFAULT));
        mRouteSortList.add(new RouteSortModel("时间优先", IBNRoutePlanManager.RoutePlanPreference
                .ROUTE_PLAN_PREFERENCE_TIME_FIRST));
        mRouteSortList.add(new RouteSortModel("少收费", IBNRoutePlanManager.RoutePlanPreference
                .ROUTE_PLAN_PREFERENCE_NOTOLL));
        mRouteSortList.add(new RouteSortModel("躲避拥堵", IBNRoutePlanManager.RoutePlanPreference
                .ROUTE_PLAN_PREFERENCE_AVOID_TRAFFIC_JAM));
        mRouteSortList.add(new RouteSortModel("不走高速", IBNRoutePlanManager.RoutePlanPreference
                .ROUTE_PLAN_PREFERENCE_NOHIGHWAY));
        mRouteSortList.add(new RouteSortModel("高速优先", IBNRoutePlanManager.RoutePlanPreference
                .ROUTE_PLAN_PREFERENCE_ROAD_FIRST));
    }

    private void initView() {
        BNScrollView scrollView = mRootView.findViewById(R.id.content_scroll);
        scrollView.setVerticalScrollBarEnabled(false);
        final LinearLayout layoutTab = mRootView.findViewById(R.id.layout_3tab);
        final BNScrollLayout scrollLayout = mRootView.findViewById(R.id.layout_scroll);
        scrollLayout.setMaxOffset(0);
        scrollLayout.setToOpen();
        mRlButton.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                RelativeLayout.LayoutParams layoutParams =
                        (RelativeLayout.LayoutParams) mRlButton.getLayoutParams();
                float dipValue = 10f;
                if (BNDemoUtils.checkDeviceHasNavigationBar(getActivity())) {
                    int maxOffSet = layoutTab.getMeasuredHeight()
                            + BNDemoUtils.dip2px(getActivity(), dipValue)
                            + BNDemoUtils.getNavigationBarHeight(getActivity());
                    scrollLayout.setMaxOffset(maxOffSet);
                    mRlButton.setPadding(0, 0, 0, maxOffSet);
                } else {
                    int maxOffSet = layoutTab.getMeasuredHeight()
                            + BNDemoUtils.dip2px(getActivity(), dipValue);
                    scrollLayout.setMaxOffset(maxOffSet);
                    mRlButton.setPadding(0, 0, 0, maxOffSet);
                }
                scrollLayout.setToOpen();
                mRlButton.invalidate();
            }
        }, DELAY_MILLIS);
    }

    private void initData() {
        Bundle bundle = BaiduNaviManagerFactory.getRouteResultManager().getRouteInfo();
        if (bundle == null) {
            return;
        }
        // 3Tab信息
        mRoutePlanItems = bundle.getParcelableArrayList(BNaviCommonParams.BNRouteInfoKey.INFO_TAB);
        // 每条路线的详细信息
        mRouteDetails = bundle.getBundle(BNaviCommonParams.BNRouteInfoKey.INFO_ROUTE_DETAIL);
        // 每条路线的限行信息
        mLimitInfos =
                bundle.getStringArrayList(BNaviCommonParams.BNRouteInfoKey.TRAFFIC_LIMIT_INFO);
        // 每条路线的点坐标
        mRoutePoints = bundle.getBundle(BNaviCommonParams.BNRouteInfoKey.INFO_ROUTE_POINT);
        if (mRoutePoints != null) {
            ArrayList<BNRoutePlanNode> nodes = mRoutePoints.getParcelableArrayList("0");
        }
        if (mLimitInfos != null) {
            for (int i = 0; i < mLimitInfos.size(); i++) {
                String[] arr = mLimitInfos.get(i).split(",");
                Log.e(TAG, "第" + arr[0] + "条路线限行消息：" + arr[1]);
            }
        }
        if (mRoutePlanItems != null) {
            if (mRoutePlanItems.size() > 0 && mRoutePlanItems.get(0) != null) {
                initTabView(mLayoutTab0, mRoutePlanItems.get(0));
            }

            if (mRoutePlanItems.size() > 1 && mRoutePlanItems.get(1) != null) {
                initTabView(mLayoutTab1, mRoutePlanItems.get(1));
                mLayoutTab1.setVisibility(View.VISIBLE);
            } else {
                mLayoutTab1.setVisibility(View.GONE);
            }

            if (mRoutePlanItems.size() > 2 && mRoutePlanItems.get(2) != null) {
                initTabView(mLayoutTab2, mRoutePlanItems.get(2));
                mLayoutTab2.setVisibility(View.VISIBLE);
            } else {
                mLayoutTab2.setVisibility(View.GONE);
            }
        }
        mLayoutTab0.setSelected(true);

        mRouteList.clear();
        mRouteList.addAll(mRouteDetails.<BNRouteDetail>getParcelableArrayList("0"));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mResultAdapter = new RouteResultAdapter(mRouteList);
        mRecyclerView.setAdapter(mResultAdapter);
    }

    private void initTabView(LinearLayout layoutTab, BNRoutePlanItem bnRoutePlanItem) {
        TextView prefer = layoutTab.findViewById(R.id.prefer);
        prefer.setText(bnRoutePlanItem.getPusLabelName());
        TextView time = layoutTab.findViewById(R.id.time);
        time.setText((int) bnRoutePlanItem.getPassTime() / 60 + "分钟");
        TextView distance = layoutTab.findViewById(R.id.distance);
        distance.setText((int) bnRoutePlanItem.getLength() / 1000 + "公里");
        TextView trafficLight = layoutTab.findViewById(R.id.traffic_light);
        trafficLight.setText(String.valueOf(bnRoutePlanItem.getLights()));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mFlRetry.getVisibility() == View.GONE) {
            initView();
        }
        BaiduNaviManagerFactory.getRouteResultManager().onResume();
        FrameLayout ybContainer = mRootView.findViewById(R.id.yb_container);
        BaiduNaviManagerFactory.getRouteResultManager().addYellowTipsToContainer(ybContainer);
    }

    @Override
    public void onPause() {
        super.onPause();
        BaiduNaviManagerFactory.getRouteResultManager().onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mItemsAdapter.onDestroy();
        BaiduNaviManagerFactory.getRouteResultManager().onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.route_0:
                mLayoutTab0.setSelected(true);
                mLayoutTab1.setSelected(false);
                mLayoutTab2.setSelected(false);
                BaiduNaviManagerFactory.getRouteResultManager().selectRoute(0);
                BaiduNaviManagerFactory.getRouteResultManager().fullView();
                mRouteList.clear();
                mRouteList.addAll(mRouteDetails.<BNRouteDetail>getParcelableArrayList("0"));
                mResultAdapter.notifyDataSetChanged();
                break;
            case R.id.route_1:
                mLayoutTab0.setSelected(false);
                mLayoutTab1.setSelected(true);
                mLayoutTab2.setSelected(false);
                BaiduNaviManagerFactory.getRouteResultManager().selectRoute(1);
                BaiduNaviManagerFactory.getRouteResultManager().fullView();
                mRouteList.clear();
                mRouteList.addAll(mRouteDetails.<BNRouteDetail>getParcelableArrayList("1"));
                mResultAdapter.notifyDataSetChanged();
                break;
            case R.id.route_2:
                if (mRoutePlanItems.size() < 3) {
                    return;
                }
                mLayoutTab0.setSelected(false);
                mLayoutTab1.setSelected(false);
                mLayoutTab2.setSelected(true);
                BaiduNaviManagerFactory.getRouteResultManager().selectRoute(2);
                BaiduNaviManagerFactory.getRouteResultManager().fullView();
                mRouteList.clear();
                mRouteList.addAll(mRouteDetails.<BNRouteDetail>getParcelableArrayList("2"));
                mResultAdapter.notifyDataSetChanged();
                break;
            case R.id.btn_fullView:
                BaiduNaviManagerFactory.getRouteResultManager().fullView();
                break;
            case R.id.btn_road:
                BaiduNaviManagerFactory.getRouteResultSettingManager().setRealRoadCondition(getActivity(),
                        !BaiduNaviManagerFactory.getRouteResultSettingManager().isRealRoadConditionOpen());
                break;
            case R.id.btn_prefer:
                mPopWindow.showAtLocation(mRootView, Gravity.BOTTOM, 0, 0);
                break;
            case R.id.fl_retry:
                routePlan();
                break;
            default:
                break;
        }
    }

    @Override
    public void onClickPrefer(int clickPrefer) {
        currentPrefer = clickPrefer;
        mItemsAdapter.updatePrefer(clickPrefer);
        mItemsAdapter.notifyDataSetChanged();
        mPopWindow.dismiss();
        mRlButton.setVisibility(View.GONE);
        routePlan();
    }

    private void updateBtnText(int clickPrefer) {
        switch (clickPrefer) {
            case IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT:
                mPreferBtn.setText("智能推荐");
                break;
            case IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_TIME_FIRST:
                mPreferBtn.setText("时间优先");
                break;
            case IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_NOTOLL:
                mPreferBtn.setText("少收费");
                break;
            case IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_AVOID_TRAFFIC_JAM:
                mPreferBtn.setText("躲避拥堵");
                break;
            case IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_NOHIGHWAY:
                mPreferBtn.setText("不走高速");
                break;
            case IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_ROAD_FIRST:
                mPreferBtn.setText("高速优先");
                break;
            default:
                break;
        }
    }

    private void routePlan() {
        List<BNRoutePlanNode> list = new ArrayList<>();
        BNRoutePlanNode startNode = BNDemoFactory.getInstance().getCurrentNode(getContext(), 0);
        if (startNode == null) {
            startNode = BNDemoFactory.getInstance().getStartNode(getContext());
        }
        list.add(startNode);
        list.add(BNDemoFactory.getInstance().getEndNode(getContext()));

        // 关闭电子狗
        if (BaiduNaviManagerFactory.getCruiserManager().isCruiserStarted()) {
            BaiduNaviManagerFactory.getCruiserManager().stopCruise();
        }
        Bundle bundle = new Bundle();
        bundle.putInt(BNaviCommonParams.RoutePlanKey.VEHICLE_TYPE, IBNRoutePlanManager.Vehicle.TRUCK);
        bundle.putBoolean(BNaviCommonParams.RoutePlanKey.EXTRA_KEY_IS_FUTURE, true);

        BaiduNaviManagerFactory.getRoutePlanManager().routePlan(list, currentPrefer, bundle,
                handler);
    }
}
