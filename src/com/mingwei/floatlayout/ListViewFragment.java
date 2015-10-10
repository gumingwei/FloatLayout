package com.mingwei.floatlayout;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ListViewFragment extends Fragment {

	private View mContentView;
	private ListView mListView;
	private List<String> mList = new ArrayList<String>();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContentView = LayoutInflater.from(getActivity()).inflate(R.layout.float_layout_inner_listview, null);
		mListView = (ListView) mContentView.findViewById(R.id.float_layout_inner_view);
		initData();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return mContentView;
	}

	private void initData() {
		for (int i = 0; i < 100; i++) {
			mList.add("ListView_Item" + i);
		}
		mListView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mList));
	}

	static Fragment getInstain() {
		Fragment fragment = new ListViewFragment();
		return fragment;
	}
}
