//
//UZModule
//
//Modified by magic 15/9/14.
//Copyright (c) 2015年 APICloud. All rights reserved.
//
package com.uzmap.pkg.uzmodules.uzUIChatBox;

import java.util.ArrayList;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzkit.UZUtility;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.GridView;

public class ExtraPagerAdapter extends PagerAdapter {
	private ArrayList<ExpandData> mExpandData;
	private Context mContext;
	private UZModuleContext mModuleContext;
	private ViewPager mViewPager;
	private UzUIChatBox mModule;
	private int mBgColor;

	public ExtraPagerAdapter(UzUIChatBox module,
			ArrayList<ExpandData> mExpandData, Context mContext,
			ViewPager mViewPager, UZModuleContext mModuleContext,int bgColor) {
		this.mModule = module;
		this.mExpandData = mExpandData;
		this.mContext = mContext;
		this.mViewPager = mViewPager;
		this.mModuleContext = mModuleContext;
		this.mBgColor = bgColor;
	}

	@Override
	public int getCount() {
		return (int) Math.ceil((double) mExpandData.size() / (double) 8) == 0 ? 1
				: (int) Math.ceil((double) mExpandData.size() / (double) 8);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public Object instantiateItem(View collection, int position) {
		int mo_chatbox_gridId = UZResourcesIDFinder
				.getResLayoutID("mo_uichatbox_expand_grid");
		View layout = View.inflate(mContext, mo_chatbox_gridId, null);
		int initialPosition = position * 8;
		ArrayList<ExpandData> imageInPage = new ArrayList<ExpandData>();
		for (int i = initialPosition; i < initialPosition + 8
				&& i < mExpandData.size(); i++) {
			imageInPage.add(mExpandData.get(i));
		}
		int emoticons_gridId = UZResourcesIDFinder.getResIdID("expand_grid");
		GridView grid = (GridView) layout.findViewById(emoticons_gridId);
		grid.setPadding(0, UZUtility.dipToPix(20), 0, 0);
		grid.setBackgroundColor(mBgColor);
		ExpandGridAdapter adapter = new ExpandGridAdapter(mModule, imageInPage,
				mContext, mViewPager, mModuleContext);
		grid.setAdapter(adapter);
		((ViewPager) collection).addView(layout);
		return layout;
	}

	@Override
	public void destroyItem(View collection, int position, Object view) {
		((ViewPager) collection).removeView((View) view);
	}

}
