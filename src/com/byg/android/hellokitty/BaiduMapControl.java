package com.byg.android.hellokitty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMap.SnapshotReadyCallback;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption.DrivingPolicy;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * @ClassName: BaiduMapControl
 * @Description: 用于显示活动地址或线路 
 * @author byg
 * @date 2015-03-05
 * 
 */
public class BaiduMapControl extends LinearLayout implements OnGetRoutePlanResultListener{

    private static String TAG = BaiduMapControl.class.getSimpleName();
    
    private static String URL = "http://api.map.baidu.com/staticimage?center=116.403874,39.914888&width=500&height=500&zoom=11&paths=116.288891,40.004261;116.487812,40.017524;116.525756,39.967111;116.536105,39.872373|116.442968,39.797022;116.270494,39.851993;116.275093,39.935251;116.383177,39.923743&pathStyles=0xff0000,5,1";
    
    private Context context;
    private MapView mMapView = null;                                      // 百度view
    private BaiduMap mBaiduMap = null;                                    // 地图
    private ImageView imageView = null;                                   // 用于显示截屏的图片
    private InfoWindow mInfoWindow = null;                                // 节点弹窗
    private RoutePlanSearch routeSearch = null;                           // 规划线路
    private Bitmap mBitmap = null;
    
    private ImageLoader imageLoader = null;                               // 图片下载工具
    
    private boolean isPlan = false;
    
    private ArrayList<PlanNode> passNodeList = new ArrayList<PlanNode>(); // 途经点
    private ArrayList<LatLng> passLatLngs = new ArrayList<LatLng>();      // 途经点的经纬度集合
    
    private BitmapDescriptor venue_marker                                 // 集合点marker
                        = BitmapDescriptorFactory.fromResource(R.drawable.venue_marker);
    private BitmapDescriptor pass_marker                                  // 经过点marker
                        = BitmapDescriptorFactory.fromResource(R.drawable.passby_marker);
    private BitmapDescriptor end_marker                                   // 终点marker
                        = BitmapDescriptorFactory.fromResource(R.drawable.end_marker);
    
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case 0x001:
                markerPassBy();
                mHandler.sendEmptyMessage(0x002); // 截图并设置到地图页面
                break;
            case 0x002:
                snapshotScreen();
                break;
            case 0x003:
//                mMapView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(mBitmap); // 以流的形式从sd卡读出图片
//                imageView.setImageBitmap(BitmapFactory.decodeFile("/mnt/sdcard/test.png")); // 以流的形式从sd卡读出图片
            default:
                break;
            }
        };
    };
    public BaiduMapControl(Context context) {
        super(context);
        this.context = context;
        
        inflatelayout();
        initView();
    }
    
    /**
     * 屏幕截图并显示
     */
    protected void snapshotScreen() {
     // 截图，在SnapshotReadyCallback中保存图片到 sd 卡
        mBaiduMap.snapshot(new SnapshotReadyCallback() {
            public void onSnapshotReady(Bitmap snapshot) {
                mBitmap = snapshot;
                File file = new File("/mnt/sdcard/test.png");
                FileOutputStream out;
                try {
                    out = new FileOutputStream(file);
                    if (snapshot.compress(
                            Bitmap.CompressFormat.PNG, 100, out)) {
                        out.flush();
                        out.close();
                    }
                    mHandler.sendEmptyMessage(0x003);
                    Toast.makeText(context,
                            "屏幕截图成功，图片存在: " + file.toString(),
                            Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Toast.makeText(context, "正在截取屏幕图片...",
                Toast.LENGTH_SHORT).show();
    }

    public BaiduMapControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        inflatelayout();
        initView();
    }

    public BaiduMapControl(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        
        inflatelayout();
        initView();
    }

    /**
     * 解析布局
     */
    private void inflatelayout(){
        LayoutInflater.from(context).inflate(R.layout.baidu_mapview_control_layout, this);
        imageLoader = ImageLoader.getInstance();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        imageView                           = (ImageView) findViewById(R.id.img_view);
        mMapView                            = (MapView) findViewById(R.id.bmapView);
        mMapView.removeViewAt(2);
        mBaiduMap = mMapView.getMap();
        
//        imageLoader.displayImage(URL, imageView);
        
        // 初始化线路搜索模块，注册事件监听
        routeSearch = RoutePlanSearch.newInstance();
        routeSearch.setOnGetRoutePlanResultListener(this);
        
        mBaiduMap.setOnMapLoadedCallback(new OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mHandler.sendEmptyMessage(0x002);
            }
        });
        
        mBaiduMap.setOnMapStatusChangeListener(new OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus arg0) {
                
            }
            
            @Override
            public void onMapStatusChangeFinish(MapStatus arg0) {
                isPlan = true;
                
            }
            
            @Override
            public void onMapStatusChange(MapStatus arg0) {
                
            }
        });
    }

    /**
     * 设置只有一个经纬度点时的显示
     * @param mLatLng 需要显示在地图上的一个点
     */
    public void setRoutePlan(LatLng mLatLng){
        OverlayOptions options = new MarkerOptions().icon(venue_marker).position(mLatLng);
        mBaiduMap.addOverlay(options);
        MapStatusUpdate statusUpdate = MapStatusUpdateFactory.newLatLng(mLatLng);
        mBaiduMap.animateMapStatus(statusUpdate);
        mHandler.sendEmptyMessage(0x002);
    }
    
    /**
     * 显示多个经纬度点的线路
     * @param mlist 经纬度点的集合
     */
    public void setRoutePlan(List<LatLng> mlist) {
        PlanNode venueNode = null;                                    // 集合点
        PlanNode endNode = null;                                      // 目的地
        Log.i("123", "setRoutePlan:"+mlist.size());
        if (routeSearch != null && mlist != null && !mlist.isEmpty()) {
            if (mlist.size() == 1) {
                setRoutePlan(mlist.get(0));
            } else if (mlist.size() == 2) {
                for (int i = 0; i < mlist.size(); i++) {
                    if (i == 0) {
                        Log.i("123", "setRoutePlan i == 0  latlng:"+ mlist.get(0).toString());
                        venueNode = PlanNode.withLocation(mlist.get(0));
                    } else {
                        Log.i("123", "setRoutePlan i == 1  latlng:"+ mlist.get(1).toString());
                        endNode = PlanNode.withLocation(mlist.get(1));
                    }
                }
                routeSearch.drivingSearch(new DrivingRoutePlanOption().from(venueNode).to(endNode)
                        .policy(DrivingPolicy.ECAR_DIS_FIRST));
            } else if (mlist.size() > 2) {
                for (int i = 0; i < mlist.size(); i++) {
                    if (i == 0) {
                        venueNode = PlanNode.withLocation(mlist.get(i));
                    } else if (i > 0 && i < mlist.size()-1) {
                        passLatLngs.add(mlist.get(i));
                        PlanNode pass = PlanNode.withLocation(mlist.get(i));
                        passNodeList.add(pass); 
                    } else if (i == mlist.size()-1) {
                        endNode = PlanNode.withLocation(mlist.get(i));
                    }
                }
                routeSearch.drivingSearch(new DrivingRoutePlanOption().from(venueNode).passBy(passNodeList).to(endNode)
                        .policy(DrivingPolicy.ECAR_DIS_FIRST));
            }
        }
    }
    
    /**
     * 下载图片并显示
     * @param imageLoader
     */
    public void setImage(ImageLoader imageLoader){
        imageLoader.displayImage(URL, imageView);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        return super.onTouchEvent(event);
        return true;
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return super.onInterceptTouchEvent(ev);
        return true;
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
//        return super.dispatchTouchEvent(ev);
        return true;
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(context, "抱歉，未找到结果:" + result.error, Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            mBaiduMap.clear();
            MyDrivingStepOverlay overlay = new MyDrivingStepOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
            isPlan = true;
            mHandler.sendEmptyMessage(0x001); // 更新经过点marker
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
            mPop = new Button(context);
            mPop.setTextColor(Color.BLACK);
            mPop.setBackgroundResource(R.drawable.popup);
            mPop.setText(((DrivingRouteLine.DrivingStep) (data.getAllStep().get(arg0 - 1))).getExitInstructions());
            mInfoWindow = new InfoWindow(mPop, ((DrivingRouteLine.DrivingStep) (data.getAllStep().get(arg0)))
                    .getEntrace().getLocation(), 0);
            mMap.showInfoWindow(mInfoWindow);
            return true;
        }

        @Override
        public void setData(DrivingRouteLine arg0) {
            super.setData(arg0);
            this.data = arg0;
        }
    }
    /**
     * 在途经点上描上图标
     */
    private void markerPassBy() {
        for (int i = 0; i < passNodeList.size(); i++) {
            OverlayOptions options = new MarkerOptions().position(passLatLngs.get(i)).icon(pass_marker);
            mBaiduMap.addOverlay(options);
        }
    }
    
    @Override
    public void onGetTransitRouteResult(TransitRouteResult arg0) {
        
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult arg0) {
        
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        
        super.dispatchDraw(canvas);
    }
    
    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        Log.i(TAG, "drawChild:"+canvas.getSaveCount()+","+drawingTime);
        return super.drawChild(canvas, child, drawingTime);
    }
}
