//
//UZModule
//
//Modified by magic 15/9/14.
//Copyright (c) 2015年 APICloud. All rights reserved.
//
package com.lbbniu.daishu.uzUIChatBox;

import android.content.Context;
import android.os.Handler;
import android.widget.LinearLayout;

public class ChatBoxLayout extends LinearLayout {

	private UzUIChatBox mUiChatBox;
	private Handler mDelayedHandler = new Handler();
	private boolean isAutoFocus;

	public ChatBoxLayout(Context context) {
		super(context);
	}

	public void setUiChatBox(UzUIChatBox uiChatBox) {
		this.mUiChatBox = uiChatBox;
	}

	public void setAutoFocus(boolean isAutoFocus) {
		this.isAutoFocus = isAutoFocus;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mDelayedHandler.postDelayed(mDelayedShowKeyBoardRunnable, 300);
	}

	private Runnable mDelayedShowKeyBoardRunnable = new Runnable() {
		@Override
		public void run() {
			if (isAutoFocus) {
				mUiChatBox.showKeybord();
			}
			mUiChatBox.openCallBack("show", 0);
		}
	};

}
