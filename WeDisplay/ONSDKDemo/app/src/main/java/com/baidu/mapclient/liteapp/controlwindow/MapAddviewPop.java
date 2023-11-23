package com.baidu.mapclient.liteapp.controlwindow;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.baidu.mapapi.map.ArcOptions;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapclient.liteapp.R;

import java.util.ArrayList;
import java.util.List;

// 地图sdk mapview 绘制点线面
public class MapAddviewPop extends PopupWindow {

    View mPopView;
    BaiduMap mbaiduMap;

    public MapAddviewPop(Context context, MapView mapview) {
        super(context);
        mbaiduMap = mapview.getMap();
        init(context);
        setpopwindow(context);
    }

    private void setpopwindow(final Context context) {
        this.setContentView(mPopView); // 设置View
        this.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT); // 设置弹出窗口的宽
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT); // 设置弹出窗口的高
        this.setFocusable(false); // 设置弹出窗口可
        this.setOutsideTouchable(false);
        this.setBackgroundDrawable(null);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        mPopView = inflater.inflate(R.layout.mapview_addview, null);
        initListener(mPopView);
    }

    private void initListener(View view) {
        view.findViewById(R.id.mark_point).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapviewOverlayPoint();
            }
        });
        view.findViewById(R.id.line_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapviewOverlayLine();
            }
        });
        view.findViewById(R.id.arcline_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapviewOverlayArcline();
            }
        });
        view.findViewById(R.id.polygon_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapviewOverlayPolygon();
            }
        });
    }

    // mapviewOverlay..—— 地图sdk绘制
    private void mapviewOverlayPoint() {
        // 定义Maker坐标点
        LatLng point = new LatLng(39.963175, 116.400244);
        // 构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.nsdk_map_watermark);
        // 构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        // 在地图上添加Marker，并显示
        mbaiduMap.addOverlay(option);
    }

    private void mapviewOverlayLine() {
        // 构建折线点坐标
        LatLng p1 = new LatLng(39.97923, 116.357428);
        LatLng p2 = new LatLng(39.94923, 116.397428);
        LatLng p3 = new LatLng(39.97923, 116.437428);
        List<LatLng> points = new ArrayList<LatLng>();
        points.add(p1);
        points.add(p2);
        points.add(p3);

        // 设置折线的属性
        OverlayOptions mOverlayOptions = new PolylineOptions()
                .width(10)
                .color(0xAAFF0000)
                .points(points);
        // 在地图上绘制折线  mPloyline 折线对象
        Overlay mPolyline = mbaiduMap.addOverlay(mOverlayOptions);
    }

    private void mapviewOverlayArcline() {
        // 添加弧线坐标数据
        LatLng p1 = new LatLng(39.97923, 116.357428); // 起点
        LatLng p2 = new LatLng(39.94923, 116.397428); // 中间点
        LatLng p3 = new LatLng(39.97923, 116.437428); // 终点
        // 构造ArcOptions对象
        OverlayOptions mArcOptions = new ArcOptions().color(Color.RED).width(10).points(p1, p2, p3);
        // 在地图上显示弧线
        Overlay mArc = mbaiduMap.addOverlay(mArcOptions);
    }

    private void mapviewOverlayPolygon() {
        // 多边形顶点位置
        List<LatLng> points = new ArrayList<>();
        points.add(new LatLng(39.93923, 116.357428));
        points.add(new LatLng(39.91923, 116.327428));
        points.add(new LatLng(39.89923, 116.347428));
        points.add(new LatLng(39.89923, 116.367428));
        points.add(new LatLng(39.91923, 116.387428));
        // 构造PolygonOptions
        PolygonOptions mPolygonOptions = new PolygonOptions()
                .points(points)
                .fillColor(0xAAFFFF00) // 填充颜色
                .stroke(new Stroke(5, 0xAA00FF00)); // 边框宽度和颜色
        // 在地图上显示多边形
        mbaiduMap.addOverlay(mPolygonOptions);
    }

}
