package com.ziluone.wedisplay.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ziluone.wedisplay.BNDemoFactory;
import com.ziluone.wedisplay.BNDemoUtils;
import com.ziluone.wedisplay.R;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;

/**
 * Author: v_duanpeifeng
 * Time: 2020-05-21
 * Description:
 */
public class DemoSelectNodeActivity extends Activity implements View.OnClickListener {

    private EditText carNumText;
    private EditText startNodeText;
    private EditText endNodeText;

    private TextView startTv;
    private TextView endtv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node);

        carNumText = findViewById(R.id.car_num);
        startNodeText = findViewById(R.id.start_node);
        endNodeText = findViewById(R.id.end_node);
        if (BNDemoUtils.getString(this, "start_node") != null) {
            startNodeText.setText(BNDemoUtils.getString(this, "start_node"));
        }
        if (BNDemoUtils.getString(this, "end_node") != null) {
            endNodeText.setText(BNDemoUtils.getString(this, "end_node"));
        }
        findViewById(R.id.confirm).setOnClickListener(this);

        startTv = findViewById(R.id.poi_search_start);
        startTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BNDemoFactory.getInstance().searchType = 1;
                Intent it = new Intent(DemoSelectNodeActivity.this, BNPoiSearchActivity.class);
                DemoSelectNodeActivity.this.startActivity(it);
            }
        });

        endtv = findViewById(R.id.poi_search_end);
        endtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BNDemoFactory.getInstance().searchType = 2;
                Intent it = new Intent(DemoSelectNodeActivity.this, BNPoiSearchActivity.class);
                DemoSelectNodeActivity.this.startActivity(it);
            }
        });
    }

    @Override
    public void onClick(View v) {
        String carNum = carNumText.getText().toString();
        if (!TextUtils.isEmpty(carNum)) {
            BaiduNaviManagerFactory.getCommonSettingManager().setCarNum(carNum);
        }

        String startNode = startNodeText.getText().toString();
        if (!TextUtils.isEmpty(startNode)) {
            BNDemoFactory.getInstance().setStartNode(this, startNode);
        }

        String endNode = endNodeText.getText().toString();
        if (!TextUtils.isEmpty(endNode)) {
            BNDemoFactory.getInstance().setEndNode(this, endNode);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BNDemoFactory.getInstance().searchType != -1) {
            switch (BNDemoFactory.getInstance().searchType) {
                case 1:
                    BNRoutePlanNode startNode = BNDemoFactory.getInstance().getPoiSearchNode();
                    String startName = startNode.getName();
                    startName = startName.length() < 5 ? startName : startName.substring(0, 4) + "..";
                    startTv.setText("起点坐标(" + startName + ")");
                    startNodeText.setText(startNode.getLongitude() + "," + startNode.getLatitude());
                    break;
                case 2:
                    BNRoutePlanNode endNode = BNDemoFactory.getInstance().getPoiSearchNode();
                    String endtName = endNode.getName();
                    endtName = endtName.length() < 5 ? endtName : endtName.substring(0, 4) + "..";
                    endtv.setText("终点坐标(" + endtName + ")");
                    endNodeText.setText(endNode.getLongitude() + "," + endNode.getLatitude());
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BNDemoFactory.getInstance().searchType = -1;
        BNDemoFactory.getInstance().clearSuggestionInfo();
    }
}
