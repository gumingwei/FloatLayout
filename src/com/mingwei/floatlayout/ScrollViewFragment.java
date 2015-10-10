package com.mingwei.floatlayout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ScrollViewFragment extends Fragment {
	private View mContentView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContentView = LayoutInflater.from(getActivity()).inflate(R.layout.float_layout_inner_scrollview, null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return mContentView;
	}

	static Fragment getInstain() {
		Fragment fragment = new ScrollViewFragment();
		return fragment;
	}
}
