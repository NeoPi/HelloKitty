package com.byg.android.hellokitty;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.lbsapi.auth.LBSAuthManagerListener;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.baidu.navisdk.BNaviPoint;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BaiduNaviManager.OnStartNavigationListener;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;

public class NavigationTestActivity extends Activity implements OnGetRoutePlanResultListener {

    private static String TAG = NavigationTestActivity.class.getSimpleName();
    
    private Button naviButton = null;
    
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    
    private RoutePlanSearch routeSearch = null;                           // 规划线路
    double[] lng = {
     //  ----------------第一条线路-----------------------
//                     113.419254, 113.421122,
//                     121.484564, 120.275031,
//                     126.646398, 116.314591, 
//                     113.32521, 113.227951,
//                     91.128332
            
     //  ----------------第二条线路-----------------------      
                       121.445928,
                       121.097466,
                       120.986202,
                       119.402067,
                       113.628137,
                       117.024208,
                       116.404008
                   };
    
    double[] lat = {
     //  ----------------第一条线路-----------------------
//                     23.177427, 23.18158,
//                     31.245284, 30.177971,
//                     46.00394, 40.016282, 
//                     23.14008, 23.110758, 
//                     29.656935
            
     //  ----------------第二条线路-----------------------
                       31.199634,
                       31.470234,
                       31.376111,
                       32.401051,
                       34.724593,
                       36.631252,
                       39.912732

    };
    
    private ArrayList<PlanNode> passNodeList = new ArrayList<PlanNode>(); // 途经点
    
    private BitmapDescriptor venue_marker                                 // 集合点marker
                            = BitmapDescriptorFactory.fromResource(R.drawable.venue_marker);
    private BitmapDescriptor pass_marker                                  // 经过点marker
                            = BitmapDescriptorFactory.fromResource(R.drawable.passby_marker);
    private BitmapDescriptor end_marker                                   // 终点marker
                            = BitmapDescriptorFactory.fromResource(R.drawable.end_marker);
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_test_layout);
        
        initView();
        initNavigation();
        naviButton = (Button) findViewById(R.id.button1);
        naviButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                launcherNavigation();
            }
        });
        
     // 初始化线路搜索模块，注册事件监听
        routeSearch = RoutePlanSearch.newInstance();
        routeSearch.setOnGetRoutePlanResultListener(this);
        
        initPlanNode();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
    }

    /**
     * 初始化路经点
     */
    private void initPlanNode() {
        PlanNode venNode = null;
        PlanNode endNode = null;
        
        for (int i = 0; i < lat.length; i++) {
            if (i == 0) {
                venNode = PlanNode.withLocation(new LatLng(lat[i], lng[i]));
                passNodeList.add(venNode);
            } else if (i == lat.length - 1) {
                endNode = PlanNode.withLocation(new LatLng(lat[i], lng[i]));
                passNodeList.add(endNode);
            } else {
                PlanNode pass = PlanNode.withLocation(new LatLng(lat[i], lng[i]));
                passNodeList.add(pass);
            }
        }
        
        routeSearch.drivingSearch(new DrivingRoutePlanOption().from(venNode).passBy(passNodeList).to(endNode));
    }

    //导航引擎初始化
    private void initNavigation() {
    //导航初始化
        BaiduNaviManager.getInstance().initEngine(this, getSdcardDir(),
                mNaviEngineInitListener, new LBSAuthManagerListener() {
                    @Override
                    public void onAuthResult(int status, String msg) {
                        String str = null;
                        if (0 == status) {
                            str = "key 验证成功，" + msg;
                        } else {
                            str = "key 验证失败," + msg;
                        }
                        Log.i("123", str);
                    }
                });
    }
    
    private String getSdcardDir() {  
        if (Environment.getExternalStorageState().equalsIgnoreCase(  
                Environment.MEDIA_MOUNTED)) {  
            return Environment.getExternalStorageDirectory().toString();  
        }  
        return null;  
    }

    //导航监听器  
    private boolean mIsEngineInitSuccess = BaiduNaviManager.getInstance().checkEngineStatus(NavigationTestActivity.this);  
    private NaviEngineInitListener mNaviEngineInitListener = new NaviEngineInitListener() {  
            public void engineInitSuccess() {  
                //导航初始化是异步的，需要一小段时间，以这个标志来识别引擎是否初始化成功，为true时候才能发起导航  
                mIsEngineInitSuccess = true;
            }  
     
            public void engineInitStart() {  
            }  
     
            public void engineInitFail() {  
            }  
        };
        
    //导航路径点
    private void launcherNavigation(){
        if (!mIsEngineInitSuccess) {
            Toast.makeText(NavigationTestActivity.this, "导航初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }
        BNaviPoint point = null;
        List<BNaviPoint> mPoints = new ArrayList<BNaviPoint>();
        for (int i = 0; i < lat.length; i++) {
            point = new BNaviPoint(lng[i],lat[i], "广州", BNaviPoint.CoordinateType.BD09_MC);
            mPoints.add(point);
        }
        
        BaiduNaviManager.getInstance().launchNavigator(this,
                mPoints,                                           //路线点列表  
                NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME,         //算路方式  
                true,                                              //真实导航  
                BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY, new OnStartNavigationListener() {

                    @Override
                    public void onJumpToDownloader() {

                    }

                    @Override
                    public void onJumpToNavigator(Bundle configParams) {
                        //跳转监听  
                        Intent intent = new Intent(NavigationTestActivity.this, BNavigatorActivity.class);
                        intent.putExtras(configParams);
                        startActivity(intent);
                    }
                });
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(NavigationTestActivity.this, "抱歉，未找到结果:" + result.error, Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            mBaiduMap.clear();
            MyDrivingStepOverlay overlay = new MyDrivingStepOverlay(mBaiduMap,result.getRouteLines().get(0));
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }
    
    /**
     * 在途经点上描上图标
     * @param list 
     */
    private void markerPassBy(List<OverlayOptions> list) {
        for (int i = 0; i < passNodeList.size(); i++) {
            if (i == 0) {
                OverlayOptions options = new MarkerOptions().position(new LatLng(lat[i],lng[i])).icon(venue_marker).zIndex(i);
                mBaiduMap.addOverlay(options);
                list.add(options);
            } else if (i == passNodeList.size() -1) {
                OverlayOptions options = new MarkerOptions().position(new LatLng(lat[i],lng[i])).icon(end_marker).zIndex(i);
                mBaiduMap.addOverlay(options);
                list.add(options);
            } else {
                OverlayOptions options = new MarkerOptions().position(new LatLng(lat[i],lng[i])).icon(pass_marker).zIndex(i);
                mBaiduMap.addOverlay(options);
                list.add(options);
            }
        }
    }
    
    private class MyDrivingStepOverlay extends OverlayManager{
        
        @Override
        public void zoomToSpan() {
            super.zoomToSpan();
        }

        private DrivingRouteLine result ;
        private List<LatLng> latLngs ;
        
        public MyDrivingStepOverlay(BaiduMap arg0,DrivingRouteLine result) {
            super(arg0);
            this.result = result;
            latLngs = new ArrayList<LatLng>();
            initData();
        }
        
        /**
         * 初始化数据
         */
//        private void initData() {
//            List<RouteNode> step = result.getWayPoints();
//            for (int i = 0; i < step.size(); i++) {
//                latLngs.add(step.get(i).getLocation());
//            }
//        }
        /**
         * 初始化数据
         */
        private void initData() {
            for (int i = 0; i < result.getAllStep().size(); i++) {
                Object step = result.getAllStep().get(i);
                latLngs.add(((DrivingRouteLine.DrivingStep) step).getExit().getLocation());
            }
        }
        @Override
        public boolean onMarkerClick(Marker marker) {
            Button mPop;
            mPop = new Button(getApplicationContext());
            mPop.setTextColor(Color.WHITE);
            mPop.setBackgroundResource(R.drawable.popup);
            
            Toast.makeText(NavigationTestActivity.this, marker.getZIndex()+"", Toast.LENGTH_SHORT).show();
//            if (marker.equals(venMarker)) {
//                clickLatLng = new LatLng(venue.getLatitude(), venue.getLngitude());
////                ToastUtils.show(ActiveRoutePlanActivity.this, "集合点");
//                if (currentLatLng != null) {
//                    clickResult = venue;
//                    PlanNode start = PlanNode.withLocation(clickLatLng);
//                    PlanNode end = PlanNode.withLocation(currentLatLng);
//                    mPop.setText(venue.getDescription());
//                    mInfoWindow = new InfoWindow(mPop, clickLatLng, -50);
//                    mBaiduMap.showInfoWindow(mInfoWindow);
//                    mPlanSearch.drivingSearch(new DrivingRoutePlanOption().from(start).to(end).policy(DrivingPolicy.ECAR_DIS_FIRST));
//                }
//            } else {
//                PlanNode start = null;
//                PlanNode end = null;
//                for (int i = 0; i < markerList.size(); i++) {
//                    Logs.i(TAG, mlist.get(i).toString());
//                    if (marker.equals(markerList.get(i))){
//                        clickLatLng = new LatLng(mlist.get(i).getLatitude(), mlist.get(i).getLngitude());
//                        clickResult = mlist.get(i);
//                        break;
//                    } 
//                }
//                if (mPlanSearch != null && currentLatLng != null && clickLatLng != null) {
//                    start = PlanNode.withLocation(currentLatLng);
//                    end = PlanNode.withLocation(clickLatLng);
//                    mPop.setText(clickResult.getDescription());
//                    mInfoWindow = new InfoWindow(mPop, clickLatLng, -50);
//                    mBaiduMap.showInfoWindow(mInfoWindow);
//                    mPlanSearch.drivingSearch(new DrivingRoutePlanOption().from(start).to(end).policy(DrivingPolicy.ECAR_DIS_FIRST));
//                }
//            }
            return true;
        }
        
        @Override
        public List<OverlayOptions> getOverlayOptions() {
            Log.i(TAG, latLngs.size()+"");
            Toast.makeText(NavigationTestActivity.this, latLngs.size()+"", Toast.LENGTH_SHORT).show();
            List<OverlayOptions> list = new ArrayList<OverlayOptions>();
            for (int i = 0; i < result.getAllStep().size(); i++) {
                OverlayOptions ooPolyline = new PolylineOptions().width(8)
                        .color(0xAA64C864).points(latLngs);
                mBaiduMap.addOverlay(ooPolyline); 
            }
            markerPassBy(list);
            return list;
        }
        
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult arg0) {
        
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult arg0) {
        
    }
    
}
