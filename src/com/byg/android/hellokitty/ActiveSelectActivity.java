package com.byg.android.hellokitty;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import android.R.anim;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author byg
 */
public class ActiveSelectActivity extends FragmentActivity implements OnGetRoutePlanResultListener,
                                    OnClickListener, OnGetGeoCoderResultListener, OnMapClickListener{

    private static final LatLng GEO_BEIJING = new LatLng(40.057092, 116.307171);// 可以根据需要来定位获取
    
    private BaiduMap mMap = null;
    private SupportMapFragment supportMapFragment = null;
    private RoutePlanSearch routeSearch = null;
    OverlayManager routeOverlay = null;
    
    
    BitmapDescriptor bdA = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_marka);
    BitmapDescriptor bdB = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_markb);
    BitmapDescriptor bdC = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_markc);
    BitmapDescriptor bdD = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_markd);
    
    
    Marker mMarkerD;
    
    private boolean drivingSelf = false; // 是否为自驾游 true为自驾游，false为非自驾游
    private RelativeLayout mRelativelayout = null;
    private TextView address = null;
    
    private GeoCoder mSearch = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.active_layout);
        
        initView();
        if (drivingSelf) {
            routePlan(); // 显示线路规划
        } else {
//            showAddgrate();//　显示集合点
            showAddressByLatlng();
        }
    }



    private void initView() {
        address = (TextView) findViewById(R.id.aggregate_address);
        mRelativelayout = (RelativeLayout) findViewById(R.id.aggregate_layout);
        mRelativelayout.setOnClickListener(this);
        MapStatusUpdate u1 = MapStatusUpdateFactory.newLatLng(GEO_BEIJING);
        supportMapFragment = (SupportMapFragment) (getSupportFragmentManager()
                .findFragmentById(R.id.fragment_active_address));
        mMap = supportMapFragment.getBaiduMap();
        mMap.setMyLocationEnabled(true);
        mMap.setMapStatus(u1);
        mMap.setOnMapClickListener(this);
        if (drivingSelf) {
        // 初始化搜索模块，注册事件监听
            routeSearch = RoutePlanSearch.newInstance();
            routeSearch.setOnGetRoutePlanResultListener(this);
        } else {
            mSearch = GeoCoder.newInstance();
            mSearch.setOnGetGeoCodeResultListener(this);
        }
        
    }
    
    private void showAddgrate() {
        MyLocationData locData = new MyLocationData.Builder()
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(0).latitude(31.199709).longitude(121.445954).build();
        // 设置定位数据
        mMap.setMyLocationData(locData);
        mMap.setMyLocationConfigeration(new MyLocationConfiguration(LocationMode.NORMAL, true, bdA));
        LatLng newlatLng = new LatLng(31.199709, 121.445954);
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(newlatLng);
        mMap.animateMapStatus(u);
    }
    
    /**
     * 直接根据经纬度反地理编码得到地点名，并显示在地图上
     */
    private void showAddressByLatlng(){
        // 反Geo搜索
        mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                .location(GEO_BEIJING)); 
    }
    
    private void routePlan() {
        mMap.clear();
        //设置终点起点信息
        PlanNode startNode = PlanNode.withLocation(new LatLng(31.199709, 121.445954));
        PlanNode endNode = PlanNode.withLocation(new LatLng(31.198538, 121.43034));
        PlanNode passNode = PlanNode.withLocation(new LatLng(31.198375, 121.430243));
        PlanNode passNode2 = PlanNode.withLocation(new LatLng(31.203812, 121.441706));
        List<PlanNode> passByNodes = new ArrayList<PlanNode>();
//        passByNodes.add(passNode);
        passByNodes.add(passNode2);
        routeSearch.drivingSearch(new DrivingRoutePlanOption()
                    .from(startNode)
                    .passBy(passByNodes)
                    .to(endNode));
    }
    
    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(ActiveSelectActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mMap);
            routeOverlay = overlay;
            mMap.setOnMarkerClickListener(overlay);
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
            
            // 此方法是添加途经点的覆盖icon
            OverlayOptions mOverlayOptions = new MarkerOptions().position(new LatLng(31.203812, 121.441706)).icon(bdD)
                    .perspective(false).zIndex(7);
            mMarkerD = (Marker) (mMap.addOverlay(mOverlayOptions));
        }
    }
    @Override
    public void onGetTransitRouteResult(TransitRouteResult arg0) {
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult arg0) {
        
    }

  /**
   * 自定义RouteOverly
   * @author byg
   */
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }
        @Override
        public void setData(DrivingRouteLine arg0) {
            super.setData(arg0);
        }
        @Override
        public BitmapDescriptor getStartMarker() {
            return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.aggregate_layout:
            Toast.makeText(getApplicationContext(), "页面跳转", Toast.LENGTH_SHORT).show();
            break;
        default:
            break;
        }
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult arg0) {
        
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(ActiveSelectActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG).show();
            return;
        }
        mMap.clear();
        mMap.addOverlay(new MarkerOptions().position(result.getLocation())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_marka)));
        mMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result.getLocation()));
        
        address.setText(result.getAddress());
        Toast.makeText(ActiveSelectActivity.this, result.getAddress(),Toast.LENGTH_LONG).show();
    }



    @Override
    public void onMapClick(LatLng arg0) {
        Toast.makeText(getApplicationContext(), "页面跳转",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), AddressDetailActivity.class);
        startActivity(intent);
        overridePendingTransition(0,anim.slide_in_left);
    }

    @Override
    public boolean onMapPoiClick(MapPoi arg0) {
        Toast.makeText(getApplicationContext(), arg0.getName(),Toast.LENGTH_SHORT).show();
        return false;
    }
}
