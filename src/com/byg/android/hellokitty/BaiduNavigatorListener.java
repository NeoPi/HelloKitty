package com.byg.android.hellokitty;

import com.baidu.navisdk.model.datastruct.LocData;
import com.baidu.navisdk.model.datastruct.SensorData;
import com.baidu.navisdk.ui.routeguide.IBNavigatorListener;

public class BaiduNavigatorListener implements IBNavigatorListener{

	@Override
	public void notifyGPSStatusData(int arg0) {
		
	}
	@Override
	public void notifyLoacteData(LocData arg0) {
		
	}
	@Override
	public void notifyNmeaData(String arg0) {
		
	}
	@Override
	public void notifySensorData(SensorData arg0) {
		
	}
	@Override
	public void notifyStartNav() {
		
	}
	@Override
	public void notifyViewModeChanged(int arg0) {
		
	}
	@Override
	public void onPageJump(int jumpTiming, Object arg) {
		// TODO 页面跳转回调  
        if(IBNavigatorListener.PAGE_JUMP_WHEN_GUIDE_END == jumpTiming){  
//            finish();  
        }else if(IBNavigatorListener.PAGE_JUMP_WHEN_ROUTE_PLAN_FAIL == jumpTiming){  
//            finish();  
        } 
	}
	@Override
	public void onYawingRequestStart() {
		
	}
	@Override
	public void onYawingRequestSuccess() {
		
	}

}
