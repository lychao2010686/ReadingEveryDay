package com.example.drawerlayout;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class MainActivity extends FragmentActivity {

	//"情感治愈" ,"经典散文" ,"联系我们", 
	public static final String[] TITLES = {"情感治愈" ,"经典散文","每日一文", "心灵鸡汤", "设置"};
	private DrawerLayout mDrawerLayout;
	private RelativeLayout mLeftLayout;
	private RelativeLayout mRightLayout;
	private ListView mLeftListView;
	private ListView mRightListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById();
		mLeftListView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_expandable_list_item_1, TITLES));
//		mRightListView.setAdapter(new ArrayAdapter<String>(this,
//				android.R.layout.simple_expandable_list_item_1, TITLES));

		// 监听菜单
		mLeftListView.setOnItemClickListener(new DrawerItemClickListenerLeft());
//		mRightListView.setOnItemClickListener(new DrawerItemClickListenerRight());
	}

	private void findViewById() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mLeftLayout = (RelativeLayout) findViewById(R.id.menu_layout_left);
//		mRightLayout = (RelativeLayout) findViewById(R.id.menu_layout_right);
		mLeftListView = (ListView) findViewById(R.id.menu_listView_l);
//		mRightListView = (ListView) findViewById(R.id.menu_listView_r);
	}

	/**
	 * the clicklistener in left side
	 * 
	 * @author liyangchao
	 * 
	 */
	public class DrawerItemClickListenerLeft implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Fragment fragment = null;
			
			//according to the click row number decide to start which fragment;
			switch (position) {
			case 0:
				fragment = new FirstFragment();
				break;
			case 1:
				fragment = new SecondFragment();
				break;
			case 2:
			case 3:
			case 4:
				fragment = new SettingActivity();
				break;
			default:
				break;
			}
			ft.replace(R.id.fragment_layout, fragment);
			ft.commit();
			mDrawerLayout.closeDrawer(mLeftLayout);
		}

	}
	
	/*public class DrawerItemClickListenerRight implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Fragment fragment = null;
			
			//according to the click row number decide to start which fragment;
			switch (position) {
			case 0:
				fragment = new FirstFragment();
				break;
			case 1:
				fragment = new SecondFragment();
				break;
			default:
				break;
			}
			ft.replace(R.id.fragment_layout, fragment);
			ft.commit();
			mDrawerLayout.closeDrawer(mRightLayout);
		}

		}*/
		

}
