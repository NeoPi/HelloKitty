package com.byg.android.hellokitty;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @ClassName   NavigationLeftFragment
 * @Description 侧滑栏导航页
 * @author      NeoPi
 * @date        2015年04月17日 上午 10:00:01
 * 
 *                                  _oo0oo_
 *                                 o8888888o
 *                                 88" . "88
 *                                 (| -_- |)
 *                                  o\ = /o
 *                              ____/'---'\____
 *                            .   ' \\| |// '   .
 *                             / \\||| : |||// \
 *                           / _||||| -:- |||||- \
 *                             | | \\\ - /// | |
 *                           | \_| ' \---/'' | |
 *                            \ .-\__ `-` ___/-. /
 *                         ___`. .' /--.--\ `. . __
 *                      ."" '< '.___\_<|>_/___.' >' "".
 *                     | | : `- \` .;`\_/`;. `/ -` : | |
 *                       \ \ `-. \_ __\ /__ _/ .-` / /
 *               ======`_.____`-.___\_____/___.-`____.-`======
 *                                  '=---='
 *  
 *              ...............................................
 *                              佛祖镇楼  , BUG辟易
 *              佛曰:
 *                          写字楼里写字间  , 写字间里程序猿
 *                          程序人生写程序  , 又拿程序换酒钱
 *                          酒醒只在网上坐  , 酒醉还在网下眠
 *                          酒醉酒醒日复日  , 网上网下年复年
 *                          但愿老死电脑前  , 不愿鞠躬老板前
 *                          奔驰宝马贵者取  , 公交自行程序猿
 *                          别人笑我太疯癫  , 我笑自己命太贱
 *                          不见满街漂亮妹  , 哪个归得程序猿
 */


public class FozuCode extends Fragment{
    
    private View rootView = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (rootView == null) {
//            rootView = inflater.inflate(R.layout.navigation_left_layout, null);
            
        } else {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null) {
                parent.removeAllViewsInLayout();
            }
        }
        return rootView;
    }

}

