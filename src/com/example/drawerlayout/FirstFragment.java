package com.example.drawerlayout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class FirstFragment extends Fragment {
	
	private TextView tv;
	private View mParentView;
	private FragmentActivity mFragmentActivity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.first, null);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mParentView = getView();
		mFragmentActivity = getActivity();
		
		tv = (TextView) mParentView.findViewById(R.id.textView1);
		
		tv.setText("哈哈哈，這是個測試而已！！");
	}
	
	
}
