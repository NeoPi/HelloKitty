package com.byg.android.hellokitty;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DriverRouteDetailActivity extends Activity implements OnClickListener {

	private ArrayList<String> mlist;
	private ImageView back_IM;
	private Myadapter adapter;
	private ListView listView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_driver_route_detail_layout);
		mlist = getIntent().getExtras().getStringArrayList("step");
		if (mlist == null) {
			Toast.makeText(this, "mlist 为空", Toast.LENGTH_SHORT).show();
		} else {
//			for (int i = 0; i < mlist.size(); i++) {
//				Log.i("123",mlist.get(i));
//			}
		}
		findView();
	}
	/**
	 * 查找组件
	 */
	private void findView() {
		back_IM = (ImageView)findViewById(R.id.public_top_back);
		back_IM.setOnClickListener(this);
		adapter = new Myadapter(DriverRouteDetailActivity.this, mlist);
		listView = (ListView)findViewById(R.id.step_listView);
		listView.setAdapter(adapter);
	}
	
	public class Myadapter extends BaseAdapter{
		
		private Context context;
		private ArrayList<String> list;
		
		
		public Myadapter(Context context, ArrayList<String> list) {
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
			String data = list.get(position);
			if (data != null) {
				holder.text.setText(data);
			}
			return convertView;
		}
	}
	
	class Holder{
		ImageView image;
		TextView text;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.public_top_back:
			finish();
			break;
		default:
			break;
		}
	}
}
