//
//UZModule
//
//Modified by magic 15/9/14.
//Copyright (c) 2015年 APICloud. All rights reserved.
//
package com.uzmap.pkg.uzmodules.uzUIChatBox;

import com.uzmap.pkg.uzkit.UZUtility;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.widget.EditText;

public class ChatBoxEditText extends EditText {
	private Paint mPaint;
	private RectF mRectF;

	public ChatBoxEditText(Context context) {
		super(context);
		init();
	}

	private void init() {
		mPaint = new Paint();
		mPaint.setStrokeWidth(2);
		mRectF = new RectF();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		mRectF.set(getScrollX(),getScrollY(), getWidth()
				+ getScrollX(), getHeight() + getScrollY() );
		//mPaint.setColor(Constans.INPUT_BOX_BG_COLOR);
		mPaint.setColor(UZUtility.parseCssColor("#ecedee"));
		mPaint.setStyle(Style.FILL);
		canvas.drawRoundRect(mRectF, 0, 0, mPaint);
		mPaint.setStyle(Style.STROKE);	
		mPaint.setColor(Constans.INPUT_BOX_BORDER_COLOR);
		//mPaint.setColor(UZUtility.parseCssColor("#ffffff"));
		mRectF.set(2 + getScrollX(), 2 + getScrollY(), getWidth() - 3
				+ getScrollX(), getHeight() + getScrollY() - 1);
		canvas.drawRoundRect(mRectF, 45, 45, mPaint);
		mPaint.setColor(Constans.INPUT_BOX_BG_COLOR);
		//mPaint.setColor(UZUtility.parseCssColor("#28d4bc"));
		mPaint.setStyle(Style.FILL);
		canvas.drawRoundRect(mRectF, 45, 45, mPaint);
		super.onDraw(canvas);
	}
}
