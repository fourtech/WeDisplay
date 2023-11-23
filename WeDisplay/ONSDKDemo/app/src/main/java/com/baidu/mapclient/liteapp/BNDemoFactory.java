package com.baidu.mapclient.liteapp;

import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNOuterSettingParams;
import com.baidu.navisdk.adapter.struct.BNMotorInfo;
import com.baidu.navisdk.adapter.struct.BNTruckInfo;
import com.baidu.navisdk.adapter.struct.VehicleConstant;

import android.content.Context;
import android.text.TextUtils;

/**
 * Author: v_duanpeifeng
 * Time: 2020-05-22
 * Description:
 */
public class BNDemoFactory {

    public static final int AXLES_NUMBER = 6;
    public static final int AXLES_WEIGHT = 100 * 1000;
    public static final int LENGTH = 25 * 1000;
    public static final int WEIGHT = 100 * 1000;
    public static final int LOAD_WEIGHT = 80 * 1000;
    public static final String OIL_COST = "40000";
    public static final int HEIGHT = 10 * 1000;
    public static final float WIDTH = 5f * 1000;
    public static final double LATITUDE = 40.041690;
    public static final double LONGITUDE = 116.306333;
    public static final String NAME = "百度大厦";
    public static final double LATITUDE1 = 39.908560;
    public static final double LONGITUDE1 = 116.397609;
    public static final String NAME1 = "北京天安门";
    public static final double DOUBLE = 0.5;
    private BNRoutePlanNode startNode;

    private BNRoutePlanNode endNode;

    private BNDemoFactory() {
    }

    private static class Holder {
        private static BNDemoFactory INSTANCE = new BNDemoFactory();
    }

    public static BNDemoFactory getInstance() {
        return Holder.INSTANCE;
    }

    public void initCarInfoNewEnergy() {
        // 驾车车牌设置
        BaiduNaviManagerFactory.getCommonSettingManager().setCarNum("沪FTP939",
                IBNOuterSettingParams.CarPowerType.NewEnergy);

        // 货车信息 kg/mm
        BNTruckInfo truckInfo = new BNTruckInfo.Builder()
//                .truckUsage(VehicleConstant.TruckUsage.DANGER)
                .plate("沪FTP939")
                .axlesNumber(AXLES_NUMBER)
                .axlesWeight(AXLES_WEIGHT)
                .emissionLimit(VehicleConstant.EmissionStandard.S3)
                .length(LENGTH)
                .weight(WEIGHT)
                .loadWeight(LOAD_WEIGHT)
                .oilCost(OIL_COST)
                .plateType(VehicleConstant.PlateType.YELLOW)
                .powerType(VehicleConstant.PowerType.ELECTRIC)
                .truckType(VehicleConstant.TruckType.HEAVY)
                .height(HEIGHT)
                .width(WIDTH)
                .build();
        // 该接口会做本地持久化，在应用中设置一次即可
        BaiduNaviManagerFactory.getCommonSettingManager().setTruckInfo(truckInfo);
    }

    public void initCarInfo() {
        // 驾车车牌设置
        BaiduNaviManagerFactory.getCommonSettingManager().setCarNum("沪FTP939",
                IBNOuterSettingParams.CarPowerType.Normal);

        // 货车信息
        BNTruckInfo truckInfo = new BNTruckInfo.Builder()
//                .truckUsage(VehicleConstant.TruckUsage.DANGER)
                .plate("沪FTP939")
                .axlesNumber(AXLES_NUMBER)
                .axlesWeight(AXLES_WEIGHT)
                .emissionLimit(VehicleConstant.EmissionStandard.S3)
                .length(LENGTH)
                .weight(WEIGHT)
                .loadWeight(LOAD_WEIGHT)
                .oilCost(OIL_COST)
                .plateType(VehicleConstant.PlateType.YELLOW)
                .powerType(VehicleConstant.PowerType.OIL)
                .truckType(VehicleConstant.TruckType.HEAVY)
                .height(HEIGHT)
                .width(WIDTH)
                .build();
        // 该接口会做本地持久化，在应用中设置一次即可
        BaiduNaviManagerFactory.getCommonSettingManager().setTruckInfo(truckInfo);

        // 摩托车信息
        BNMotorInfo motorInfo = new BNMotorInfo.Builder()
                .plate("沪FTP939")
                .plateType(VehicleConstant.PlateType.BLUE)
                .motorType(VehicleConstant.MotorType.OIL)
                .displacement("")
                .build();
        // 该接口会做本地持久化，在应用中设置一次即可
        BaiduNaviManagerFactory.getCommonSettingManager().setMotorInfo(motorInfo);

        // BaiduNaviManagerFactory.getCommonSettingManager().setTestEnvironment(false);
        BaiduNaviManagerFactory.getCommonSettingManager().setNodeClick(true);
    }

    public void initRoutePlanNode() {
        startNode = new BNRoutePlanNode.Builder()
                .latitude(LATITUDE)
                .longitude(LONGITUDE)
                .name(NAME)
                .description(NAME)
                .build();
        endNode = new BNRoutePlanNode.Builder()
                .latitude(LATITUDE1)
                .longitude(LONGITUDE1)
                .name(NAME1)
                .description(NAME1)
                .build();
    }

    public BNRoutePlanNode getStartNode(Context context) {
        String start = BNDemoUtils.getString(context, "start_node");
        if (!TextUtils.isEmpty(start)) {
            String[] node = start.split(",");
            startNode = new BNRoutePlanNode.Builder()
                    .longitude(Double.parseDouble(node[0]))
                    .latitude(Double.parseDouble(node[1]))
                    .build();
        }
        return startNode;
    }

    public BNRoutePlanNode getNewNode(Context context) {
        String start = BNDemoUtils.getString(context, "start_node");
        if (!TextUtils.isEmpty(start)) {
            String[] node = start.split(",");
            startNode = new BNRoutePlanNode.Builder()
                    .longitude(Double.parseDouble(node[0]) + DOUBLE)
                    .latitude(Double.parseDouble(node[1]) + DOUBLE)
                    .build();
        }
        return startNode;
    }

    public void setStartNode(Context context, String value) {
        BNDemoUtils.setString(context, "start_node", value);
    }

    public BNRoutePlanNode getEndNode(Context context) {
        String end = BNDemoUtils.getString(context, "end_node");
        if (!TextUtils.isEmpty(end)) {
            String[] node = end.split(",");
            endNode = new BNRoutePlanNode.Builder()
                    .longitude(Double.parseDouble(node[0]))
                    .latitude(Double.parseDouble(node[1]))
                    .build();
        }
        return endNode;
    }

    public void setEndNode(Context context, String value) {
        BNDemoUtils.setString(context, "end_node", value);
    }

    public BNRoutePlanNode getCurrentNode(Context context, float d) {
        String end = BNDemoUtils.getString(context, "current_node");
        if (!TextUtils.isEmpty(end)) {
            String[] node = end.split(",");
            return new BNRoutePlanNode.Builder()
                    .longitude(Double.parseDouble(node[0]) + d)
                    .latitude(Double.parseDouble(node[1]) + d)
                    .build();
        }
        return null;
    }

    public int searchType = -1;

    private SuggestionResult.SuggestionInfo poiSearchInfo;

    public BNRoutePlanNode getPoiSearchNode() {
        return getNode(poiSearchInfo);
    }

    public void clearSuggestionInfo() {
        poiSearchInfo = null;
    }

    public void setSuggestionInfo(SuggestionResult.SuggestionInfo suggestionInfo) {
        this.poiSearchInfo = suggestionInfo;
    }

    public BNRoutePlanNode getNode(SuggestionResult.SuggestionInfo suggestionInfo) {
        if (suggestionInfo == null) {
            return null;
        }
        BNRoutePlanNode node = new BNRoutePlanNode.Builder()
                .longitude(suggestionInfo.pt.longitude)
                .latitude(suggestionInfo.pt.latitude)
                .name(suggestionInfo.key)
                .build();
        return node;
    }
}
