package com.byg.android.hellokitty;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

public class StartActiveActivity extends Activity implements OnClickListener, OnGetRoutePlanResultListener {

    private static String TYPE_GATHER = "venue";            // 类型：集合地
    private static String TYPE_DESTINATION = "destination"; // 类型：目的地
    private String typeClick = "";                          // 记录点击类型

    private int desPosition;                              // 目的地的在list中的位置
    private LinearLayout gather_address = null;             // 集合地视图
    private LinearLayout destination_item = null;           // 目的地视图
    private TextView addressTV;                             // 集合地点
    private ScrollView scrollView = null;
    private Button mButton = null;                          // 添加目的地按钮
    private Button mCheck = null;                           // 查看线路图

//    private ImageLoader imageLoader = null;                 // 图片下载工具

//    private MapView mMapView = null;                       // 百度地图
//    private BaiduMap mBaiduMap = null;                     // 地图control
    private RouteLine route = null;
    private InfoWindow mInfoWindow = null;                 // 节点弹窗
    private UiSettings uiSettings  = null;                 // 百度地图UI设置界面
    private BaiduMapControl mControl = null;               // 自定义百度地图容器

    private RoutePlanSearch routeSearch = null;            // 规划线路
    private LatLng currentnNode = null;
    private LatLng endNode = null;
    
    private List<PlanNode> passNodeList = null;
    double lat;
    double lng;

    private DestinationBean venue = null;                   // 保存集合地的数据
    private ArrayList<DestinationBean> datas = new ArrayList<DestinationBean>();
    private ArrayList<LatLng> latlngList = new ArrayList<LatLng>();
    
    private BitmapDescriptor venue_marker                  // 集合点marker
                                = BitmapDescriptorFactory.fromResource(R.drawable.venue_marker);
    private BitmapDescriptor pass_marker                   // 经过点marker
                                = BitmapDescriptorFactory.fromResource(R.drawable.passby_marker);
    private BitmapDescriptor end_marker                    // 终点marker
                                = BitmapDescriptorFactory.fromResource(R.drawable.end_marker);

    Handler mhandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            // 地图描点或规划线路
            case 0x001:
                checkRoute();
                break;
            default:
                break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_active_layout);

//        imageLoader = ImageLoader.getInstance();
        
        initView();
        initData();

    }

    /**
     * 初始化视图
     * 
     */
    private void initView() {
        mControl = (BaiduMapControl) findViewById(R.id.baidu_mapview_control);
        gather_address = (LinearLayout) findViewById(R.id.gather_address);
        destination_item = (LinearLayout) findViewById(R.id.destination_ll);
        mButton = (Button) findViewById(R.id.add_destination);
        mCheck = (Button) findViewById(R.id.check_route);
//        mMapView = (MapView) findViewById(R.id.bmapView);
//        mBaiduMap = mMapView.getMap();
        
//        uiSettings = mBaiduMap.getUiSettings();
//        uiSettings.setZoomGesturesEnabled(false);   // 不允许缩放手势
//        uiSettings.setRotateGesturesEnabled(false); // 不允许旋转手势
//        uiSettings.setScrollGesturesEnabled(false); // 不允许平移手势
//        uiSettings.setCompassEnabled(false);        // 不允许指南针
        
        scrollView = (ScrollView) findViewById(R.id.srcollview_baidu);
        scrollView.requestDisallowInterceptTouchEvent(false);

        // 初始化线路搜索模块，注册事件监听
        routeSearch = RoutePlanSearch.newInstance();
        routeSearch.setOnGetRoutePlanResultListener(this);

        mButton.setOnClickListener(this);
        gather_address.setOnClickListener(this);
        mCheck.setOnClickListener(this);
        mControl.setOnClickListener(this);
        
        addressTV = (TextView) findViewById(R.id.gather_address_tv);
        
//        mControl.setImage(imageLoader);
    }
    
    private void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        // 选择目的地
        case R.id.gather_address:
            typeClick = TYPE_GATHER;
            selectGatherAddress(TYPE_GATHER);
            break;
        // 添加目的地
        case R.id.add_destination:
            addDestination();
            break;
        // 查看线路图
        case R.id.check_route:
            checkRoute();
            break;
        case R.id.baidu_mapview_control:
            Toast.makeText(StartActiveActivity.this, "跳转到地图页面。。", Toast.LENGTH_SHORT).show();
            break;
        default:
            break;
        }
    }

    /**
     * 查看线路
     * 
     */
    private void checkRoute() {
        if (venue != null && datas != null && datas.size() > 0) {
            mControl.setRoutePlan(latlngList);
        }
        // 当只有集合地点有数据时，只在地图上描一个集合marker
        else if (venue != null && datas != null && datas.isEmpty()) {
            mControl.setRoutePlan(new LatLng(venue.getLatitude(), venue.getLongitude()));
        }
    }

    /**
     * 在途经点上描上图标
     */
    public void markerPassBy() {
        for (int i = 0; i < passNodeList.size(); i++) {
            OverlayOptions options = new MarkerOptions().position(new LatLng(31.199904, 121.445892)).icon(pass_marker);
//            mBaiduMap.addOverlay(options);
        }
    }

    /**
     * 选择集合地点
     * 
     * @param type
     */
    private void selectGatherAddress(String type) {
        Intent intent = new Intent(getApplicationContext(), AddressDetailActivity.class);
        if (TYPE_GATHER.equals(type)) {
            if (venue == null) {
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
            } else {
                intent.putExtra("lat", venue.getLatitude());
                intent.putExtra("lng", venue.getLongitude());
            }
        } else if (TYPE_DESTINATION.equals(type)) {
            if (datas != null && datas.size() > 0 && desPosition < datas.size()) {
                DestinationBean bean = datas.get(desPosition);
                intent.putExtra("lat", bean.getLatitude());
                intent.putExtra("lng", bean.getLongitude());
                intent.putExtra("desCount", destination_item.getChildCount());
            } else {
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
                intent.putExtra("desCount", destination_item.getChildCount()); // 将目的地的数目传过去
            }
        }
        startActivityForResult(intent, 101);
    }

    /**
     * 添加目的地
     */
    private int desCount = 0;

    private void addDestination() {
        if (desCount < 5) {
            View view = getLayoutInflater().inflate(R.layout.destination_item, null);
            TextView tv1 = (TextView) view.findViewById(R.id.destination_num);
            view.setTag(desCount);
            destination_item.addView(view);
            tv1.setText("目的地 " + ++desCount);
            datas.add(new DestinationBean());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    typeClick = TYPE_DESTINATION;
                    desPosition = (int) v.getTag();
                    selectGatherAddress(TYPE_DESTINATION);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data != null) {
            // 设置集合地和目的地
            if (requestCode == 101 && resultCode == RESULT_OK) {
                getPlace(data);
                mhandler.sendEmptyMessage(0x001);
            }
        } else {
            // 删除目的地
            if (resultCode == 102) {
                deletaDestination();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 删除某个目的地
     * 
     */
    private void deletaDestination() {
        if (datas != null && datas.size() > 0) {
            destination_item.removeViewAt(desPosition);
            datas.remove(desPosition);
            int count = destination_item.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = destination_item.getChildAt(i);
                view.setTag(i);
            }
        }
    }

    /**
     * 获取地址
     * 
     * @param data
     */
    private void getPlace(Intent data) {
        latlngList.add(new LatLng(data.getDoubleExtra("lat", 0), data.getDoubleExtra("lng", 0)));
        DestinationBean bean = new DestinationBean();
        bean.setLatitude(data.getDoubleExtra("lat", 0));
        bean.setLongitude(data.getDoubleExtra("lng", 0));
        bean.setName(data.getStringExtra("name"));
        Log.i("123", "getPlace bean:" + bean.toString());
        if (typeClick.equals(TYPE_GATHER)) {
            venue = bean;
            currentnNode = new LatLng(data.getDoubleExtra("lat", 0), data.getDoubleExtra("lng", 0));
            addressTV.setText(venue.getName());
        } else {
            endNode = new LatLng(data.getDoubleExtra("lat", 0), data.getDoubleExtra("lng", 0));
            Log.i("123", "endNode latlng:" + endNode);
            View view = destination_item.getChildAt(desPosition);
            TextView streetTV = (TextView) view.findViewById(R.id.destination_address);
            streetTV.setText(bean.getName());
            datas.add(desPosition, bean);
        }
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(StartActiveActivity.this, "抱歉，未找到结果:" + result.error, Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            route = result.getRouteLines().get(0);
            for (int i = 0; i < route.getAllStep().size(); i++) {
                Object step = route.getAllStep().get(i);
//                ((DrivingRouteLine.DrivingStep) step).getWayPoints();
//                PolylineOptions line = new PolylineOptions()
//                                        .points(((DrivingRouteLine.DrivingStep) step).getWayPoints())
//                                        .color(Color.GREEN);
//                mBaiduMap.addOverlay(line);
            }
//            MyDrivingStepOverlay overlay = new MyDrivingStepOverlay(mBaiduMap);
//            mBaiduMap.setOnMarkerClickListener(overlay);
//            overlay.setData(result.getRouteLines().get(0));
//            overlay.addToMap();
//            overlay.zoomToSpan();
            List<LatLng> list = new ArrayList<LatLng>();
            list.add(currentnNode);
            list.add(endNode);
//            for (int i = 0; i < overlay.getOverlayOptions().size(); i++) {
////                LatLng latLng = ((PolylineOptions)overlay.getOverlayOptions().get(i)).getPoints().get(0);
//                list.add(latLng);
//            }
            PolylineOptions line = new PolylineOptions().points(list).color(Color.GREEN);
//            mBaiduMap.addOverlay(line);
            markerPassBy();
        }
    }

    /**
     * 驾车路线
     * 
     * @author byg
     *
     */
    private class MyDrivingStepOverlay extends DrivingRouteOverlay implements OnMarkerClickListener {

        private BaiduMap mMap;
        private DrivingRouteLine data;

        public MyDrivingStepOverlay(BaiduMap mMap) {
            super(mMap);
            this.mMap = mMap;
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            return venue_marker;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            return end_marker;
        }

        @Override
        public boolean onRouteNodeClick(int arg0) {
            Button mPop;
            mPop = new Button(getApplicationContext());
            mPop.setTextColor(Color.BLACK);
            mPop.setBackgroundResource(R.drawable.popup);
            mPop.setText(((DrivingRouteLine.DrivingStep) (data.getAllStep().get(arg0 - 1))).getExitInstructions());
            mInfoWindow = new InfoWindow(mPop, ((DrivingRouteLine.DrivingStep) (data.getAllStep().get(arg0)))
                    .getEntrace().getLocation(), 0);
            mMap.showInfoWindow(mInfoWindow);
//          ToastUtils.show(getApplicationContext(), arg0 + ((DrivingRouteLine.DrivingStep) (data.getAllStep().get(arg0))).getInstructions()+"<><><><><><>");
            return true;
        }

        @Override
        public void setData(DrivingRouteLine arg0) {
            super.setData(arg0);
            this.data = arg0;
        }
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult arg0) {
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult arg0) {
    }

}
