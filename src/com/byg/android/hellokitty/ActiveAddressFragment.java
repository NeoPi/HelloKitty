package com.byg.android.hellokitty;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * @author byg
 */
public class ActiveAddressFragment extends Fragment {
    
    private View contentView = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.active_layout, null);
            
            initView();
        }
        return contentView;
    }

    private void initView() {
        
    }
}
