package com.ziluone.wedisplay.activity;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.ziluone.wedisplay.BNDemoFactory;
import com.ziluone.wedisplay.BNOrderItemAdapter;
import com.ziluone.wedisplay.R;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BNPoiSearchActivity extends Activity
        implements OnGetPoiSearchResultListener, OnGetSuggestionResultListener {

    SuggestionSearch mSuggestionSearch;
    PoiSearch mPoiSearch = PoiSearch.newInstance();
    BNOrderItemAdapter adapter;
    RecyclerView recycleView;
    List<SuggestionResult.SuggestionInfo> suggestionInfos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onsdk_fragment_poisearch);
        init();
    }

    private void init() {
        recycleView = findViewById(R.id.recycle);
        adapter = new BNOrderItemAdapter(this, null,
                new BNOrderItemAdapter.ItemClickListener() {
                    @Override
                    public void click(String info, int position) {
                        BNDemoFactory.getInstance().setSuggestionInfo(suggestionInfos.get(position));
                        BNPoiSearchActivity.this.finish();
                    }
                });
        recycleView.setAdapter(adapter);
        recycleView.setLayoutManager(new LinearLayoutManager(this));

        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);
        mPoiSearch.setOnGetPoiSearchResultListener(this);
        ((EditText) findViewById(R.id.name)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (((EditText) findViewById(R.id.city)).getText() == null
                        || ((EditText) findViewById(R.id.city)).getText().length() == 0) {
                    Toast.makeText(getApplicationContext(), "未输入城市~", Toast.LENGTH_SHORT).show();
                    return;
                }
                mPoiSearch.searchInCity(new PoiCitySearchOption()
                        .city(((EditText) findViewById(R.id.city)).getText().toString())
                        .keyword(s.toString())
                        .pageNum(0));
                mSuggestionSearch.requestSuggestion(new SuggestionSearchOption()
                        .city(((EditText) findViewById(R.id.city)).getText().toString())
                        .keyword(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onGetSuggestionResult(SuggestionResult suggestionResult) {
        suggestionInfos = suggestionResult.getAllSuggestions();
        if (suggestionInfos != null && suggestionInfos.size() > 0) {
            List<String> keyList = new ArrayList<>();
            for (int i = 0; i < suggestionInfos.size(); i++) {
                keyList.add(suggestionInfos.get(i).getKey());
            }
            adapter.setOrderInfos(keyList);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSuggestionSearch.destroy();
        mPoiSearch.destroy();
    }

    @Override
    public void onGetPoiResult(PoiResult poiResult) {

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }
}
