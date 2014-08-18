package com.example.drawerlayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SecondFragment extends Fragment {

	private int currIndex = 0;
	private int zero = 0;
	// 用于保证startY的值在一个完整的touch事件中只被记录一次
	private boolean isRecored;
	private List<Map<String, Object>> data;
	private int firstItemIndex;
	private int state;
	private boolean isBack;
	private float startY;
	// listview 实例对象
	private ListView mListView;
	private View mParentView;
	private FragmentActivity mFragmentActivity;
	private View headView;
	private final static int RELEASE_To_REFRESH = 0;
	private final static int PULL_To_REFRESH = 1;
	private final static int REFRESHING = 2;
	private final static int DONE = 3;
	private final static int LOADING = 4;
	private int headViewWidth, headViewHeight;
	// 实际的padding的距离与界面上偏移距离的比例
	private final static int RATIO = 3;
	// 查看更多
	private TextView moreTextView;
	// 正在加载进度条
	private LinearLayout loadProgressBar;
	private ProgressBar mProgressBar;
	// 分页加载的数据的数量
	private int pageSize = 10;
	private SimpleAdapter adapter;
	private TextView tipsTextview, lastUpdatedTextView;
	private ImageView head_arrowImageView;
	/** 旋转动画 */
	private RotateAnimation animation, reverseAnimation;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.second, null);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mParentView = getView();
		mFragmentActivity = getActivity();

		animation = new RotateAnimation(0, 180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setFillAfter(true);
		animation.setDuration(200);
		animation.setInterpolator(new LinearInterpolator());
		
		reverseAnimation = new RotateAnimation(180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setFillAfter(true);
		reverseAnimation.setDuration(200);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		LayoutInflater layoutInflater = LayoutInflater.from(mFragmentActivity);
		headView = layoutInflater.inflate(R.layout.head, null);
		mListView = (ListView) mParentView.findViewById(R.id.listview);
		mProgressBar = (ProgressBar) headView
				.findViewById(R.id.head_progressBar);
		tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView);
		lastUpdatedTextView = (TextView) headView
				.findViewById(R.id.head_lastUpdatedTextView);
		head_arrowImageView = (ImageView) headView
				.findViewById(R.id.head_arrowImageView);
		
		data = initValue(1,15);
		adapter = new SimpleAdapter(mFragmentActivity, data, android.R.layout.simple_expandable_list_item_2, new String[] {"title","text"}, new int[] {android.R.id.text1,android.R.id.text2});

		head_arrowImageView.setMinimumHeight(50);
		head_arrowImageView.setMinimumWidth(70);

		measureView(headView);
		addPageMore();

		headViewWidth = headView.getMeasuredWidth();
		headViewHeight = headView.getMeasuredHeight();

		headView.setPadding(0, -1 * headViewHeight, 0, 0);

		headView.invalidate();
		mListView.addHeaderView(headView, null, false);
		mListView.setAdapter(adapter);

		mListView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (firstItemIndex == 0 && !isRecored) {
						isRecored = true;
						startY = (int) event.getY();
						Toast.makeText(getActivity(), "你點擊了item", 1).show();
						Log.i("lyc", "在down时候记录当前位置‘" + startY);
					}
					break;

				case MotionEvent.ACTION_UP:

					if (state != REFRESHING && state != LOADING) {
						if (state == DONE) {
							// 什么都不做
						}
						if (state == PULL_To_REFRESH) {
							state = DONE;
							changeHeaderViewByState();
							Log.i("lyc", "由下拉刷新状态，到done状态");
						}
						if (state == RELEASE_To_REFRESH) {
							state = REFRESHING;
							changeHeaderViewByState();
							Log.i("lyc", "由松开刷新状态，到done状态");
						}
					}

					isRecored = false;
					isBack = false;

					break;

				case MotionEvent.ACTION_MOVE:
					int tempY = (int) event.getY();

					if (!isRecored && firstItemIndex == 0) {
						Log.i("lyc", "在move时候记录下位置" + tempY);
						isRecored = true;
						startY = tempY;
					}

					if (state != REFRESHING && isRecored && state != LOADING) {

						// 保证在设置padding的过程中，当前的位置一直是在head，否则如果当列表超出屏幕的话，当在上推的时候，列表会同时进行滚动

						// 可以松手去刷新了
						if (state == RELEASE_To_REFRESH) {

							mListView.setSelection(0);

							// 往上推了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
							if (((tempY - startY) / RATIO < headViewHeight)
									&& (tempY - startY) > 0) {
								state = PULL_To_REFRESH;
								changeHeaderViewByState();

								Log.i("lyc", "由松开刷新状态转变到下拉刷新状态");
							}
							// 一下子推到顶了
							else if (tempY - startY <= 0) {
								state = DONE;
								changeHeaderViewByState();

								Log.i("lyc", "由松开刷新状态转变到done状态");
							}
							// 往下拉了，或者还没有上推到屏幕顶部掩盖head的地步
							else {
								// 不用进行特别的操作，只用更新paddingTop的值就行了
							}
						}
						// 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
						if (state == PULL_To_REFRESH) {

							// 下拉到可以进入RELEASE_TO_REFRESH的状态
							if ((tempY - startY) / RATIO >= headViewHeight) {
								state = RELEASE_To_REFRESH;
								isBack = true;
								changeHeaderViewByState();

								Log.i("lyc", "由done或者下拉刷新状态转变到松开刷新");
							}
							// 上推到顶了
							else if (tempY - startY <= 0) {
								state = DONE;
								changeHeaderViewByState();

								Log.i("lyc", "由DOne或者下拉刷新状态转变到done状态");
							}
						}

						// done状态下
						if (state == DONE) {
							if (tempY - startY > 0) {
								state = PULL_To_REFRESH;
								changeHeaderViewByState();
							}
						}

						// 更新headView的size
						if (state == PULL_To_REFRESH) {
							headView.setPadding(
									0,
									(int) (-1 * headViewHeight + (tempY - startY)
											/ RATIO), 0, 0);
							System.out.println("top_distance0:"
									+ (-1 * headViewHeight + (tempY - startY)
											/ RATIO));

						}

						// 更新headView的paddingTop
						if (state == RELEASE_To_REFRESH) {
							headView.setPadding(0, (int) ((tempY - startY)
									/ RATIO - headViewHeight), 0, 0);
						}

					}

					break;
				}

				return false;
			}

		});
	}

	// 当状态改变时候，调用该方法，以更新界面
	private void changeHeaderViewByState() {
		// TODO Auto-generated method stub
		switch (state) {
		case RELEASE_To_REFRESH:
			head_arrowImageView.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			head_arrowImageView.clearAnimation();
			head_arrowImageView.startAnimation(animation);

			tipsTextview.setText("松开刷新");

			Log.v("lyc", "当前状态，松开刷新");
			break;
		case PULL_To_REFRESH:
			mProgressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			head_arrowImageView.clearAnimation();
			head_arrowImageView.setVisibility(View.VISIBLE);
			// 是由RELEASE_To_REFRESH状态转变来的
			if (isBack) {
				isBack = false;
				head_arrowImageView.clearAnimation();
				head_arrowImageView.startAnimation(reverseAnimation);

				tipsTextview.setText("下拉刷新");
			} else {
				tipsTextview.setText("下拉刷新");
			}
			Log.i("lyc", "当前状态，下拉刷新");
			break;

		case REFRESHING:

			headView.setPadding(0, 0, 0, 0);

			mProgressBar.setVisibility(View.VISIBLE);
			head_arrowImageView.clearAnimation();
			head_arrowImageView.setVisibility(View.GONE);
			tipsTextview.setText("正在刷新...");
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.GONE);
			Log.i("lyc", "当前状态,正在刷新...");
			new refreshTask().execute();
			break;
		case DONE:
			headView.setPadding(0, -1 * headViewHeight, 0, 0);

			mProgressBar.setVisibility(View.GONE);
			head_arrowImageView.clearAnimation();
			head_arrowImageView
					.setImageResource(R.drawable.ic_pulltorefresh_arrow);
			tipsTextview.setText("下拉刷新");
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			Log.i("lyc", "当前状态，done");
			break;
		}
	}

	// 此方法直接照搬自网络上的一个下拉刷新的demo，此处是“估计”headView的width以及height
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	/**
	 * 加载下一页的数据
	 * 
	 * @param pageStart
	 * @param pageSize
	 */
	private void chageListView(int pageStart, int pageSize) {
		List<Map<String, Object>> data = initValue(pageStart, pageSize);
		for (Map<String, Object> map : data) {
			this.data.add(map);
		}
		data = null;
	}

	/**
	 * 在ListView中添加"加载更多"
	 */
	private void addPageMore() {
		View view = LayoutInflater.from(mFragmentActivity).inflate(
				R.layout.bottom, null);
		moreTextView = (TextView) view.findViewById(R.id.more_id);
		loadProgressBar = (LinearLayout) view.findViewById(R.id.load_id);
		moreTextView.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 隐藏"加载更多"
				moreTextView.setVisibility(View.GONE);
				// 显示进度条
				loadProgressBar.setVisibility(View.VISIBLE);
				new refreshTask().execute();

			}
		});
		mListView.addFooterView(view);
	}

	/**
	 * 模拟数据分页加载，
	 * 
	 * @param pageStart
	 *            起始数
	 * @param pageSize
	 *            每页显示数目
	 *            這裡可以傳入文章title和content摘要
	 * @return
	 */
	public static List<Map<String, Object>> initValue(int pageStart,
			int pageSize) {
		Map<String, Object> map;
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < pageSize; i++) {
			map = new HashMap<String, Object>();
			map.put("text", "定义会话列表");
			map.put("title", "第" + pageStart + "条会话");
			++pageStart;
			list.add(map);
		}
		return list;
	}

	class refreshTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub

			// 需要在加载过程中做的事情

			try {
				Thread.sleep(3000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// 加载模拟数据：下一页数据， 在正常情况下，上面的休眠是不需要，直接使用下面这句代码加载相关数据
			chageListView(data.size() + 1, pageSize);
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			// 进度更新
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			// 加载完成之后，应该做什么样的操作

			// 通知适配器，发现改变操作
			adapter.notifyDataSetChanged();
			onRefreshComplete();
			// 再次显示"加载更多"
			moreTextView.setVisibility(View.VISIBLE);
			// 再次隐藏“进度条”
			loadProgressBar.setVisibility(View.GONE);
		}

	}

	/** 刷新完成，还原界面各种状态，修改更新时间 */
	public void onRefreshComplete() {
		state = DONE;
		// 更新最新刷新时间
		lastUpdatedTextView.setText("最近更新:" + new Date().toLocaleString());
		changeHeaderViewByState();
	}
}
