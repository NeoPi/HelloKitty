package com.byg.android.hellokitty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * @author byg
 */
public class FirstActivity extends Activity implements OnClickListener {

    
    private Button searchBtn = null;
    private Button activeBtn = null;
    private Button startBtn = null;
    private Button navBtn = null;        // 导航按钮
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first);
		
		initView();
	}

    private void initView() {
        searchBtn = (Button) findViewById(R.id.button1);
        searchBtn.setOnClickListener(this);
        
        activeBtn = (Button) findViewById(R.id.button2);
        activeBtn.setOnClickListener(this);
        
        startBtn = (Button) findViewById(R.id.button3);
        startBtn.setOnClickListener(this);
        
        navBtn = (Button) findViewById(R.id.button4);
        navBtn.setOnClickListener(this);
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.button1:
            Intent intent = new Intent("com.byg");
            startActivity(intent);
            break;
        case R.id.button2:
            Intent intent2 = new Intent(getApplicationContext(), ActiveSelectActivity.class);
            startActivity(intent2);
            break;
        case R.id.button3:
            Intent intent3 = new Intent(getApplicationContext(), StartActiveActivity.class);
            startActivity(intent3);
            break;
        case R.id.button4:
            Intent intent4 = new Intent(getApplicationContext(), NavigationTestActivity.class);
            startActivity(intent4);
            break;
        default:
            break;
        }
    }
}
