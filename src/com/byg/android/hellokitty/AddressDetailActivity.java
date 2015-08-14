package com.byg.android.hellokitty;

import java.util.ArrayList;
import java.util.List;

import android.R.anim;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

public class AddressDetailActivity extends Activity implements OnClickListener, OnMapStatusChangeListener,
        OnGetGeoCoderResultListener, OnMapClickListener, OnItemClickListener {

    private static int SEARCH_DESTINATION = 0;          // 搜索目的地请求码
    private static int UPDATE_MARKER = 0x001;
    private static int UPDATE_ADDRESS = 0x002;
    
    private TextView title = null;
    private ImageView backIV = null;
    private ImageView relocationIV = null;              // 将定位点拉回到屏幕中心的按钮
    private TextView okTv = null;
    private TextView addressTV = null;
    private ListView mListView = null;
    private ArrayList<DestinationBean> mlist = null;
    private MapView mMapView = null;
    private BaiduMap mBaiduMAp = null;
    private LatLng mlatlng;                            // 定位经纬度
    private LatLng currentLatLng;                      // 记录地图中心点的经纬度(由于地图的拖动引起地图中心点经纬度发生改变)
    private String address = "";                       // 记录地址
    private String city = "";                          // 记录定位所得城市名
    private GeoCoder mGeoCoder = null;
    private LocationClient mLocClient = null;
    private MyLocationListenner myListener = new MyLocationListenner();
    private SearchAdapter adapter = null;
    private ProgressBar mProgressBar = null;            // 加载地址菊花

    private boolean isFirst = true;                     // 是否是第一次定位
    private Button search = null;                       // 搜索按钮
    private Button delete = null;                       // 删除按钮
    
    private int desCount = 0;                           // 目的地计数
    
    private DestinationBean selectBean = null;

    private Marker mMarker;
    OverlayOptions ooA = null;
    OverlayOptions ooB = null;

    BitmapDescriptor bdA = BitmapDescriptorFactory.fromResource(R.drawable.icon_marker);

    BitmapDescriptor bdLocation = BitmapDescriptorFactory.fromResource(R.drawable.icon_location);

    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case 0x001: // 绘制中心点图标
                updateMarker(currentLatLng);
                break;
            case 0x002:
                updateAddress();
                break;
            default:
                break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.active_address_detail_layout);
        Bundle bundle = getIntent().getExtras();
        initView();
        if (bundle != null) {
            double mlat = bundle.getDouble("lat", 0);
            double mlng = bundle.getDouble("lng", 0);
            desCount = bundle.getInt("desCount",0);
            if (desCount < 2 && delete != null) {
                // 如果目的地的数目 小于等于2时 使删除本目的地的按钮不可见 
//                delete.setVisibility(View.GONE);
            }
            if (mlat != 0 && mlng != 0) {
                isFirst = false;
                currentLatLng = new LatLng(mlat, mlng);
                setLocation();
            }
        }
        initLocation();
        initScreenParams();
    }

    /**
     * 更新确认地址
     */
    protected void updateAddress() {
        if (addressTV != null && selectBean != null) {
            addressTV.setText(selectBean.getStreet().toString());
        }
    }

    /**
     * 定位
     */
    private void initLocation() {
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setIsNeedAddress(true);// 需要定位到地址的详细信息
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    /**
     * 根据经纬度来设置在地图上描点
     */
    private void setLocation() {
        mHandler.sendEmptyMessage(UPDATE_MARKER);
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(currentLatLng);
        mBaiduMAp.animateMapStatus(u);
        mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(currentLatLng));
    }

    private void initView() {

        okTv = (TextView) findViewById(R.id.public_top_right_tv);
        title = (TextView) findViewById(R.id.public_top_title);
        backIV = (ImageView) findViewById(R.id.public_top_back);
        mMapView = (MapView) findViewById(R.id.bmapView);
        search = (Button) findViewById(R.id.search_destination);
        delete = (Button) findViewById(R.id.delete_destination);
        mListView = (ListView) findViewById(R.id.address_listview);
        addressTV = (TextView) findViewById(R.id.address_edit);
        mProgressBar = (ProgressBar) findViewById(R.id.active_address_progressBar);
        relocationIV = (ImageView) findViewById(R.id.baidumap_location);

        mlist = new ArrayList<DestinationBean>();
        adapter = new SearchAdapter();
        mListView.setAdapter(adapter);

        mBaiduMAp = mMapView.getMap();
        mBaiduMAp.setMyLocationEnabled(true);
        mBaiduMAp.setMyLocationConfigeration(new MyLocationConfiguration(LocationMode.NORMAL, true, bdLocation)); // 修改定位默认图标
        mMapView.showZoomControls(false);   // 一处 (-,+)按钮  效果同  mMapView.removeViewAt(2);
        MapStatusUpdate statusUpdate = MapStatusUpdateFactory.zoomTo(17); // 设置地图默认缩放级别
        mBaiduMAp.animateMapStatus(statusUpdate);

        relocationIV.setOnClickListener(this);
        mListView.setOnItemClickListener(this);
        search.setOnClickListener(this);
        delete.setOnClickListener(this);
        mBaiduMAp.setOnMapStatusChangeListener(this);
//        mBaiduMAp.setOnMapClickListener(this);
        okTv.setOnClickListener(this);
        backIV.setOnClickListener(this);

        title.setText("活动地址");
        // 地理编码（经纬度与地址的转换）
        mGeoCoder = GeoCoder.newInstance();
        mGeoCoder.setOnGetGeoCodeResultListener(this);

    }

    /**
     * 用来更新地图中心marker的位置
     * 
     * @param llA
     *            marker的经纬度
     */
    private void updateMarker(LatLng llA) {

        ooA = new MarkerOptions().position(llA).icon(bdA).draggable(true).zIndex(9);
        if (mMarker != null) {
            mMarker.remove();
        }
        mMarker = (Marker) mBaiduMAp.addOverlay(ooA);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.public_top_back:
            finish();
            overridePendingTransition(0, anim.slide_out_right);
            break;
        case R.id.public_top_right_tv:
            saveAddress();
            break;
        case R.id.delete_destination:
            deleteDestination();
            break;
        case R.id.search_destination: // 跳转搜索目的地界面
            searchDestination();
            break;
        case R.id.baidumap_location:
            reLocation();
            break;
        default:
            break;
        }
    }

    
    /**
     * 删除目的地
     * 
     */
    private void deleteDestination() {
        if (desCount > 1) {
            setResult(102,null);
            finish();
            overridePendingTransition(0, anim.slide_out_right);
        } else {
            Toast.makeText(getApplicationContext(), "目的地不能少于2个", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 从新将定位点拉回到屏幕中心
     */
    private void reLocation() {
        currentLatLng = mlatlng;
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(currentLatLng);
        mBaiduMAp.animateMapStatus(u);
        mHandler.sendEmptyMessage(UPDATE_MARKER);
    }

    /**
     * 跳转搜索目的地
     */
    private void searchDestination() {
        Intent intent = new Intent(AddressDetailActivity.this, SearchDestinationActivity.class);
        intent.putExtra("city", city);
        startActivityForResult(intent, SEARCH_DESTINATION);
    }

    private void saveAddress() {
        if (selectBean == null) {
            Toast.makeText(getApplicationContext(), "定位中，请稍后再试", Toast.LENGTH_SHORT).show();
            return ;
        }
        Intent intent = new Intent();
        intent.putExtra("name", selectBean.getStreet());
        intent.putExtra("lat", selectBean.getLatitude());
        intent.putExtra("lng", selectBean.getLongitude());
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(0, anim.slide_out_right);
    }

    private void initScreenParams() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Env.screenWidth = metrics.widthPixels;
        Env.screenHeight = metrics.heightPixels;
    }

    /**
     * 实现百度定位的监听接口
     */
    private class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(final BDLocation location) {
            if (location == null || location.getLocType() == BDLocation.TypeNone
                    || location.getLocType() == BDLocation.TypeServerError) {
                Toast.makeText(AddressDetailActivity.this, "定位失败，请确保开启位置服务!", Toast.LENGTH_SHORT).show();
                return;
            }
            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(0).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
            // 设置定位数据
//            mBaiduMAp.setMyLocationData(locData);
            mlatlng = new LatLng(location.getLatitude(), location.getLongitude());
            city = location.getCity();
            OverlayOptions options = new MarkerOptions()
                                .anchor(0.5f, 0.5f)// 默认（0.5,1.0f）
                                .position(mlatlng)
                                .icon(bdLocation).draggable(true).zIndex(1);
            mBaiduMAp.addOverlay(options);
            if (isFirst) {
                currentLatLng = mlatlng;
                mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(mlatlng));
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(mlatlng);
                mBaiduMAp.animateMapStatus(u);
                mHandler.sendEmptyMessage(UPDATE_MARKER);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SEARCH_DESTINATION && resultCode == RESULT_OK) {
            DestinationBean bean = (DestinationBean) data.getSerializableExtra("result");
            currentLatLng = new LatLng(bean.getLatitude(), bean.getLongitude());
            address = bean.getName();
            mHandler.sendEmptyMessage(UPDATE_MARKER);
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(currentLatLng);
            mBaiduMAp.animateMapStatus(u);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onMapStatusChangeStart(MapStatus arg0) {
    }

    @Override
    public void onMapStatusChange(MapStatus arg0) {
        Log.i("123", "onMapStatusChange:" + arg0.target);
        currentLatLng = arg0.target;
        mHandler.sendEmptyMessage(UPDATE_MARKER);
    }

    /**
     * 拖动地图结束后会调的方法。。
     */
    @Override
    public void onMapStatusChangeFinish(MapStatus arg0) {

        if (mProgressBar != null && mProgressBar.getVisibility() == View.GONE) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        currentLatLng = arg0.target;
        mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(currentLatLng));
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult arg0) {
        Log.i("123", "onGetGeoCodeResult :" + arg0.getLocation().toString());
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {
        if (mProgressBar != null && mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.GONE);
        }
        if (arg0 == null || arg0.getPoiList() == null || arg0.getPoiList().size() <= 0) {
            return;
        }
        Log.i("123", "onGetReverseGeoCodeResult: " + arg0.getPoiList().size());
        try {
            mlist.clear();
            address = arg0.getPoiList().get(0).name;
            DestinationBean bean = new DestinationBean();
            bean.setLatitude(arg0.getLocation().latitude);
            bean.setLongitude(arg0.getLocation().longitude);
            bean.setStreet(arg0.getAddress());
            bean.setName(arg0.getAddressDetail().district);
            bean.setSelect(true);
            selectBean = bean;
//            bean.setType(type);
            mlist.add(bean);
            List<PoiInfo> list = arg0.getPoiList();
            for (int i = 0; i < arg0.getPoiList().size(); i++) {
                DestinationBean bean1 = new DestinationBean();
                bean1.setName(list.get(i).name);
                bean1.setStreet(list.get(i).address);
                bean1.setLatitude(list.get(i).location.latitude);
                bean1.setLongitude(list.get(i).location.longitude);
                bean1.setSelect(false);
                mlist.add(bean1);
            }
            adapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mHandler.sendEmptyMessage(UPDATE_MARKER);
            mHandler.sendEmptyMessage(UPDATE_ADDRESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapClick(LatLng arg0) {

    }

    @Override
    public boolean onMapPoiClick(MapPoi arg0) {
        Toast.makeText(getApplicationContext(), arg0.getName(), Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mLocClient != null) {
            // 退出时销毁定位
            mLocClient.stop();
        }
//         关闭定位图层
        mBaiduMAp.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        currentLatLng = new LatLng(mlist.get(position).getLatitude(), mlist.get(position).getLongitude());
////        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(currentLatLng);
////        mBaiduMAp.animateMapStatus(u);
        if(!mlist.get(position).equals(selectBean)){
            for (int i = 0; i < mlist.size(); i++) {
                if (mlist.get(i).equals(selectBean)) {
                    mlist.get(i).setSelect(false);
                }else {
                    mlist.get(position).setSelect(true);
                }
            }
            selectBean = mlist.get(position);
        }
        adapter.notifyDataSetChanged();
        mHandler.sendEmptyMessage(UPDATE_MARKER);
        mHandler.sendEmptyMessage(UPDATE_ADDRESS);
    }

    private class SearchAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mlist.size();
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
            if (position == 0) {
                mHolder.name.setText("[位置]");
                mHolder.street.setHint(mlist.get(position).getStreet());
                mHolder.icon.setVisibility(View.VISIBLE);
            } else {
                mHolder.name.setText(mlist.get(position).getName());
                mHolder.street.setHint(mlist.get(position).getStreet());
                mHolder.icon.setVisibility(View.INVISIBLE);
            }
            if (mlist.get(position).isSelect()) {
                mHolder.icon.setVisibility(View.VISIBLE);
            }else {
                mHolder.icon.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }
    }

    class Holder {
        TextView name;
        TextView street;
        ImageView icon;
    }

}
