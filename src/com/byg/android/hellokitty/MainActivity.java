package com.byg.android.hellokitty;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.baidu.lbsapi.auth.LBSAuthManagerListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
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
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.navisdk.BNaviPoint;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.baidu.navisdk.BaiduNaviManager.OnStartNavigationListener;
import com.baidu.navisdk.comapi.mapcontrol.BNMapController;
import com.baidu.navisdk.comapi.routeplan.BNRoutePlaner;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;
import com.baidu.navisdk.ui.routeguide.BNavigator;


/**
 * @ClassName   NavigationLeftFragment
 * @Description 侧滑栏导航页
 * @author      NeoPi
 * @date        2015年04月17日 上午 10:00:01
 * 
 * 									_oo0oo_
 * 								   o8888888o
 * 								   88" . "88
 * 								   (| -_- |)
 * 									o\ = /o
 * 								____/'---'\____
 * 							  .   ' \\| |// '   .
 *							   / \\||| : |||// \
 * 							 / _||||| -:- |||||- \
 *  						   | | \\\ - /// | |
 *  						 | \_| ' \---/'' | |
 *  					      \ .-\__ `-` ___/-. /
 *  					   ___`. .' /--.--\ `. . __
 *                    	."" '< `.___\_<|>_/___.' >'""
 *  				   | | : `- \` .;`\_/`;. `/ -` : | |
 *  					 \ \ `-. \_ __\ /__ _/ .-` / /
 *  			 ======`_.____`_.___\_____/___.-`____.-`======
 *  								'=---='
 *  
 * 			    ...............................................
 * 								 佛祖镇楼  , BUG辟易
 * 				佛曰:
 *  						写字楼里写字间  , 写字间里程序猿
 * 							程序人生写程序  , 又拿程序换酒钱
 * 							酒醒只在网上坐  , 酒醉还在网下眠
 * 							酒醉酒醒日复日  , 网上网下年复年
 * 							但愿老死电脑前  , 不愿鞠躬老板前
 * 							奔驰宝马贵者取  , 公交自行程序猿
 * 							别人笑我太疯癫  , 我笑自己命太贱
 * 							不见满街漂亮妹  , 哪个归得程序猿
 */


public class MainActivity extends Activity implements OnClickListener,
		OnGetPoiSearchResultListener, OnGetRoutePlanResultListener {

	private static String PARK_STATION = "停车场";
	private static String GAS_STATION = "加油站";
	private static String REPAIR_STATION = "汽车修理";
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private LocationClient mLocClient;
	private Button requestLocButton;
	private OnCheckedChangeListener radioButtonListener;
	private LocationMode mCurrentMode;
	boolean isFirstLoc = true;// 是否首次定位
	public MyLocationListenner myListener = new MyLocationListenner();
	public String select = "";
	public LatLng currentlatLng; //当前经纬度坐标点
	public LatLng endLatLng; // 目标地的经纬度坐标点
	private PoiSearch mPoiSearch = null;
	private boolean isNavInitSuccess = false; //导航初始化是否成功

	private BDLocation mlocation;  // 启动定位获得的当前定位
	private PoiDetailResult poiResult;  // 点击poi检索到的某一点得到的详细信息
	
	private TextView choiceTitle;
	private TextView choiceDistance;
	private TextView choiceStreet;
	private Button searchNearBy;
	private Button goThere;
	private RelativeLayout addressRelative;
	private LinearLayout poiChoiceInfo;
	private LinearLayout choiceLayout;
	
	private InfoWindow mInfoWindow;
	private RouteLine route;
	private RoutePlanSearch routeSearch;
	private TextView detailInfo;
	
	private OverlayManager routeOverlay;
	
	private ProgressBar mBar = null;
	int nodeIndex ;
	private ArrayList<String> stepList = new ArrayList<String>();
	private ArrayList<LatLng> latlngList = new ArrayList<LatLng>();
	
	private ListView detail_LV;
	private CheckBox mBox;
	private Myadapter adapter;
	private LinearLayout mlayout;
	private MyMapClickListener mMapClickListener = new MyMapClickListener();
	
	int wid;
	int hei;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		WindowManager wManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		wid = wManager.getDefaultDisplay().getWidth();
		hei = wManager.getDefaultDisplay().getHeight();
		
		findView();
		requestLocButton = (Button) findViewById(R.id.button1);
		requestLocButton.setText("普通");
		mCurrentMode = LocationMode.NORMAL;
		RadioGroup group = (RadioGroup) this.findViewById(R.id.radioGroup);
		radioButtonListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.defaulticon) {
					// 传入null则，恢复默认图标
					mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, null));
				}
				if (checkedId == R.id.customicon) {
					mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, null));
				}
			}
		};
		group.setOnCheckedChangeListener(radioButtonListener);
		requestLocButton.setOnClickListener(this);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.removeViewAt(2); // 清除掉百度地图上的(+ -)按钮所在的图层
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMyLocationEnabled(true);
		mBaiduMap.setOnMapClickListener(mMapClickListener);
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(5000);
		mLocClient.setLocOption(option);
		mLocClient.start();

		// 初始化搜索模块，注册搜索事件监听
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);

		// 初始化搜索模块，注册事件监听
        routeSearch = RoutePlanSearch.newInstance();
        routeSearch.setOnGetRoutePlanResultListener(this);
        
		findViewById(R.id.button2).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBar != null && mBar.getVisibility() != View.VISIBLE) {
					mBar.setVisibility(View.VISIBLE);
				}
				Log.i("123", "停车场");
				if ( poiChoiceInfo != null && poiChoiceInfo.getVisibility() == View.VISIBLE) {
					poiChoiceInfo.setVisibility(View.GONE);
				}
				select = PARK_STATION;
				mPoiSearch.searchNearby((new PoiNearbySearchOption())
						.keyword("").radius(3000).pageCapacity(10)
						.location(currentlatLng));
			}
		});
		
		findViewById(R.id.gas_station).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBar != null && mBar.getVisibility() != View.VISIBLE) {
					mBar.setVisibility(View.VISIBLE);
				}
				Log.i("123", "加油站");
				if ( poiChoiceInfo != null && poiChoiceInfo.getVisibility() == View.VISIBLE) {
					poiChoiceInfo.setVisibility(View.GONE);
				}
				select = GAS_STATION;
				mPoiSearch.searchNearby((new PoiNearbySearchOption())
						.keyword(GAS_STATION).radius(3000).pageCapacity(10)
						.location(currentlatLng));
			}
		});
		findViewById(R.id.repair_station).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBar != null && mBar.getVisibility() != View.VISIBLE) {
					mBar.setVisibility(View.VISIBLE);
				}
				Log.i("123", "汽车修理");
				if ( poiChoiceInfo != null && poiChoiceInfo.getVisibility() == View.VISIBLE) {
					poiChoiceInfo.setVisibility(View.GONE);
				}
				select = REPAIR_STATION;
				mPoiSearch.searchNearby((new PoiNearbySearchOption())
						.keyword(REPAIR_STATION).radius(3000).pageCapacity(10)
						.location(currentlatLng));
			}
		});
		
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
		
		findViewById(R.id.refresh).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPoiSearch.searchNearby((new PoiNearbySearchOption())
						.keyword(select).radius(3000).pageCapacity(10)
						.location(mBaiduMap.getProjection().fromScreenLocation(new Point(200,300))));
			}
		});
	}

	private void findView() {
		choiceLayout = (LinearLayout)findViewById(R.id.choice);
		choiceTitle = (TextView)findViewById(R.id.choice_info_title);
		choiceDistance = (TextView)findViewById(R.id.choice_info_distance);
		choiceStreet = (TextView)findViewById(R.id.choice_info_address);
		searchNearBy = (Button)findViewById(R.id.search_nearby);
		goThere = (Button)findViewById(R.id.go_there_button);
		addressRelative = (RelativeLayout)findViewById(R.id.choice_info);
		poiChoiceInfo = (LinearLayout)findViewById(R.id.poi_choice_info);
		detailInfo = (TextView)findViewById(R.id.choice_info_detail);
		mBar = (ProgressBar) findViewById(R.id.progressBar1);
		detail_LV = (ListView)findViewById(R.id.detail_pop_step);
		mBox = (CheckBox)findViewById(R.id.detail_pop_select);
		mlayout = (LinearLayout)findViewById(R.id.poi_choice_step);
		detailInfo.setOnClickListener(this);
		goThere.setOnClickListener(this);
		searchNearBy.setOnClickListener(this);
		addressRelative.setOnClickListener(this);
		mBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mBox.setText("收起");
					detail_LV.setVisibility(View.VISIBLE);
					LayoutParams lP = (LayoutParams) detail_LV.getLayoutParams();
					lP.width = wid;
					lP.height = hei / 2;
					Log.i("123", wid + "," + hei);
					detail_LV.setLayoutParams(lP);
				} else {
					if (detail_LV != null && detail_LV.getVisibility() == View.VISIBLE) {
						mBox.setText("详情");
						detail_LV.setVisibility(View.GONE);
					}
				}
			}
		});
		
	}

	private NaviEngineInitListener mNaviEngineInitListener = new NaviEngineInitListener() {
		
		@Override
		public void engineInitSuccess() {
			Log.i("123", "导航初始化成功。。。");
			isNavInitSuccess = true;
		}
		
		@Override
		public void engineInitStart() {
			Log.i("123", "导航初始化启动。。。");
		}
		
		@Override
		public void engineInitFail() {
			Log.i("123", "导航初始化失败。。。");
		}
	};
	/**
	 * 获取外部存储设备路径
	 * @return
	 */
	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(
				Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return "";
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
        BNavigator.getInstance().resume();  
        BNMapController.getInstance().onResume();  
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
		BNavigator.getInstance().pause();  
        BNMapController.getInstance().onPause(); 
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		
		BNavigator.destory();  
	    BNRoutePlaner.getInstance().setObserver(null);  
	}

	@Override  
    public void onConfigurationChanged(Configuration newConfig) {  
        BNavigator.getInstance().onConfigurationChanged(newConfig);  
        super.onConfigurationChanged(newConfig);  
    }
	
	public void onBackPressed(){  
        BNavigator.getInstance().onBackPressed();
        finish();
    }
	/**
	 * 实现百度定位监听事件
	 * @author byg
	 *
	 */
	private class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null) {
				Toast.makeText(MainActivity.this, "定位失败，请检查网络!",Toast.LENGTH_SHORT).show();
				if (mBar != null && mBar.getVisibility() == View.VISIBLE) {
					mBar.setVisibility(View.GONE);
				}
				return ;
			}
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			// 设置定位数据
			currentlatLng = new LatLng(location.getLatitude(), location.getLongitude());
			mlocation = location;
			Log.i("123", location.getLatitude()+"...."+location.getLongitude());
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
			}
		}

//		@Override
//		public void onReceivePoi(BDLocation poiLocation) {
//			
//		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
			switch (mCurrentMode) {
			case NORMAL:
				requestLocButton.setText("跟随");
				mCurrentMode = LocationMode.FOLLOWING;
				mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
						mCurrentMode, true, null));
				break;
			case COMPASS:
				requestLocButton.setText("普通");
				mCurrentMode = LocationMode.NORMAL;
				mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
						mCurrentMode, true, null));
				break;
			case FOLLOWING:
				requestLocButton.setText("罗盘");
				mCurrentMode = LocationMode.COMPASS;
				mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
						mCurrentMode, true, null));
				break;
			}
			break;
		case R.id.choice_info:
			
			break;
		case R.id.search_nearby:
			
			break;
		case R.id.go_there_button:
			startCalcRoute();
//			driverRoutePlan();
			break;
		case R.id.choice_info_detail:
			Intent intent = new  Intent(MainActivity.this,DriverRouteDetailActivity.class);
			Bundle bundle = new Bundle();
//			bundle.putStringArrayList("step",stepList);
			bundle.putSerializable("latlng", latlngList);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	
	/**
	 * 线路规划
	 */
	private void driverRoutePlan() {
//		Toast.makeText(getApplicationContext(), "我点击了到那儿去按钮", Toast.LENGTH_SHORT).show();
		if (mBar != null && mBar.getVisibility() != View.VISIBLE) {
			mBar.setVisibility(View.VISIBLE);
		}
		route = null;
		mBaiduMap.clear();
		//设置终点起点信息
		PlanNode startNode = PlanNode.withLocation(currentlatLng);
		PlanNode endNode = PlanNode.withLocation(endLatLng);
		PlanNode passNode = PlanNode.withLocation(currentlatLng);
		List<PlanNode> passByNodes = new ArrayList<PlanNode>();
		passByNodes.add(passNode);
		routeSearch.drivingSearch(new DrivingRoutePlanOption()
					.from(startNode)
					.passBy(passByNodes)
					.to(endNode));
	}

	@Override
	public void onGetPoiDetailResult(PoiDetailResult result) {
		if (mBar != null && mBar.getVisibility() == View.VISIBLE) {
			mBar.setVisibility(View.GONE);
		}
		if (result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(this, "抱歉，未找到结果", Toast.LENGTH_SHORT)
					.show();
		} else {
			poiResult = result;
			endLatLng = result.getLocation();
			Log.i("123", endLatLng.toString());
			Button button = new Button(getApplicationContext());
			button.setText(result.getName());
			button.setTextColor(Color.BLACK);
			button.setBackgroundResource(R.drawable.popup);
			mInfoWindow = new InfoWindow(button, endLatLng, -47);
			mBaiduMap.showInfoWindow(mInfoWindow);
			Toast.makeText(this, 
					result.getName() + ": " + result.getAddress(),
//					BaiduMapUtils.GetLongDistance(currentlatLng.longitude,currentlatLng.latitude,
//							endLatLng.longitude,endLatLng.latitude)+"",
//					DistanceUtil.getDistance(currentlatLng, endLatLng)+"",
					Toast.LENGTH_SHORT
			).show();
			mHandler.sendEmptyMessage(0x01);
			
		}
	}

	private void setViewText() {
		poiChoiceInfo.setVisibility(View.VISIBLE);
		choiceTitle.setText(poiResult.getName());
		choiceDistance.setHint(BaiduMapUtils.getDistance(currentlatLng, endLatLng));
		choiceStreet.setText(poiResult.getAddress());
	}

	/**
	 * 开始规划路线 前提条件 导航引擎初始化成功
	 */
	private void startCalcRoute() {
		if (!isNavInitSuccess) {
			Toast.makeText(getApplicationContext(), "导航初始化未成功，请扫后再试！", Toast.LENGTH_SHORT).show();
		} else {
			BNaviPoint startPoint = new BNaviPoint(mlocation.getLongitude(), mlocation.getLatitude(),
					mlocation.getAddrStr(), BNaviPoint.CoordinateType.BD09_MC);
			BNaviPoint endPoint = new BNaviPoint(poiResult.getLocation().longitude, poiResult.getLocation().latitude,
					poiResult.getAddress(), BNaviPoint.CoordinateType.BD09_MC);
			BaiduNaviManager.getInstance().launchNavigator(this, startPoint, endPoint,
					NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME,// 算路方式
					true, // 真实导航
					BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY, new OnStartNavigationListener() { // 跳转监听
						@Override
						public void onJumpToDownloader() {
						}
						@Override
						public void onJumpToNavigator(Bundle configParams) {
							Intent intent = new Intent(MainActivity.this, BNavigatorActivity.class);
							intent.putExtras(configParams);
							startActivity(intent);
						}
					});
		}
	}

	@Override
	public void onGetPoiResult(PoiResult result) {
		if (result == null
				|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
			Toast.makeText(MainActivity.this, "没有搜索到结果，请检查网络", Toast.LENGTH_SHORT).show();
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			if (mBar != null && mBar.getVisibility() == View.VISIBLE) {
				mBar.setVisibility(View.GONE);
			}
			mBaiduMap.clear();
			MyOm overlay = new MyOm(mBaiduMap);
			overlay.setResult(result);
			mBaiduMap.setOnMarkerClickListener(overlay);
//			overlay.setData(result);
			overlay.addToMap();
			overlay.zoomToSpan();
			return;
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

	private class MyOm extends OverlayManager {
        private PoiResult presult;
        private BaiduMap bdmap;
        List<PoiInfo> pois ;
        List<OverlayOptions> ops;
        Marker mMarker;
        public void setResult(PoiResult result) {
                presult = result;
        }

        public MyOm(BaiduMap bdmap) {
                super(bdmap);
                this.bdmap = bdmap;
        }

        @Override
		public boolean onMarkerClick(Marker marker) {
        	if (mBar != null && mBar.getVisibility() != View.VISIBLE) {
				mBar.setVisibility(View.VISIBLE);
			}
			if (select.equals(GAS_STATION)) {
				marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.gas_stations_press));
				if (mMarker != null && mMarker != marker) {
					mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.gas_stations));
				}
			} else if (select.equals(PARK_STATION)) {
				marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.park_station_press));
				if (mMarker != null && mMarker != marker) {
					mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.park_station));
				}
			} else if (select.equals(REPAIR_STATION)) {
				marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.repair_station_press));
				if (mMarker != null && mMarker != marker) {
					mMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.repair_station));
				}
			}
			mMarker = marker;
			onClick(marker.getZIndex());
			return true;
        }

		public boolean onClick(int index) {
			if (detailInfo != null && detailInfo.getVisibility() == View.VISIBLE) {
				detailInfo.setVisibility(View.GONE);
			}
			PoiInfo poi = presult.getAllPoi().get(index);
			mPoiSearch.searchPoiDetail((new PoiDetailSearchOption()).poiUid(poi.uid));
			return true;
		}

        @Override
        public List<OverlayOptions> getOverlayOptions() {
                ops = new ArrayList<OverlayOptions>();
                pois = presult.getAllPoi();
                BitmapDescriptor bitmap = null;
                if (select.equals(GAS_STATION)) {
                	bitmap = BitmapDescriptorFactory
                            .fromResource(R.drawable.gas_stations);
				}else if (select.equals(PARK_STATION)) {
                	bitmap = BitmapDescriptorFactory
                            .fromResource(R.drawable.park_station);
				}else if (select.equals(REPAIR_STATION)) {
                	bitmap = BitmapDescriptorFactory
                            .fromResource(R.drawable.repair_station);
				}
                for (int i = 0; i < pois.size(); i++) {
                        OverlayOptions op = new MarkerOptions()
                                .position(pois.get(i).location)
                                .icon(bitmap);
                        ops.add(op);
                        bdmap.addOverlay(op).setZIndex(i);
                }
                return ops;
        }
	}
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0x01:
				setViewText();
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            if (mBar != null && mBar.getVisibility() == View.VISIBLE) {
    			mBar.setVisibility(View.GONE);
    		}
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            //result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
        	if (mBar != null && mBar.getVisibility() == View.VISIBLE) {
    			mBar.setVisibility(View.GONE);
    		}
            nodeIndex = -1;
            route = result.getRouteLines().get(0);
            if (stepList != null && stepList.size() > 0) {
				stepList.clear();
			}
            for (int i = 0; i < route.getAllStep().size(); i++) {
            	Object step = route.getAllStep().get(i);
//				Log.i("123", ((DrivingRouteLine.DrivingStep) step).getEntrace().getTitle()+"");
//				Log.d("123", ((DrivingRouteLine.DrivingStep) step).getExitInstructions());
//				Log.i("123", ((DrivingRouteLine.DrivingStep) step).getWayPoints()+"");
//				Log.w("123", ((DrivingRouteLine.DrivingStep) step).getInstructions());
//				Log.w("123", ((DrivingRouteLine.DrivingStep) step).getDistance()+","+((DrivingRouteLine.DrivingStep) step).getDuration());
				stepList.add(((DrivingRouteLine.DrivingStep) step).getExitInstructions());
            }
            List<DrivingRouteLine.DrivingStep> steps = route.getAllStep();
            adapter = new Myadapter(MainActivity.this,steps);
            detail_LV.setAdapter(adapter);
            MyDrivingStepOverlay overlay = new MyDrivingStepOverlay(mBaiduMap);
            routeOverlay = overlay;
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
            if (detailInfo.getVisibility() != View.VISIBLE) {
    			detailInfo.setVisibility(View.VISIBLE);
    			mlayout.setVisibility(View.VISIBLE);
    			Animation animation = new TranslateAnimation(0, 0, poiChoiceInfo.getHeight(), getWindow().getAttributes().height / 2);
    			animation.setDuration(2000);
    			poiChoiceInfo.startAnimation(animation);
    		}
        }
	}

	private class MyDrivingStepOverlay extends DrivingRouteOverlay{

		private BaiduMap mMap;
		private DrivingRouteLine data;
		
		public MyDrivingStepOverlay(BaiduMap mMap) {
			super(mMap);
			this.mMap = mMap;
		}

		@Override
		public BitmapDescriptor getStartMarker() {
			return super.getStartMarker();
		}

		@Override
		public BitmapDescriptor getTerminalMarker() {
			return super.getTerminalMarker();
		}

		@Override
		public boolean onRouteNodeClick(int arg0) {
			Button mPop;
			mPop = new Button(getApplicationContext());
			mPop.setTextColor(Color.BLACK);
			mPop.setBackgroundResource(R.drawable.popup);
			mPop.setText(((DrivingRouteLine.DrivingStep) (data.getAllStep().get(arg0-1))).getExitInstructions());
			mInfoWindow = new InfoWindow(mPop,  ((DrivingRouteLine.DrivingStep) (data.getAllStep().get(arg0))).getEntrace().getLocation(), 0);
			mMap.showInfoWindow(mInfoWindow);
//			ToastUtils.show(getApplicationContext(), arg0 + ((DrivingRouteLine.DrivingStep) (data.getAllStep().get(arg0))).getInstructions()+"<><><><><><>");
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
	
	
	public class MyMapClickListener implements OnMapClickListener{

        @Override
        public void onMapClick(LatLng arg0) {
            Toast.makeText(getApplicationContext(), "页面跳转", Toast.LENGTH_SHORT).show();
        }
        @Override
        public boolean onMapPoiClick(MapPoi arg0) {
            Toast.makeText(MainActivity.this, arg0.getName().replace("\\", ""), Toast.LENGTH_SHORT).show();
            return false;
        }
	}
	public class Myadapter extends BaseAdapter{
		
		private Context context;
		private List<DrivingRouteLine.DrivingStep> list;
		
		public Myadapter(Context context,List<DrivingRouteLine.DrivingStep> list) {
			super();
			this.context = context;
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder ;
			if (convertView == null) {
				holder = new Holder();
				convertView = LayoutInflater.from(context).inflate(R.layout.step_item, null);
				holder.image = (ImageView) convertView.findViewById(R.id.step_item_image);
				holder.text = (TextView) convertView.findViewById(R.id.step_item_text);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			DrivingRouteLine.DrivingStep step = list.get(position);
			if (step != null) {
				String data = step.getExitInstructions();
				holder.text.setText(data);
				initImg(holder.image,step.getNumTurns());
			}
			return convertView;
		}

		private void initImg(ImageView image, int numTurns) {
			switch (numTurns) {
			case 0:
				image.setImageResource(R.drawable.walk_turn_0);
				break;
			case 1:
				image.setImageResource(R.drawable.walk_turn_1);
				break;
			case 2:
				image.setImageResource(R.drawable.walk_turn_2);
				break;
			case 3:
				image.setImageResource(R.drawable.walk_turn_3);
				break;
			case 4:
				image.setImageResource(R.drawable.walk_turn_4);
				break;
			case 5:
				image.setImageResource(R.drawable.walk_turn_5);
				break;
			case 6:
				image.setImageResource(R.drawable.walk_turn_6);
				break;
			case 7:
				image.setImageResource(R.drawable.walk_turn_7);
				break;
			case 8:
				image.setImageResource(R.drawable.walk_turn_8);
				break;
			case 9:
				image.setImageResource(R.drawable.walk_turn_9);
				break;
			case 10:
				image.setImageResource(R.drawable.walk_turn_uturn);
				break;
			default:
				break;
			}
		}
		
//		private void updateSingleRow(ListView listView, long id) {
//
//			if (listView != null) {
//				int start = listView.getFirstVisiblePosition();
//				for (int i = start, j = listView.getLastVisiblePosition(); i <= j; i++)
//					if (id == ((DrivingRouteLine.DrivingStep) listView.getItemAtPosition(i)).getId()) {
//						View view = listView.getChildAt(i - start);
//						getView(i, view, listView);
//						break;
//					}
//			}
//		}
	}
	
	class Holder{
		ImageView image;
		TextView text;
	}
}
