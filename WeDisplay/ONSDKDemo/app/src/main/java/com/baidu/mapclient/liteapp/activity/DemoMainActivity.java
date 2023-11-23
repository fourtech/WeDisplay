/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.mapclient.liteapp.activity;


import java.util.ArrayList;
import java.util.List;

import com.baidu.mapclient.liteapp.BNDemoFactory;
import com.baidu.mapclient.liteapp.BNDemoUtils;
import com.baidu.mapclient.liteapp.ForegroundService;
import com.baidu.mapclient.liteapp.R;
import com.baidu.mapclient.liteapp.controlwindow.ControlBoardWindow;
import com.baidu.mapclient.liteapp.BNInitHelper;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.comapi.setting.BNCommSettingManager;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class DemoMainActivity extends Activity {

    private static final String[] authBaseArr = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int authBaseRequestCode = 1;

    private Button mNaviBtn = null;
    private Button mTruckBtn = null;
    private Button mMotorBtn = null;
    private Button mExternalBtn = null;
    private Button mDrivingBtn = null;
    private Button mOverlayBtn = null;
    private Button mCruiserBtn = null;
    private Button mAnalogBtn = null;
    private Button mSelectNodeBtn = null;
    private Button mGotoSettingsBtn = null;
    private Button limitChange = null;

    private BroadcastReceiver mReceiver;
    private int mPageType = BNDemoUtils.NORMAL;

    private Runnable mClickAction;
    private BNInitHelper bnInitHelper;

    // 携带到导航中的信息
    private Bundle mBundle;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                    Toast.makeText(DemoMainActivity.this, "算路开始", Toast.LENGTH_SHORT).show();
                    ControlBoardWindow.getInstance().showControl("算路开始");
                    break;
                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                    Toast.makeText(DemoMainActivity.this, "算路成功", Toast.LENGTH_SHORT).show();
                    ControlBoardWindow.getInstance().showControl("算路成功");
                    // 躲避限行消息
                    Bundle infoBundle = (Bundle) msg.obj;
                    if (infoBundle != null) {
                        String info = infoBundle
                                .getString(BNaviCommonParams.BNRouteInfoKey.TRAFFIC_LIMIT_INFO);
                        Log.e("OnSdkDemo", "info = " + info);
                    }
                    break;
                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                    ControlBoardWindow.getInstance().showControl("算路失败");
                    Toast.makeText(DemoMainActivity.this.getApplicationContext(),
                            "算路失败", Toast.LENGTH_SHORT).show();
                    break;
                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                    Toast.makeText(DemoMainActivity.this.getApplicationContext(),
                            "算路成功准备进入导航", Toast.LENGTH_SHORT).show();
                    ControlBoardWindow.getInstance().showControl("算路成功准备进入导航");
                    switch (mPageType) {
                        case BNDemoUtils.NORMAL:
                            BNDemoUtils.gotoNavi(DemoMainActivity.this, mBundle);
                            break;
                        case BNDemoUtils.ANALOG:
                            BNDemoUtils.gotoAnalog(DemoMainActivity.this);
                            break;
                        case BNDemoUtils.EXTGPS:
                            BNDemoUtils.gotoExtGps(DemoMainActivity.this);
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    // nothing
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!this.isTaskRoot()){
            finish();
            return;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 开启前台服务防止应用进入后台gps挂掉
        startService(new Intent(this, ForegroundService.class));

        initView();
        initListener();
        initPermission();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!Settings.canDrawOverlays(this)) {
//                // 若未授权则请求权限
//                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
//                intent.setData(Uri.parse("package:" + getPackageName()));
//                startActivityForResult(intent, 0);
//            } else {
//                ControlBoardWindow.getInstance().showPopupWindow(DemoMainActivity.this);
//                ControlBoardWindow.getInstance().showControl("初始化");
//            }
//        }
        initBroadCastReceiver();
        bnInitHelper = new BNInitHelper(getApplicationContext());
    }

    private void initBroadCastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.navi.ready");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                BNDemoFactory.getInstance().initCarInfo();
                BNDemoFactory.getInstance().initRoutePlanNode();
                if (mClickAction != null) {
                    mClickAction.run();
                    mClickAction = null;
                }
            }
        };
        registerReceiver(mReceiver, filter);
    }

    private void initPermission() {
        // 申请权限
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (!hasBasePhoneAuth()) {
                requestPermissions(authBaseArr, authBaseRequestCode);
            }
        }
    }

    private void initView() {
        mNaviBtn = findViewById(R.id.naviBtn);
        mTruckBtn = findViewById(R.id.truckBtn);
        mMotorBtn = findViewById(R.id.motorBtn);
        mExternalBtn = findViewById(R.id.externalBtn);
        mAnalogBtn = findViewById(R.id.analogBtn);
        mOverlayBtn = findViewById(R.id.overlayBtn);
        mDrivingBtn = findViewById(R.id.drivingBtn);
        mCruiserBtn = findViewById(R.id.cruiserBtn);
        mGotoSettingsBtn = findViewById(R.id.gotoSettingsBtn);
        mSelectNodeBtn = findViewById(R.id.selectNodeBtn);
    }

    private void clickAction(Runnable runnable) {
        if (!BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
            mClickAction = runnable;
            bnInitHelper.init();
        } else {
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    private void initListener() {
        findViewById(R.id.addpoint).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.addpoint).setSelected(!findViewById(R.id.addpoint).isSelected());
            }
        });

        findViewById(R.id.new_energy).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
                    mPageType = BNDemoUtils.NORMAL;
                    Bundle bundle = new Bundle();
                    bundle.putInt(BNaviCommonParams.RoutePlanKey.SUB_VEHICLE, IBNRoutePlanManager.SubVehicle.NEW_ENERGY);
                    routePlanToNavi(bundle);
                }
            }
        });




        findViewById(R.id.hdnavi_open).setSelected(BNCommSettingManager.getInstance().isHdNaviEnable());
        findViewById(R.id.hdnavi_open).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean b = findViewById(R.id.hdnavi_open).isSelected();
                findViewById(R.id.hdnavi_open).setSelected(!b);
                BaiduNaviManagerFactory.getCommonSettingManager().hdnaviOpen(!b);
            }
        });
        if (mNaviBtn != null) {

            mNaviBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 设置最大途径点数量
                    BaiduNaviManagerFactory.getCommonSettingManager().setViaPointCount(16);
                    clickAction(new Runnable() {
                        @Override
                        public void run() {
                            mPageType = BNDemoUtils.NORMAL;
                            routePlanToNavi(null);
                        }
                    });
                }
            });
        }

        if (mTruckBtn != null) {
            mTruckBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickAction(new Runnable() {
                        @Override
                        public void run() {
                            mPageType = BNDemoUtils.NORMAL;
                            Bundle bundle = new Bundle();
                            bundle.putInt(BNaviCommonParams.RoutePlanKey.VEHICLE_TYPE,
                                    IBNRoutePlanManager.Vehicle.TRUCK);
                            routePlanToNavi(bundle);
                        }
                    });
                }
            });
        }

        if (mMotorBtn != null) {
            mMotorBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickAction(new Runnable() {
                        @Override
                        public void run() {
                            mPageType = BNDemoUtils.NORMAL;
                            Bundle bundle = new Bundle();
                            bundle.putInt(BNaviCommonParams.RoutePlanKey.VEHICLE_TYPE,
                                    IBNRoutePlanManager.Vehicle.MOTOR);
                            routePlanToNavi(bundle);
                        }
                    });
                }
            });
        }

        if (mExternalBtn != null) {
            mExternalBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickAction(new Runnable() {
                        @Override
                        public void run() {
                            mPageType = BNDemoUtils.EXTGPS;
                            routePlanToNavi(null);
                        }
                    });
                }
            });
        }

        if (mAnalogBtn != null) {
            mAnalogBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickAction(new Runnable() {
                        @Override
                        public void run() {
                            mPageType = BNDemoUtils.ANALOG;
                            Bundle bundle = new Bundle();
                            bundle.putInt(BNaviCommonParams.RoutePlanKey.VEHICLE_TYPE,
                                    IBNRoutePlanManager.Vehicle.CAR);
                            routePlanToNavi(bundle);
                        }
                    });
                }
            });
        }

        if (mOverlayBtn != null) {
            mOverlayBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickAction(new Runnable() {
                        @Override
                        public void run() {
                            BNDemoUtils.gotoDrawOverlay(DemoMainActivity.this);
                        }
                    });
                }
            });
        }

        if (mDrivingBtn != null) {
            mDrivingBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickAction(new Runnable() {
                        @Override
                        public void run() {
                            BNDemoUtils.gotoDriving(DemoMainActivity.this);
                        }
                    });
                }
            });
        }

        if (mCruiserBtn != null) {
            mCruiserBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickAction(new Runnable() {
                        @Override
                        public void run() {
                            BNDemoUtils.gotoCruiser(DemoMainActivity.this);
                        }
                    });
                }
            });
        }

        if (mGotoSettingsBtn != null) {
            mGotoSettingsBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickAction(new Runnable() {
                        @Override
                        public void run() {
                            BNDemoUtils.gotoSettings(DemoMainActivity.this);
                        }
                    });
                }
            });
        }

        if (mSelectNodeBtn != null) {
            mSelectNodeBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickAction(new Runnable() {
                        @Override
                        public void run() {
                            BNDemoUtils.gotoSelectNode(DemoMainActivity.this);
                        }
                    });
                }
            });
        }

        findViewById(R.id.lightNaviBtn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clickAction(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(DemoMainActivity.this, BNDemoLightNaviActivity.class));
                    }
                });
            }
        });
        limitChange = findViewById(R.id.closeLimit);
        limitChange.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                limitChange.setSelected(!limitChange.isSelected());
                // 货车算路限行
                BaiduNaviManagerFactory.getCommonSettingManager().setTruckLimitSwitch(!limitChange.isSelected());
            }
        });
    }

    private void routePlanToNavi(final Bundle bundle) {
        if (bundle != null) {
            mBundle = new Bundle(bundle);
        } else {
            mBundle = null;
        }
        List<BNRoutePlanNode> list = new ArrayList<>();
        BNRoutePlanNode startNode = BNDemoFactory.getInstance().getCurrentNode(this, 0);
        if (startNode == null) {
            startNode = BNDemoFactory.getInstance().getStartNode(this);
        }
        list.add(startNode);
        if (findViewById(R.id.addpoint).isSelected()) {
            list.add(BNDemoFactory.getInstance().getNewNode(this));
        }
        list.add(BNDemoFactory.getInstance().getEndNode(this));

        // 关闭电子狗
        if (BaiduNaviManagerFactory.getCruiserManager().isCruiserStarted()) {
            BaiduNaviManagerFactory.getCruiserManager().stopCruise();
        }
        BaiduNaviManagerFactory.getRoutePlanManager().routePlanToNavi(
                list,
                IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT,
                bundle, handler);
    }

    private boolean hasBasePhoneAuth() {
        PackageManager pm = this.getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager
                    .PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == authBaseRequestCode) {
            for (int ret : grantResults) {
                if (ret != 0) {
                    Toast.makeText(DemoMainActivity.this.getApplicationContext(),
                            "缺少导航基本的权限!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        limitChange = findViewById(R.id.closeLimit);
        limitChange.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                limitChange.setSelected(!limitChange.isSelected());
                BaiduNaviManagerFactory.getCommonSettingManager().setTruckLimitSwitch(!limitChange.isSelected());
                BaiduNaviManagerFactory.getCommonSettingManager().setTruckWeightLimitSwitch(!limitChange.isSelected());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        stopService(new Intent(this, ForegroundService.class));
    }
}
