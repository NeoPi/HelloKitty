package com.byg.android.hellokitty;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

public class BaiduMapUtils {

	static double DEF_PI = 3.14159265359; // PI
	static double DEF_2PI = 6.28318530712; // 2*PI
	static double DEF_PI180 = 0.01745329252; // PI/180.0
	static double DEF_R = 6370693.5; // radius of earth

	/**
	 * 线路规划
	 */
	public static void calculateRoute() {

	}

	/**
	 * 计算两个坐标点之间的距离
	 * 
	 * @param startLatLng
	 *            起始坐标点
	 * @param endLatLng
	 *            终点
	 * @return
	 */
	public static double getShortDistance(LatLng startLatLng, LatLng endLatLng) {
		double distance = 0;
		if (startLatLng != null && endLatLng != null) {
			double ew1, ns1, ew2, ns2;
			double dx, dy, dew;
			// 角度转换为弧度
			ew1 = startLatLng.longitude * DEF_PI180;
			ns1 = startLatLng.latitude * DEF_PI180;
			ew2 = endLatLng.longitude * DEF_PI180;
			ns2 = endLatLng.latitude * DEF_PI180;
			// 经度差
			dew = ew1 - ew2;
			// 若跨东经和西经180 度，进行调整
			if (dew > DEF_PI)
				dew = DEF_2PI - dew;
			else if (dew < -DEF_PI)
				dew = DEF_2PI + dew;
			dx = DEF_R * Math.cos(ns1) * dew; // 东西方向长度(在纬度圈上的投影长度)
			dy = DEF_R * (ns1 - ns2); // 南北方向长度(在经度圈上的投影长度)
			// 勾股定理求斜边长
			distance = Math.sqrt(dx * dx + dy * dy);
		}
		return distance;
	}
	
	
    //适用于远距离
   public static double GetLongDistance(double lon1, double lat1, double lon2, double lat2){
       double ew1, ns1, ew2, ns2;
       double distance;
       // 角度转换为弧度
       ew1 = lon1 * DEF_PI180;
       ns1 = lat1 * DEF_PI180;
       ew2 = lon2 * DEF_PI180;
       ns2 = lat2 * DEF_PI180;
       // 求大圆劣弧与球心所夹的角(弧度)
       distance = Math.sin(ns1) * Math.sin(ns2) + Math.cos(ns1) * Math.cos(ns2) * Math.cos(ew1 - ew2);
       // 调整到[-1..1]范围内，避免溢出
       if (distance > 1.0)
            distance = 1.0;
       else if (distance < -1.0)
             distance = -1.0;
       // 求大圆劣弧长度
       distance = DEF_R * Math.acos(distance);
       return distance;
   }
   
   /**
    * 若传入的参数大于1000m 则返回XX公里，若小于1000m，则返回xx米
    * @param arg0
    * @return
    */
   public static String getDistance(LatLng startlatLng, LatLng endLatLng){
	   int arg0 = (int)DistanceUtil.getDistance(startlatLng, endLatLng);
	   String distance = null;
	   if (arg0 < 1000) {
		   distance = (int)arg0+"m";
	   } else {
		   int x = arg0 / 1000; // 千位以后
		   int y = arg0 % 1000 / 100; // 百位
		   int z = arg0 % 100 / 10; // 十位
		   if (z > 5) {
			   y+=1;
		   } 
		   distance = x+"."+y+"公里";
	   }
	   return distance;
   }
}
