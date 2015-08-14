package com.byg.android.hellokitty;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.platform.comapi.map.m;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchDestinationActivity extends Activity implements 
            OnGetSuggestionResultListener, OnClickListener, OnItemClickListener, OnGetPoiSearchResultListener {

    private EditText keyWorldsView;
    private SuggestionSearch suggestionSearch = null;
    private ArrayList<DestinationBean> mlist = null;
    private SearchAdapter adapter  = null; 
    private ListView  mListView = null;
    private Button mButton = null;
    private ImageView clearEdit; // 清除输入框按钮
    private ImageView nothing ;  // 无找到结果页面
    private PoiSearch mPoisearch = null;
    private String city = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_destination_layout);
        initData();
        initView();
        initSoftKey();
    }

    private void initData() {
        city = getIntent().getExtras().getString("city");
    }

    private void initSoftKey() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
      //得到InputMethodManager的实例
      if (imm.isActive()) {
      //如果开启
          imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
      }
      //关闭软键盘，开启方法相同，这个方法是切换开启与关闭状态的
    }

    private void initView() {
        keyWorldsView = (EditText) findViewById(R.id.search_key);
        mListView = (ListView) findViewById(R.id.suggest_listview);
        clearEdit = (ImageView) findViewById(R.id.clear_icon);
        mButton = (Button) findViewById(R.id.search_btn);
        nothing = (ImageView) findViewById(R.id.nothing_found);
        mButton.setOnClickListener(this);
        clearEdit.setOnClickListener(this);
        suggestionSearch = SuggestionSearch.newInstance();
        suggestionSearch.setOnGetSuggestionResultListener(this);
        mlist = new ArrayList<DestinationBean>();
        adapter = new SearchAdapter();
        mListView.setAdapter(adapter);
        
        mListView.setOnItemClickListener(this);
        mPoisearch = PoiSearch.newInstance();
        mPoisearch.setOnGetPoiSearchResultListener(this);
    }

    @Override
    public void onGetSuggestionResult(SuggestionResult result) {
        if (result == null || result.getAllSuggestions() == null) {
            Toast.makeText(getApplicationContext(), " 没有搜到相关内容  ", Toast.LENGTH_SHORT).show();
            return;
        }
        mlist.clear();
        for (SuggestionResult.SuggestionInfo info : result.getAllSuggestions()) {
            if (info.key != null){
//                sugAdapter.add(info.city+info.district+info.key);
                Log.i("TAG", info.city+","+info.district+","+info.key);
            }
        }
        adapter.notifyDataSetChanged();
        mListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.search_btn:
//            suggestionSearch();
            searchIncity();
            break;
            
        case R.id.clear_icon:
            if (keyWorldsView != null) {
                keyWorldsView.setText("");
            }
            break;
        default:
            break;
        }
    }
    
    /**
     * 在定位城市内搜索，结果在onGetPoiResult()中更新
     */
    private void searchIncity() {
        mPoisearch.searchInCity(new PoiCitySearchOption()
                        .keyword(keyWorldsView.getText().toString()).city(city).pageCapacity(20));
    }

    /**
     * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
     */
    private void suggestionSearch(){
        suggestionSearch
                .requestSuggestion((new SuggestionSearchOption())
                        .keyword(keyWorldsView.getText().toString()).city(city));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getApplicationContext(), "onItemClick:"+position, Toast.LENGTH_SHORT).show();
        Log.i("123", "onItemClick :" + mlist.get(position));
        selectItem(mlist,position);
    }

    /**
     * 
     * @param sugAdapter2
     * @param position
     */
    private void selectItem(ArrayList<DestinationBean> sugAdapter2, int position) {
        if (sugAdapter2 == null) {
            return ;
        }
        Intent  intent = new Intent();
        intent.putExtra("result", sugAdapter2.get(position));
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult arg0) {
        
    }

    @Override
    public void onGetPoiResult(PoiResult result) {
        if (result == null
                || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Log.i("123", "onGetPoiResult:"+result.error.toString());
            if (nothing != null && mListView != null) {
                mListView.setVisibility(View.GONE);
                nothing.setVisibility(View.VISIBLE);
            }
            Toast.makeText(SearchDestinationActivity.this, "没有搜索到结果，请检查网络", Toast.LENGTH_SHORT).show();
            return;
        }
        if (nothing != null && nothing.getVisibility() == View.VISIBLE)
            nothing.setVisibility(View.GONE);
        if (mlist != null)
            mlist.clear();
        Log.i("123", "onGetPoiResult:"+result.error.toString());
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            List<PoiInfo> list = result.getAllPoi();
            for (int i = 0; i < list.size(); i++) {
                DestinationBean bean = new DestinationBean();
                bean.setLatitude(list.get(i).location.latitude);
                bean.setLongitude(list.get(i).location.longitude);
                bean.setName(list.get(i).name);
                bean.setStreet(list.get(i).address);
                mlist.add(bean);
            }
            adapter.notifyDataSetChanged();
            mListView.setVisibility(View.VISIBLE);
        }
        
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            String strInfo = "在";
            for (CityInfo cityInfo : result.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }
            strInfo += "找到结果";
            Toast.makeText(this, strInfo, Toast.LENGTH_LONG).show();
        }
        
    }
    
    private class SearchAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return  mlist.size();
        }

        @Override
        public Object getItem(int position) {
            return mlist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder mHolder = null;
            if (convertView == null) {
                mHolder = new Holder();
                convertView = getLayoutInflater().inflate(R.layout.suggest_address_item, null);
                mHolder.name = (TextView) convertView.findViewById(R.id.address_name);
                mHolder.street = (TextView) convertView.findViewById(R.id.address_street);
                mHolder.icon = (ImageView) convertView.findViewById(R.id.address_selected);
                
                convertView.setTag(mHolder);
            } else {
                mHolder = (Holder) convertView.getTag();
            }
            mHolder.name.setText(mlist.get(position).getName());
            mHolder.street.setHint(mlist.get(position).getStreet());
            mHolder.icon.setVisibility(View.INVISIBLE);
            
            return convertView;
        }
    }
    
    class Holder{
        TextView name;
        TextView street;
        ImageView icon;
    }
    
}
