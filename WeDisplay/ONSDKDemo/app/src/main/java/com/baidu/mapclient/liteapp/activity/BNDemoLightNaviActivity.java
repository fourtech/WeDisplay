package com.baidu.mapclient.liteapp.activity;

import java.io.InputStream;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapclient.liteapp.BNDemoUtils;
import com.baidu.mapclient.liteapp.R;
import com.baidu.mapclient.liteapp.fragment.BNLightNaviFragment;
import com.baidu.mapclient.liteapp.fragment.BNProNaviFragment;
import com.baidu.mapclient.liteapp.fragment.FragmentNavigator;
import com.baidu.mapsdkplatform.comapi.util.CoordTrans;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNOuterSettingParams;
import com.baidu.nplatform.comjni.tools.JNITools;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

public class BNDemoLightNaviActivity extends FragmentActivity {

    private FrameLayout mapContainer = null;
    private MapView mapView = null;
    private FragmentNavigator navigator = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.onsdk_activity_light_navi);
        initView();
        navigator = new FragmentNavigator(this);
        navigator.jumpTo("BNLightNaviFragment");
        gpsListener();
    }

    private void initView() {
        mapContainer = findViewById(R.id.map_container);
        // 获取导航底图的MapView实例
        mapView = BaiduNaviManagerFactory.getMapManager().getMapView();
        // 将导航底图MapView装载进mapContainer父容器
        BaiduNaviManagerFactory.getMapManager().attach(mapContainer);

        // 设置车标icon
        BaiduNaviManagerFactory.getCommonSettingManager().setDIYImageToMap(
                getbitmap(this, "car.png"),
                IBNOuterSettingParams.DIYImageType.CarLogo);
    }

    public static Bitmap getbitmap(Context context, String fileName) {
        InputStream assetFile = null;
        try {
            AssetManager assets = context.getAssets();
            assetFile = assets.open(fileName);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "获取bitmap失败", Toast.LENGTH_SHORT).show();
        }
        return BitmapFactory.decodeStream(assetFile);
    }

    public void gpsListener() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, " 请添加定位权限", Toast.LENGTH_SHORT).show();
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (SDKInitializer.getCoordType().equals(CoordType.BD09LL)) {
                            LatLng latLng = CoordTrans.wgsToBaidu(
                                    new LatLng(location.getLatitude(), location.getLongitude()));
                            BNDemoUtils.setString(
                                    BNDemoLightNaviActivity.this, "current_node",
                                    latLng.longitude + "," + latLng.latitude);

                        } else {
                            Bundle latLng = JNITools.Wgs84ToGcj02(location.getLongitude(), location.getLatitude());
                            BNDemoUtils.setString(
                                    BNDemoLightNaviActivity.this, "current_node",
                                    latLng.getDouble("LLx") + "," + latLng.getDouble("LLy"));
                        }

                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                });
    }

    public BNRoutePlanNode getCurrentNode(float d) {
        String end = BNDemoUtils.getString(this, "current_node");
        if (!TextUtils.isEmpty(end)) {
            String[] node = end.split(",");
            return new BNRoutePlanNode.Builder()
                    .longitude(Double.parseDouble(node[0]) + d)
                    .latitude(Double.parseDouble(node[1]) + d)
                    .build();
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        BaiduNaviManagerFactory.getMapManager().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BaiduNaviManagerFactory.getMapManager().onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        navigator = null;
        BaiduNaviManagerFactory.getMapManager().detach(mapContainer);
    }

    @Override
    public void onBackPressed() {
        if (navigator != null && navigator.getFragmentStack() != null
                && navigator.getFragmentStack().size() >= 1) {
            if (navigator.getFragmentStack().peek() instanceof BNProNaviFragment) {
                BaiduNaviManagerFactory.getRouteGuideManager().onBackPressed(false);
            } else if (navigator.getFragmentStack().peek() instanceof BNLightNaviFragment) {
                goBack();
                finish();
            } else {
                goBack();
            }
        } else {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BaiduNaviManagerFactory.getRouteGuideManager()
                .onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BaiduNaviManagerFactory.getRouteGuideManager()
                .onActivityResult(requestCode, resultCode, data);
    }

    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        BaiduNaviManagerFactory.getRouteGuideManager()
                .onConfigurationChanged(newConfig);
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {

    }

    public void jumpTo(String fragmentName) {
        if (navigator != null) {
            navigator.jumpTo(fragmentName);
        }
    }

    public void goBack() {
        if (navigator != null) {
            navigator.goBack();
        }
    }

}
