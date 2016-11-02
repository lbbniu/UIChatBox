//
//UZModule
//
//Modified by magic 15/9/14.
//Copyright (c) 2015年 APICloud. All rights reserved.
//
package com.lbbniu.daishu.uzUIChatBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import com.uzmap.pkg.uzcore.UZCoreUtil;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;
import com.uzmap.pkg.uzkit.UZUtility;
import com.lbbniu.daishu.uzUIChatBox.GridAdapter.KeyClickListener;
import com.lbbniu.daishu.uzUIChatBox.ViewBackground.BackgroundType;

public class UzUIChatBox extends UZModule implements OnClickListener,
		TextWatcher, AnimationListener, KeyClickListener, OnPageChangeListener {
	private static final int NO_OF_EMOTICONS_PER_PAGE = 28;
	private UZModuleContext mModuleContext;
	private UZModuleContext mToggleKeyboardCallBack;
	private UZModuleContext mChangeCallBack;
	private UZModuleContext mPressCallBack;
	private UZModuleContext mPressCancelCallBack;
	private UZModuleContext mMoveOutCallBack;
	private UZModuleContext mMoveOutCancelCallBack;
	private UZModuleContext mMoveInCallBack;
	private UZModuleContext mShowRecordCallBack;
	private UZModuleContext mShowEmotionCallBack;
	private UZModuleContext mShowExtrasCallBack;
	private UZModuleContext mValueChangeCallBack;
	private JsParamsUtil mJsParamsUtil;
	private View mSpaceView;
	private ChatBoxLayout mChatBoxLayout;
	private InputLinearLayout mEditLayout;
	private RelativeLayout mTableLayout;
	private ChatBoxEditText mEditText;
	private Button mRecordBtn;
	private ImageView mSpeechBtn;
	private ImageView mFaceBtn;
	private FrameLayout mSendLayout;
	private ImageView mExstraBtn;
	private Button mSendBtn;
	private ViewPager mFaceViewPager;
	private ViewPager mExtraViewPager;
	private IndictorView mIndictorView;
	private String mEmotionsPath;
	private Map<String, String> mEmotionMap;
	private Map<String, String> mInsertEmotionMap;
	private ArrayList<String> mEmotionsList;
	private ArrayList<ExpandData> mExtraParams;
	private BitmapUtils mBitmapUtils;
	private StateListDrawable mSpeechBtnDrawable;
	private StateListDrawable mFaceBtnDrawable;
	private StateListDrawable mSpeechKeyDrawable;
	private StateListDrawable mKeyboardBtnDrawable;
	private Drawable mRecordNoramlDrawable;
	private Drawable mRecordActiveDrawable;
	private Animation mSendBtnShowAnimation;
	private Animation mSendBtnHideAnimation;
	private CharSequence mTempMsg;
	private String mIndicatorTarget;
	private String mRecordNormalTitle;
	private String mRecordActiveTitle;
	private LayoutListener mLayoutListener;
	private int mKeyboardHeight;
	private boolean isOnlySendBtnExist;
	private boolean isShowAnimation;
	private boolean isKeyBoardVisible;
	private boolean isIndicatorVisible;
	private Handler mDelayedHandler = new Handler();
	private Runnable mDelayedRunnable = new Runnable() {
		@Override
		public void run() {
			isCallBack = true;
			mTableLayout.setVisibility(View.VISIBLE);
		}
	};

	public UzUIChatBox(UZWebView webView) {
		super(webView);
	}

	public void jsmethod_open(UZModuleContext moduleContext) {
		if (mChatBoxLayout == null) {
			initParams(moduleContext);
			createViews();
			initEmotions();
			initExtras();
			initViews();
			setLayoutListener(mEditText);
		} else {
			mChatBoxLayout.setVisibility(View.VISIBLE);
		}
	}

	@SuppressWarnings("deprecation")
	public void jsmethod_close(UZModuleContext moduleContext) {
		if (mChatBoxLayout != null) {
			removeViewFromCurWindow(mChatBoxLayout);
			removeViewFromCurWindow(mSpaceView);
			mDelayedHandler.removeCallbacks(mDelayedRunnable);
			mDelayedHandler.removeCallbacks(mDelayedShowKeyBoardRunnable);
			if (mEditText != null) {
				mEditText.getViewTreeObserver().removeGlobalOnLayoutListener(
						mLayoutListener);
				mLayoutListener = null;
			}
			mChatBoxLayout = null;
			mToggleKeyboardCallBack = null;
			mChangeCallBack = null;
			mPressCallBack = null;
			mPressCancelCallBack = null;
			mMoveOutCallBack = null;
			mMoveOutCancelCallBack = null;
			mMoveInCallBack = null;
			mShowRecordCallBack = null;
			mShowEmotionCallBack = null;
			mShowExtrasCallBack = null;
			mValueChangeCallBack = null;
		}
	}

	public void jsmethod_hide(UZModuleContext moduleContext) {
		if (mChatBoxLayout != null) {
			mChatBoxLayout.setVisibility(View.GONE);
			mSpaceView.setVisibility(View.GONE);
		}
	}

	public void jsmethod_show(UZModuleContext moduleContext) {
		if (mChatBoxLayout != null) {
			mChatBoxLayout.setVisibility(View.VISIBLE);
			mSpaceView.setVisibility(View.VISIBLE);
		}
	}

	public void jsmethod_popupBoard(UZModuleContext moduleContext) {
		if (mChatBoxLayout != null) {
			String target = moduleContext.optString("target", "emotion");
			if (target.equals("emotion")) {
				clickFaceBtnShowTable();
				setEmotionPageNums();
			} else {
				clickExtraBtnShowTable();
				setExtraPageNums();
			}
		}
	}

	public void jsmethod_closeBoard(UZModuleContext moduleContext) {
		if (mChatBoxLayout != null) {
			mTableLayout.setVisibility(View.GONE);
		}
	}

	public void jsmethod_popupKeyboard(UZModuleContext moduleContext) {
		if (mChatBoxLayout != null) {
			showKeybord();
		}
	}

	public void jsmethod_closeKeyboard(UZModuleContext moduleContext) {
		if (mChatBoxLayout != null) {
			hideInputMethod(mContext.getCurrentFocus());
			mTableLayout.setVisibility(View.GONE);
		}
	}

	public void jsmethod_value(UZModuleContext moduleContext) {
		if (mChatBoxLayout != null) {
			String msg = mJsParamsUtil.insertMsg(moduleContext);
			if (moduleContext.isNull("msg")) {
				valueCallBack(moduleContext, getEditTextStr());
			} else if (TextUtils.isEmpty(msg)) {
				mEditText.setText("");
				valueCallBack(moduleContext, "");
			} else {
				SpannableString insertMsg = parseMsg(msg);
				mEditText.setText(insertMsg);
				valueCallBack(moduleContext, getEditTextStr());
			}
		}
	}

	public void jsmethod_setPlaceholder(UZModuleContext moduleContext) {
		if (mChatBoxLayout != null) {
			String hint = mJsParamsUtil.placeholder(moduleContext);
			mEditText.setHint(hint);
		}
	}

	public void jsmethod_insertValue(UZModuleContext moduleContext) {
		if (mChatBoxLayout != null) {
			String msg = mJsParamsUtil.insertMsg(moduleContext);
			int defaultIndex = mEditText.getText().length();
			int index = mJsParamsUtil.insertIndex(moduleContext, defaultIndex);
			SpannableString insertMsg = parseMsg(msg);
			mEditText.getText().insert(index, insertMsg);
		}
	}

	public void jsmethod_addEventListener(UZModuleContext moduleContext) {
		String target = mJsParamsUtil.listenerTarget(moduleContext);
		String name = mJsParamsUtil.listenerName(moduleContext);
		if (target.equals("inputBar")) {
			if (name.equals("move")) {
				mToggleKeyboardCallBack = moduleContext;
			} else if (name.equals("change")) {
				mChangeCallBack = moduleContext;
			} else if (name.equals("showRecord")) {
				mShowRecordCallBack = moduleContext;
			} else if (name.equals("showEmotion")) {
				mShowEmotionCallBack = moduleContext;
			} else if (name.equals("showExtras")) {
				mShowExtrasCallBack = moduleContext;
			} else if (name.equals("valueChanged")) {
				mValueChangeCallBack = moduleContext;
			}
		} else if (target.equals("recordBtn")) {
			if (name.equals("press")) {
				mPressCallBack = moduleContext;
			} else if (name.equals("press_cancel")) {
				mPressCancelCallBack = moduleContext;
			} else if (name.equals("move_out")) {
				mMoveOutCallBack = moduleContext;
			} else if (name.equals("move_out_cancel")) {
				mMoveOutCancelCallBack = moduleContext;
			} else if (name.equals("move_in")) {
				mMoveInCallBack = moduleContext;
			}
		}
	}

	public void jsmethod_reloadExtraBoard(UZModuleContext moduleContext) {
		mExtraParams = mJsParamsUtil.extras(moduleContext);
		if (mExtraParams != null) {
			setExtraPageNums();
			setExtraAdapter(moduleContext);
		}
	}

	private void initParams(UZModuleContext moduleContext) {
		mModuleContext = moduleContext;
		mBitmapUtils = new BitmapUtils(getWidgetInfo(), mContext);
		mJsParamsUtil = JsParamsUtil.getInstance();
		mEmotionMap = new HashMap<String, String>();
		mInsertEmotionMap = new HashMap<String, String>();
		mEmotionsList = new ArrayList<String>();
		mExtraParams = new ArrayList<ExpandData>();
		isOnlySendBtn();
		initConstansColors();
		initSendBtnAnimations();
	}

	private void isOnlySendBtn() {
		isOnlySendBtnExist = !isBtnShow("extrasBtn");
	}

	private void createViews() {
		mSpaceView = new View(mContext);
		mChatBoxLayout = new ChatBoxLayout(mContext);
		mChatBoxLayout.setUiChatBox(this);
		mEditLayout = new InputLinearLayout(mContext);
		mTableLayout = new RelativeLayout(mContext);
		mEditText = new ChatBoxEditText(mContext);
		mRecordBtn = new Button(mContext);
		mSpeechBtn = new ImageView(mContext);
		mFaceBtn = new ImageView(mContext);
		mSendLayout = new FrameLayout(mContext);
		mExstraBtn = new ImageView(mContext);
		mFaceViewPager = new ViewPager(mContext);
		mExtraViewPager = new ViewPager(mContext);
		mIndictorView = new IndictorView(mContext);
	}

	private void initIndictorView(int pointNums) {
		initIndictorLayout();
		initIndictorParams(pointNums);
	}

	private void initIndictorLayout() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				UZUtility.dipToPix(20));
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mIndictorView.setLayoutParams(params);
	}

	private void initIndictorParams(int pointNums) {
		int normalColor = mJsParamsUtil.indicatorColor(mModuleContext);
		int activeColor = mJsParamsUtil.indicatorActiveColor(mModuleContext);
		int w = (2 * pointNums - 1) * UZUtility.dipToPix(3) * 2;
		int screenWidth = mJsParamsUtil.getScreenWidth(mContext);
		int startX = (int) (screenWidth / 2.0 - w / 2.0);
		mIndictorView.initParams(pointNums, startX, normalColor, activeColor);
		mIndicatorTarget = mJsParamsUtil.indicatorTarget(mModuleContext);
		JSONObject jsonObject = mJsParamsUtil.innerParamJSONObject(
				mModuleContext, "styles", "indicator");
		isIndicatorVisible = (jsonObject == null ? false : true);
	}

	private void chargeIndictorVisible(String tableType) {
		if (isIndicatorVisible) {
			if (mIndicatorTarget.equals("both")
					|| tableType.equals(mIndicatorTarget)) {
				mIndictorView.setVisibility(View.VISIBLE);
			} else {
				mIndictorView.setVisibility(View.GONE);
			}
		} else {
			mIndictorView.setVisibility(View.GONE);
		}
	}

	private void initEmotions() {
		mEmotionsPath = mJsParamsUtil.emotionPath(mModuleContext);
		int jsonNameIndex = mEmotionsPath.lastIndexOf('/') + 1;
		String jsonName = mEmotionsPath.substring(jsonNameIndex);
		String realPath = makeRealPath(mEmotionsPath + "/" + jsonName + ".json");
		String emotionsStr = mJsParamsUtil.parseJsonFile(realPath);
		parseEmotionJson(emotionsStr);
		initFaceViewPager();
	}

	private void initExtras() {
		mExtraParams = mJsParamsUtil.extras(mModuleContext);
		if (mExtraParams != null) {
			setExtraPageNums();
			setExtraAdapter(mModuleContext);
		}
	}

	private void setExtraAdapter(UZModuleContext moduleContext) {
		ExtraPagerAdapter expandMyPagerAdapter = createExtraAdapter(moduleContext);
		mExtraViewPager.setAdapter(expandMyPagerAdapter);
		mExtraViewPager.setOnPageChangeListener(this);
	}

	private void setExtraPageNums() {
		int size = mExtraParams.size();
		int pageSize = 8;
		int pageNums = (size + pageSize - 1) / pageSize;
		mExtraViewPager.setOffscreenPageLimit(pageNums);
		initIndictorView(pageNums);
	}

	private ExtraPagerAdapter createExtraAdapter(UZModuleContext moduleContext) {
		int bgColor = mJsParamsUtil.inputBarBgColor(moduleContext);
		return new ExtraPagerAdapter(this, mExtraParams, mContext,
				mExtraViewPager, moduleContext, bgColor);
	}

	private void parseEmotionJson(String emotionsStr) {
		if (emotionsStr != null && !TextUtils.isEmpty(emotionsStr)) {
			try {
				JSONArray jsonArr = new JSONArray(emotionsStr);
				JSONObject jsonObject = null;
				for (int i = 0; i < jsonArr.length(); i++) {
					jsonObject = jsonArr.optJSONObject(i);
					String nameStr = jsonObject.optString("name");
					String name = mEmotionsPath + "/" + nameStr + ".png";
					String text = jsonObject.optString("text");
					mEmotionMap.put(name, text);
					mInsertEmotionMap.put(text, name);
					mEmotionsList.add(name);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void initFaceViewPager() {
		setEmotionPageNums();
		addDeleteEmotionBtn();
		setFaceAdapter();
	}

	private void setFaceAdapter() {
		FacePagerAdapter adapter = createFacePageAdapter();
		mFaceViewPager.setAdapter(adapter);
		mFaceViewPager.setOnPageChangeListener(this);
	}

	private FacePagerAdapter createFacePageAdapter() {
		int bgColor = mJsParamsUtil.inputBarBgColor(mModuleContext);
		return new FacePagerAdapter(mContext, mEmotionsList, mBitmapUtils,
				this, bgColor);
	}

	private void addDeleteEmotionBtn() {
		String delete = mEmotionsPath + "/delete.png";
		int size = 0;
		for (int i = 1; i <= mFaceViewPager.getOffscreenPageLimit(); i++) {
			int deleteIndex = (i * NO_OF_EMOTICONS_PER_PAGE) - 1;
			size = mEmotionsList.size();
			if (deleteIndex > size - 1) {
				mEmotionsList.add(size, delete);
			} else {
				mEmotionsList.add(deleteIndex, delete);
			}
			size = mEmotionsList.size();
			if (deleteIndex == (size - 1)) {
				mEmotionsList.add(delete);
			}
		}
	}

	private void setEmotionPageNums() {
		int size = mEmotionsList.size();
		int pageSize = NO_OF_EMOTICONS_PER_PAGE;
		int pageNums = (size + pageSize - 1) / pageSize;
		mFaceViewPager.setOffscreenPageLimit(pageNums);
		initIndictorView(pageNums);
	}

	private void initViews() {
		initInputAreaView();
		initTableLayout();
		initChatBoxLayout();
		insertSpaceView();
		insertCahtBoxLayout();
	}

	private void initInputAreaView() {
		initSpeechBtn();
		initRecordBtn();
		initEditText();
		initFaceBtn();
		initSendBtn();
		initExtraBtn();
		initSendLayout();
		initEditLayout();
	}

	private void initConstansColors() {
		int barBorderColor = mJsParamsUtil.inputBarBorderColor(mModuleContext);
		Constans.INPUT_LAYOUT_BORDER_COLOR = barBorderColor;
		int borderColor = mJsParamsUtil.inputBoxBorderColor(mModuleContext);
		Constans.INPUT_BOX_BORDER_COLOR = borderColor;
		int bgColor = mJsParamsUtil.inputBoxBgColor(mModuleContext);
		Constans.INPUT_BOX_BG_COLOR = bgColor;
	}

	private void initSendBtnAnimations() {
		int showAnimaId = UZResourcesIDFinder.getResAnimID("unzoom_in");
		mSendBtnShowAnimation = AnimationUtils.loadAnimation(mContext,
				showAnimaId);
		mSendBtnShowAnimation.setAnimationListener(this);
		int hideAnimaId = UZResourcesIDFinder.getResAnimID("unzoom_out");
		mSendBtnHideAnimation = AnimationUtils.loadAnimation(mContext,
				hideAnimaId);
		mSendBtnHideAnimation.setAnimationListener(this);
	}

	@SuppressLint("ClickableViewAccessibility")
	private void insertSpaceView() {
		mSpaceView.setBackgroundColor(Color.TRANSPARENT);
		mSpaceView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				onSpaceViewClick();
				return false;
			}
		});
		String fixedOn = mModuleContext.optString("fixedOn");
		insertViewToCurWindow(mSpaceView, spaceViewLayout(), fixedOn);
	}

	private RelativeLayout.LayoutParams spaceViewLayout() {
		return new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
	}

	@SuppressWarnings("deprecation")
	private void onSpaceViewClick() {
		mTableLayout.setVisibility(View.GONE);
		mFaceBtn.setBackgroundDrawable(mFaceBtnDrawable);
		if (mContext != null) {
			hideInputMethod(mContext.getCurrentFocus());
		}
	}

	private void insertCahtBoxLayout() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mChatBoxLayout.setOrientation(LinearLayout.VERTICAL);
		String fixedOn = mModuleContext.optString("fixedOn");
		insertViewToCurWindow(mChatBoxLayout, params, fixedOn);
	}

	private void autoFocus() {
		boolean isAutoFocus = mJsParamsUtil.autoFocus(mModuleContext);
		mChatBoxLayout.setAutoFocus(isAutoFocus);
	}

	private void initChatBoxLayout() {
		mChatBoxLayout.addView(mEditLayout);
		mChatBoxLayout.addView(mTableLayout);
		autoFocus();
	}

	private void initEditLayout() {
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		mEditLayout.setOrientation(LinearLayout.HORIZONTAL);
		mEditLayout.setLayoutParams(params);
		mEditLayout.addView(mSpeechBtn);
		mEditLayout.addView(mRecordBtn);
		mEditLayout.addView(mEditText);
		mEditLayout.addView(mFaceBtn);
		mEditLayout.addView(mSendLayout);
		mEditLayout.setOnClickListener(this);
		initEditLayoutColors();
	}

	private void initEditLayoutColors() {
		int bgColor = mJsParamsUtil.inputBarBgColor(mModuleContext);
		mEditLayout.setBackgroundColor(bgColor);
	}

	private void initTableLayout() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				UZUtility.dipToPix(50) * 4 + UZUtility.dipToPix(20));
		mTableLayout.setLayoutParams(params);
		mTableLayout.setVisibility(View.GONE);
		mTableLayout.addView(mFaceViewPager);
		mTableLayout.addView(mExtraViewPager);
		mTableLayout.addView(mIndictorView);
		int bgColor = mJsParamsUtil.inputBarBgColor(mModuleContext);
		mTableLayout.setBackgroundColor(bgColor);
		mFaceViewPager.setBackgroundColor(UZUtility.parseCssColor("#00FF00"));
		mExtraViewPager.setBackgroundColor(UZUtility.parseCssColor("#00FF00"));
	}

	private void initSpeechBtn() {
		LayoutParams params = new LayoutParams(
				UZUtility.dipToPix(Constans.BTN_SIZE),
				UZUtility.dipToPix(Constans.BTN_SIZE));
		params.gravity = Gravity.BOTTOM;
		int margin = UZUtility.dipToPix(Constans.INPUT_BOX_MARGIN);
		params.setMargins(margin, margin, margin, margin);
		mSpeechBtn.setLayoutParams(params);
		initSpeechBtnVisible();
		initSpeechBtnBg();
		mSpeechBtn.setOnClickListener(this);
	}

	private void initSpeechBtnVisible() {
		if (!isBtnShow("speechBtn")) {
			mSpeechBtn.setVisibility(View.GONE);
		}
	}

	@SuppressWarnings("deprecation")
	private void initSpeechBtnBg() {
		String normalStr = mJsParamsUtil.speechBtnNormalImg(mModuleContext);
		BitmapDrawable normal = createDrawable(normalStr, null);
		String activeStr = mJsParamsUtil.speechBtnActiveImg(mModuleContext);
		BitmapDrawable active = createDrawable(activeStr, normal);
		if (normal != null) {
			mSpeechBtnDrawable = createStateDrawable(normal, active);
			mSpeechBtn.setBackgroundDrawable(mSpeechBtnDrawable);
		}
	}

	private void initRecordBtn() {
		initRecordBtnLayout();
		initRecordBtnTextParams();
		initRecordBtnBg();
		setRecordBtnListener();
	}

	private void initRecordBtnLayout() {
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				UZUtility.dipToPix(Constans.BTN_SIZE), 1.0f);
		params.gravity = Gravity.CENTER_VERTICAL;
		int margin = UZUtility.dipToPix(Constans.INPUT_BOX_MARGIN);
		params.setMargins(margin, margin, margin, margin);
		mRecordBtn.setLayoutParams(params);
		mRecordBtn.setPadding(0, 0, 0, 0);
		mRecordBtn.setTransformationMethod(null);
		mRecordBtn.setVisibility(View.GONE);
	}

	private void initRecordBtnTextParams() {
		mRecordNormalTitle = mJsParamsUtil.recordBtnNormalTitle(mModuleContext);
		mRecordActiveTitle = mJsParamsUtil.recordBtnActiveTitle(mModuleContext);
		mRecordBtn.setText(mRecordNormalTitle);
		int color = mJsParamsUtil.recordBtnColor(mModuleContext);
		mRecordBtn.setTextColor(color);
		int size = mJsParamsUtil.recordBtnFontSize(mModuleContext);
		mRecordBtn.setTextSize(size);
	}

	@SuppressWarnings("deprecation")
	private void initRecordBtnBg() {
		String normalBg = mJsParamsUtil.recordBtnNormalImg(mModuleContext);
		String activeBg = mJsParamsUtil.recordBtnActiveImg(mModuleContext);
		if (isBitmap(normalBg)) {
			mRecordNoramlDrawable = new BitmapDrawable(mContext.getResources(),
					mBitmapUtils.generateBitmap(normalBg));
			mRecordActiveDrawable = new BitmapDrawable(mContext.getResources(),
					mBitmapUtils.generateBitmap(activeBg));
		} else {
			mRecordNoramlDrawable = new ColorDrawable(
					UZUtility.parseCssColor(normalBg));
			mRecordActiveDrawable = new ColorDrawable(
					UZUtility.parseCssColor(activeBg));
		}
		mRecordBtn.setBackgroundDrawable(mRecordNoramlDrawable);
	}

	private void setRecordBtnListener() {
		mRecordBtn.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					onRecordEventDown();
					break;
				case MotionEvent.ACTION_MOVE:
					onRecordEventMove(event);
					break;
				case MotionEvent.ACTION_UP:
					onRecordEventUp(event);
					break;
				default:
					break;
				}
				return false;
			}
		});
	}

	private boolean isInRecord;

	@SuppressWarnings("deprecation")
	private void onRecordEventDown() {
		recordEventBack("press");
		isInRecord = true;
		mRecordBtn.setText(mRecordActiveTitle);
		mRecordBtn.setBackgroundDrawable(mRecordActiveDrawable);
	}

	private void onRecordEventMove(MotionEvent event) {
		if (!isTouchInRecordBtn(event)) {
			if (isInRecord) {
				recordEventBack("move_out");
			}
			isInRecord = false;
		} else {
			if (!isInRecord) {
				recordEventBack("move_in");
				isInRecord = true;
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void onRecordEventUp(MotionEvent event) {
		if (isTouchInRecordBtn(event)) {
			recordEventBack("press_cancel");
		} else {
			recordEventBack("move_out_cancel");
		}
		mRecordBtn.setText(mRecordNormalTitle);
		mRecordBtn.setBackgroundDrawable(mRecordNoramlDrawable);
	}

	private void recordEventBack(String eventName) {
		if (eventName.equals("press")) {
			if (mPressCallBack != null) {
				mPressCallBack.success(null, false);
			}
		} else if (eventName.equals("press_cancel")) {
			if (mPressCancelCallBack != null) {
				mPressCancelCallBack.success(null, false);
			}
		} else if (eventName.equals("move_out")) {
			if (mMoveOutCallBack != null) {
				mMoveOutCallBack.success(null, false);
			}
		} else if (eventName.equals("move_out_cancel")) {
			if (mMoveOutCancelCallBack != null) {
				mMoveOutCancelCallBack.success(null, false);
			}
		} else if (eventName.equals("move_in")) {
			if (mMoveInCallBack != null) {
				mMoveInCallBack.success(null, false);
			}
		}
	}

	private boolean isTouchInRecordBtn(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		int[] location = new int[2];
		mRecordBtn.getLocationOnScreen(location);
		int viewX = location[0];
		float viewWidth = mRecordBtn.getWidth();
		float viewHeight = mRecordBtn.getHeight();
		if (x <= viewX + viewWidth && x >= viewX && y <= viewHeight && y >= 0) {
			return true;
		}
		return false;
	}

	private boolean isBitmap(String jsonStr) {
		if (!TextUtils.isEmpty(jsonStr)) {
			if (jsonStr.contains("://")) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	private void initEditText() {
		initEditTextParams();
		initEditTextLayout();
		initEditColors();
		initEditListener();
	}

	private void initEditTextParams() {
		String placeholder = mJsParamsUtil.placeholder(mModuleContext);
		mEditText.setHint(placeholder);
		int maxRows = mJsParamsUtil.maxRows(mModuleContext);
		mEditText.setMaxLines(maxRows);
	}

	private void initEditTextLayout() {
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, 1.0f);
		params.gravity = Gravity.CENTER_VERTICAL;
		int margin = UZUtility.dipToPix(Constans.INPUT_BOX_MARGIN);
		params.setMargins(margin, margin, margin, margin);
		mEditText.setLayoutParams(params);
		mEditText.setGravity(Gravity.CENTER_VERTICAL);
		mEditText.setPadding(margin, margin, margin, margin);
	}

	@SuppressWarnings("deprecation")
	private void initEditColors() {
		GradientDrawable gradientDrawable = new GradientDrawable();
		gradientDrawable.setColor(Color.WHITE);
		gradientDrawable.setCornerRadius(Constans.INPUT_BOX_CORNER);
		mEditText.setBackgroundDrawable(gradientDrawable);
	}

	@SuppressLint("ClickableViewAccessibility")
	private void initEditListener() {
		mEditText.addTextChangedListener(this);
		mEditText.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				onEditTextClick();
				return false;
			}
		});
	}

	private void initFaceBtn() {
		initFaceBtnLayout();
		initFaceBtnBg();
		mFaceBtn.setOnClickListener(this);
	}

	private void initFaceBtnLayout() {
		LayoutParams params = new LayoutParams(
				UZUtility.dipToPix(Constans.BTN_SIZE),
				UZUtility.dipToPix(Constans.BTN_SIZE));
		params.gravity = Gravity.BOTTOM;
		int margin = UZUtility.dipToPix(Constans.INPUT_BOX_MARGIN);
		params.setMargins(margin, margin, margin, margin);
		mFaceBtn.setLayoutParams(params);
	}

	@SuppressWarnings("deprecation")
	private void initFaceBtnBg() {
		int faceBtnImgId = UZResourcesIDFinder
				.getResDrawableID("mo_uichatbox_face_btn");
		BitmapDrawable defaultValue = createDrawable(faceBtnImgId);
		String normalImgStr = mJsParamsUtil.faceBtnNormalImg(mModuleContext);
		BitmapDrawable normal = createDrawable(normalImgStr, defaultValue);
		String activeImgStr = mJsParamsUtil.faceBtnActiveImg(mModuleContext);
		BitmapDrawable active = createDrawable(activeImgStr, normal);
		mFaceBtnDrawable = createStateDrawable(normal, active);
		mFaceBtn.setBackgroundDrawable(mFaceBtnDrawable);
		initKeyboardDrawable();
	}

	private void initKeyboardDrawable() {
		int keyboardBtnImgId = UZResourcesIDFinder
				.getResDrawableID("mo_uichatbox_keyboard_btn");
		BitmapDrawable defaultValue = createDrawable(keyboardBtnImgId);
		String normalStr = mJsParamsUtil.keyboardBtnNormalImg(mModuleContext);
		BitmapDrawable normal = createDrawable(normalStr, defaultValue);
		String activeStr = mJsParamsUtil.keyboardBtnActiveImg(mModuleContext);
		BitmapDrawable active = createDrawable(activeStr, normal);
		if (normal != null) {
			mKeyboardBtnDrawable = createStateDrawable(normal, active);
			mSpeechKeyDrawable = createStateDrawable(normal, active);
		}
	}

	private void initExtraBtn() {
		initExtraBtnLayout();
		initExtraBtnVisible();
		initExtraBtnBg();
		mExstraBtn.setOnClickListener(this);
	}

	private void initExtraBtnLayout() {
		LayoutParams params = new LayoutParams(
				UZUtility.dipToPix(Constans.BTN_SIZE),
				UZUtility.dipToPix(Constans.BTN_SIZE));
		params.gravity = Gravity.BOTTOM;
		mExstraBtn.setLayoutParams(params);
	}

	private void initExtraBtnVisible() {
		if (!isBtnShow("extrasBtn")) {
			mExstraBtn.setVisibility(View.GONE);
			mSendBtn.setVisibility(View.VISIBLE);
		}
	}

	@SuppressWarnings("deprecation")
	private void initExtraBtnBg() {
		String normalStr = mJsParamsUtil.extrasBtnNormalImg(mModuleContext);
		BitmapDrawable normal = createDrawable(normalStr, null);
		String activeStr = mJsParamsUtil.extrasBtnActiveImg(mModuleContext);
		BitmapDrawable active = createDrawable(activeStr, normal);
		if (normal != null) {
			StateListDrawable bgDrawable = createStateDrawable(normal, active);
			mExstraBtn.setBackgroundDrawable(bgDrawable);
		}
	}

	private void initSendBtn() {
		initSendBtnBg();
		initSendBtnLayout();
		mSendBtn.setOnClickListener(this);
	}

	private void initSendBtnLayout() {
		LayoutParams params = new LayoutParams(
				UZUtility.dipToPix(Constans.SEND_BTN_SIZE),
				UZUtility.dipToPix(Constans.BTN_SIZE));
		params.gravity = Gravity.BOTTOM;
		mSendBtn.setLayoutParams(params);
		mSendBtn.setVisibility(View.GONE);
	}

	private void initSendBtnBg() {
		initSendBtnStyles(mModuleContext);
	}

	private void initSendLayout() {
		LayoutParams params = new LayoutParams(
				UZUtility.dipToPix(Constans.SEND_BTN_SIZE),
				UZUtility.dipToPix(Constans.BTN_SIZE));
		params.gravity = Gravity.BOTTOM;
		int margin = UZUtility.dipToPix(Constans.INPUT_BOX_MARGIN);
		params.setMargins(margin, margin, margin, margin);
		mSendLayout.setLayoutParams(params);
		mSendLayout.addView(mExstraBtn);
		mSendLayout.addView(mSendBtn);
	}

	public void showKeybord() {
		mEditText.requestFocus();
		InputMethodManager inputManager = (InputMethodManager) mEditText
				.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.showSoftInput(mEditText, 0);
	}

	private void hideInputMethod(View view) {
		if (mContext != null && view != null) {
			InputMethodManager mInputMethodManager = (InputMethodManager) mContext
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (mInputMethodManager != null)
				mInputMethodManager.hideSoftInputFromWindow(
						view.getWindowToken(), 0);
		}
	}

	private boolean isBtnShow(String btnName) {
		JSONObject btnJson = mJsParamsUtil.innerParamJSONObject(mModuleContext,
				"styles", btnName);
		if (btnJson == null) {
			return false;
		}
		return true;
	}

	private BitmapDrawable createDrawable(String imgPath,
			BitmapDrawable defaultValue) {
		String realPath = makeRealPath(imgPath);
		Bitmap bitmap = mJsParamsUtil.getBitmap(realPath);
		if (bitmap != null) {
			return new BitmapDrawable(mContext.getResources(), bitmap);
		}
		return defaultValue;
	}

	private BitmapDrawable createDrawable(int resId) {
		Resources resources = mContext.getResources();
		Drawable drawable = resources.getDrawable(resId);
		return (BitmapDrawable) drawable;
	}

	private StateListDrawable createStateDrawable(BitmapDrawable nomalDrawable,
			BitmapDrawable pressDrawable) {
		StateListDrawable sd = new StateListDrawable();
		sd.addState(new int[] { android.R.attr.state_pressed }, pressDrawable);
		sd.addState(new int[] {}, nomalDrawable);
		return sd;
	}

	@Override
	public void onClick(View v) {
		if (v == mSpeechBtn) {
			onSpeechBtnClick();
			clickCallBack("showRecord");
		} else if (v == mFaceBtn) {
			onFaceBtnClick();
			clickCallBack("showEmotion");
		} else if (v == mExstraBtn) {
			onExtraBtnClick();
			clickCallBack("showExtras");
		} else if (v == mSendBtn && mSendBtn instanceof Button) {
			onSendBtnClick();
		}
	}

	private void onSendBtnClick() {
		openCallBack("send", 0);
		mEditText.setText("");
	}

	@SuppressWarnings("deprecation")
	private void onExtraBtnClick() {
		if (isViewVisible(mRecordBtn)) {
			mRecordBtn.setVisibility(View.GONE);
			mSpeechBtn.setBackgroundDrawable(mSpeechBtnDrawable);
			mEditText.setVisibility(View.VISIBLE);
		}
		if (!isViewVisible(mTableLayout)) {
			clickExtraBtnShowTable();
			setExtraPageNums();
		} else {
			clickExtraBtnChangeToExtra();
			setExtraPageNums();
		}
	}

	private boolean isViewVisible(View view) {
		if (view.getVisibility() == View.VISIBLE) {
			return true;
		}
		return false;
	}

	private void showExtraTable() {
		mExtraViewPager.setVisibility(View.VISIBLE);
		mFaceViewPager.setVisibility(View.GONE);
		chargeIndictorVisible("extrasPanel");
		int pageNums = mExtraViewPager.getOffscreenPageLimit();
		if (pageNums <= 1) {
			mIndictorView.setVisibility(View.GONE);
		}
		mIndictorView.setPointNums(pageNums);
		mIndictorView.setCurrentIndex(mExtraViewPager.getCurrentItem());
	}

	private void clickExtraBtnShowTable() {
		showExtraTable();
		isCallBack = false;
		hideInputMethod(mContext.getCurrentFocus());
		mDelayedHandler.postDelayed(mDelayedRunnable, 300);
	}

	@SuppressWarnings("deprecation")
	private void clickExtraBtnChangeToExtra() {
		showExtraTable();
		mFaceBtn.setBackgroundDrawable(mFaceBtnDrawable);
	}

	@SuppressWarnings("deprecation")
	private void onEditTextClick() {
		isKeyBoardVisible = true;
		mEditText.requestFocus();
		mFaceBtn.setBackgroundDrawable(mFaceBtnDrawable);
		mTableLayout.setVisibility(View.GONE);
	}

	private void onSpeechBtnClick() {
		if (!isViewVisible(mRecordBtn)) {
			showRecordBtn();
		} else {
			hideRecordBtn();
		}
	}

	@SuppressWarnings("deprecation")
	private void onFaceBtnClick() {
		if (isViewVisible(mRecordBtn)) {
			mRecordBtn.setVisibility(View.GONE);
			mSpeechBtn.setBackgroundDrawable(mSpeechBtnDrawable);
			mEditText.setVisibility(View.VISIBLE);
		}
		if (isKeyBoardVisible) {
			clickFaceBtnShowTable();
			setEmotionPageNums();
		} else {
			if (mTableLayout.getVisibility() == View.GONE) {
				clickFaceBtnShowTable();
				setEmotionPageNums();
			} else {
				if (isViewVisible(mExtraViewPager)) {
					clickFaceBtnChangeToFace();
					setEmotionPageNums();
				} else {
					clickFaceBtnHideTable();
				}
			}
		}
	}

	private boolean isCallBack = true;

	private void clickFaceBtnShowTable() {
		clickFaceBtnChangeToFace();
		hideInputMethod(mContext.getCurrentFocus());
		isCallBack = false;
		mDelayedHandler.postDelayed(mDelayedRunnable, 300);
	}

	@SuppressWarnings("deprecation")
	private void clickFaceBtnChangeToFace() {
		isKeyBoardVisible = false;
		mFaceBtn.setBackgroundDrawable(mKeyboardBtnDrawable);
		mExtraViewPager.setVisibility(View.GONE);
		mFaceViewPager.setVisibility(View.VISIBLE);
		chargeIndictorVisible("emotionPanel");
		int pageNums = mFaceViewPager.getOffscreenPageLimit();
		if (pageNums <= 1) {
			mIndictorView.setVisibility(View.GONE);
		}
		mIndictorView.setPointNums(pageNums);
		mIndictorView.setCurrentIndex(mFaceViewPager.getCurrentItem());
	}

	@SuppressWarnings("deprecation")
	private void clickFaceBtnHideTable() {
		mFaceBtn.setBackgroundDrawable(mFaceBtnDrawable);
		isKeyBoardVisible = true;
		mEditText.requestFocus();
		isCallBack = false;
		mTableLayout.setVisibility(View.GONE);
		mDelayedHandler.postDelayed(mDelayedShowKeyBoardRunnable, 300);
	}

	private Runnable mDelayedShowKeyBoardRunnable = new Runnable() {
		@Override
		public void run() {
			isCallBack = true;
			showKeybord();
		}
	};

	@SuppressWarnings("deprecation")
	private void showRecordBtn() {
		mSpeechBtn.setBackgroundDrawable(mSpeechKeyDrawable);
		mRecordBtn.setVisibility(View.VISIBLE);
		mEditText.setVisibility(View.GONE);
		mTableLayout.setVisibility(View.GONE);
		if (isBtnShow("extrasBtn")) {
			mSendBtn.setVisibility(View.GONE);
		}
		mFaceBtn.setBackgroundDrawable(mFaceBtnDrawable);
		hideInputMethod(mContext.getCurrentFocus());
	}

	@SuppressWarnings("deprecation")
	private void hideRecordBtn() {
		mSpeechBtn.setBackgroundDrawable(mSpeechBtnDrawable);
		mRecordBtn.setVisibility(View.GONE);
		mEditText.setVisibility(View.VISIBLE);
		mTableLayout.setVisibility(View.GONE);
		if (mEditText.getText().length() > 0) {
			mSendBtn.setVisibility(View.VISIBLE);
		}
		mEditText.requestFocus();
		showKeybord();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		mTempMsg = s;
	}

	@Override
	public void afterTextChanged(Editable s) {
		showOrHideSendBtn();
		valueChangeCallBack();
	}

	private void valueChangeCallBack() {
		if (mValueChangeCallBack != null) {
			JSONObject ret = new JSONObject();
			try {
				ret.put("value", getEditTextStr());
				mValueChangeCallBack.success(ret, false);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void showOrHideSendBtn() {
		int length = mTempMsg.length();
		if (!isOnlySendBtnExist) {
			if (length > 0 && mSendBtn.getVisibility() == View.GONE) {
				sendBtnShowWithAnimation();
			} else if (length == 0 && mSendBtn.getVisibility() == View.VISIBLE) {
				sendBtnHideWithAnimation();
			}
		}
	}

	private void sendBtnShowWithAnimation() {
		isShowAnimation = true;
		mSendBtn.setVisibility(View.VISIBLE);
		mSendBtn.startAnimation(mSendBtnShowAnimation);
	}

	private void sendBtnHideWithAnimation() {
		isShowAnimation = false;
		mSendBtn.startAnimation(mSendBtnHideAnimation);
	}

	@Override
	public void onAnimationStart(Animation animation) {

	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (!isShowAnimation) {
			mSendBtn.setVisibility(View.GONE);
		}
		mSendBtn.clearAnimation();
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int position) {
		mIndictorView.setCurrentIndex(position);
	}

	@Override
	public void keyClickedIndex(String index) {
		if ((mEmotionsPath + "/delete.png").equals(index)) {
			deleteEmotion();
		} else {
			insertEmotion(index);
		}
	}

	private void deleteEmotion() {
		KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0,
				0, KeyEvent.KEYCODE_ENDCALL);
		mEditText.dispatchKeyEvent(event);
	}

	private void insertEmotion(String index) {
		Bitmap bitmap = mJsParamsUtil.getBitmap(makeRealPath(index));
		ImageSpan imageSpan = new ImageSpan(mContext, bitmap);
		String text = mEmotionMap.get(index);
		if (text != null) {
			SpannableString spannableString = new SpannableString(text);
			spannableString.setSpan(imageSpan, 0, text.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			int cursorPosition = mEditText.getSelectionStart();
			mEditText.getText().insert(cursorPosition, spannableString);
		}
	}

	private SpannableString parseMsg(String msg) {
		Pattern p = Pattern.compile(".*?\\[(.*?)\\].*?");
		Matcher m = p.matcher(msg);
		List<String> emotionList = new ArrayList<String>();
		while (m.find()) {
			emotionList.add("[" + m.group(1) + "]");
		}
		SpannableString spannableString = new SpannableString(msg);
		for (int i = 0; i < emotionList.size(); i++) {
			String emotion = emotionList.get(i);
			int index = msg.indexOf(emotion);
			String source = mInsertEmotionMap.get(emotionList.get(i));
			Bitmap bitmap = mJsParamsUtil.getBitmap(makeRealPath(source));
			if (bitmap != null) {
				ImageSpan imageSpan = new ImageSpan(mContext, bitmap);
				spannableString.setSpan(imageSpan, index,
						index + emotion.length(),
						Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			}
		}
		return spannableString;
	}

	public String getEditTextStr() {
		return mEditText.getText().toString();
	}

	public void openCallBack(String eventType, int index) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("eventType", eventType);
			if (eventType.equals("clickExtras")) {
				ret.put("index", index);
			}
			if (eventType.equals("send")) {
				ret.put("msg", getEditTextStr());
			}
			mModuleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void clickCallBack(String eventType) {
		if (eventType.equals("showRecord")) {
			if (mShowRecordCallBack != null) {
				mShowRecordCallBack.success(null, false);
			}
		} else if (eventType.equals("showEmotion")) {
			if (mShowEmotionCallBack != null) {
				mShowEmotionCallBack.success(null, false);
			}
		} else if (eventType.equals("showExtras")) {
			if (mShowExtrasCallBack != null) {
				mShowExtrasCallBack.success(null, false);
			}
		}
	}

	private void setLayoutListener(View mParentLayout) {
		Rect r = new Rect();
		mParentLayout.getWindowVisibleDisplayFrame(r);
		int height = r.bottom;
		if (mLayoutListener == null) {
			mLayoutListener = new LayoutListener(mParentLayout, height);
			mParentLayout.getViewTreeObserver().addOnGlobalLayoutListener(
					mLayoutListener);
		}
	}

	private class LayoutListener implements
			ViewTreeObserver.OnGlobalLayoutListener {
		private View view;
		private boolean isTableVisible;
		private int inputBarHeight;
		private int height;

		public LayoutListener(View mParentLayout, int height) {
			this.view = mParentLayout;
			this.height = height;
			inputBarHeight = mEditText.getHeight();
		}

		@Override
		public void onGlobalLayout() {
			if (inputBarHeight != mEditText.getHeight()) {
				inputBarHeightCallBack();
			} else {
				if (height != initChatViewH(view)) {
					if (isTableVisible) {
						tableCallBack(isTableVisible);
						isTableVisible = isViewVisible(mTableLayout);
					} else {
						keyBoardCallBack(view);
						height = initChatViewH(view);
					}
				} else {
					tableCallBack(isTableVisible);
					isTableVisible = isViewVisible(mTableLayout);
				}
			}
			inputBarHeight = mEditText.getHeight();
		}
	}

	private void keyBoardCallBack(View view) {
		int height = initChatViewH(view);
		int inputBarHeight = UZCoreUtil.pixToDip(mEditText.getHeight());
		int pixPanelHeight = mJsParamsUtil.getScreenHeigth(mContext) - height;
		int dipPanelHeight = UZCoreUtil.pixToDip(pixPanelHeight);
		if (dipPanelHeight != 0) {
			mKeyboardHeight = dipPanelHeight;
		}
		inputFieldCallBack(mToggleKeyboardCallBack, inputBarHeight,
				dipPanelHeight);
	}

	private void tableCallBack(boolean isTableVisible) {
		boolean isVisible = isViewVisible(mTableLayout);
		int inputBarHeight = UZCoreUtil.pixToDip(mEditText.getHeight());
		if (isVisible != isTableVisible) {
			if (isVisible) {
				inputFieldCallBack(mToggleKeyboardCallBack, inputBarHeight, 220);
			} else {
				inputFieldCallBack(mToggleKeyboardCallBack, inputBarHeight, 0);
			}
		}
	}

	private int initChatViewH(View view) {
		Rect r = new Rect();
		view.getWindowVisibleDisplayFrame(r);
		return r.bottom;
	}

	public void inputFieldCallBack(UZModuleContext moduleContext,
			int inputFieldH, int chatViewH) {
		if (isCallBack) {
			JSONObject result = new JSONObject();
			try {
				result.put("inputBarHeight", inputFieldH);
				result.put("panelHeight", chatViewH);
				if (moduleContext != null) {
					moduleContext.success(result, false);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void inputBarHeightCallBack() {
		boolean isVisible = isViewVisible(mTableLayout);
		int inputBarHeight = UZCoreUtil.pixToDip(mEditText.getHeight());
		if (isVisible) {
			inputFieldCallBack(mChangeCallBack, inputBarHeight, 220);
		} else {
			if (isKeyBoardVisible) {
				inputFieldCallBack(mChangeCallBack, inputBarHeight,
						mKeyboardHeight);
			} else {
				inputFieldCallBack(mChangeCallBack, inputBarHeight, 0);
			}
		}
	}

	private void valueCallBack(UZModuleContext moduleContext, String msg) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("status", true);
			if (msg != null) {
				ret.put("msg", msg);
			}
			moduleContext.success(ret, false);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void initSendBtnStyles(UZModuleContext moduleContext) {
		ViewBackground btnNormalBg = new ViewBackground();
		ViewBackground btnHighlightBg = new ViewBackground();
		String btnNormalBgStr = null;
		btnNormalBgStr = mJsParamsUtil.sendBtnBg(moduleContext);
		btnNormalBg = getViewBackground(btnNormalBgStr);
		String btnHighlightBgStr = null;
		btnHighlightBgStr = mJsParamsUtil.sendBtnHighlightBg(moduleContext);
		if (btnHighlightBgStr.equals("")) {
			btnHighlightBgStr = btnNormalBgStr;
		}
		btnHighlightBg = getViewBackground(btnHighlightBgStr);

		int btnNoramlTitleColor = mJsParamsUtil
				.sendBtnTitleColor(moduleContext);
		mSendBtn = initBtn(Constans.SEND_BTN_SIZE, Constans.BTN_SIZE,
				mJsParamsUtil.sendBtnTitle(moduleContext),
				mJsParamsUtil.sendBtnTitleSize(moduleContext),
				btnNoramlTitleColor, btnNormalBg, btnHighlightBg, 10);
		mSendBtn.setPadding(0, 0, 0, 0);
		addClickListener((CustomButton) mSendBtn,
				mJsParamsUtil.sendBtnTitle(moduleContext),
				mJsParamsUtil.sendBtnHighlightTitleColor(moduleContext),
				mJsParamsUtil.sendBtnTitle(moduleContext), btnNoramlTitleColor,
				Constans.SEND_BTN_SIZE, Constans.BTN_SIZE, moduleContext);
	}

	@SuppressWarnings("deprecation")
	private void initSendBtnDefaultStyles() {
		int normalId = UZResourcesIDFinder
				.getResDrawableID(Constans.SEND_BTN_NORMAL_ID);
		int activeId = UZResourcesIDFinder
				.getResDrawableID(Constans.SEND_BTN_ACTIVE_ID);
		BitmapDrawable normal = createDrawable(normalId);
		BitmapDrawable active = createDrawable(activeId);
		StateListDrawable sendDrawable = createStateDrawable(normal, active);
		mSendBtn.setBackgroundDrawable(sendDrawable);
	}

	private ViewBackground getViewBackground(String bgStr) {
		ViewBackground viewBackground = new ViewBackground();
		Bitmap bgBitmap = null;
		bgBitmap = mJsParamsUtil.getBitmap(makeRealPath(bgStr));
		if (bgBitmap == null) {
			int color;
			color = UZUtility.parseCssColor(bgStr);
			viewBackground.setBgColor(color);
			viewBackground.setBgType(BackgroundType.COLOR);
		} else {
			viewBackground.setBgBitmap(bgBitmap);
			viewBackground.setBgType(BackgroundType.IMG);
		}
		return viewBackground;
	}

	private CustomButton initBtn(int w, int h, String btnNoramlTitle,
			int btnTitleSize, int btnNoramlTitleColor,
			ViewBackground btnNormalBg, ViewBackground btnHighlightBg,
			int corner) {
		CustomButton btn = new CustomButton(mContext);
		btn.setText(btnNoramlTitle);
		btn.setTextSize(btnTitleSize);
		btn.setTextColor(btnNoramlTitleColor);
		btn.setBackgroundColor(Color.TRANSPARENT);
		btn.init(btnNormalBg, btnHighlightBg, w, h, corner);
		return btn;
	}

	@SuppressLint("ClickableViewAccessibility")
	private void addClickListener(final CustomButton btn,
			final String btnHighLightTitle, final int btnHighLightTitleColor,
			final String btnNoramlTitle, final int btnNoramlTitleColor,
			final int w, final int h, final UZModuleContext moduleContext) {
		btn.setmNoramlTitle(btnNoramlTitle);
		btn.setmNoramlTitleColor(btnNoramlTitleColor);
		btn.setmHighLightTitle(btnHighLightTitle);
		btn.setmHighLightTitleColor(btnHighLightTitleColor);
		btn.setOnTouchListener(new OnTouchListener() {

			boolean isMoveOut = false;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					isMoveOut = false;
					onBtnClick(btn, true, btnHighLightTitle,
							btnNoramlTitleColor);
					break;
				case MotionEvent.ACTION_MOVE:
					if (!isInRange(event, w, h)) {
						isMoveOut = true;
						onBtnClick(btn, false, btnNoramlTitle,
								btnNoramlTitleColor);
					}
					break;
				case MotionEvent.ACTION_UP:
					onBtnClick(btn, false, btnNoramlTitle, btnNoramlTitleColor);
					if (!isMoveOut) {
						onSendBtnClick();
					}
					break;
				}
				return true;
			}
		});
	}

	private void onBtnClick(CustomButton btn, boolean isPressed, String text,
			int textColor) {
		btn.setPressed(isPressed);
		btn.setText(text);
		btn.setTextColor(textColor);
		btn.invalidate();
	}

	private boolean isInRange(MotionEvent event, int w, int h) {
		float x = event.getX();
		float y = event.getY();
		if (x < 0 || y < 0 || x > UZUtility.dipToPix(w)
				|| y > UZUtility.dipToPix(h)) {
			return false;
		}
		return true;
	}
}
