package com.yinge.opengl.camera.image.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.v4.view.GravityCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.yinge.opengl.camera.R;


public class BubbleSeekBar extends SeekBar {
	private Drawable mThumbDrawable;
	private BubbleIndicator mBubbleIndicator;
	private boolean mIsListenerSet = false;
	private OnBubbleSeekBarChangeListener mOnBubbleSeekBarChangeListener;
	private int start = 0;
	private int	end= 100;
	
	public BubbleSeekBar(Context context) {
		this(context, null);
	}

	public BubbleSeekBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BubbleSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mBubbleIndicator = new BubbleIndicator(context);
		setOnSeekBarChangeListener(mOnSeekBarChangeListener);
	}
	
    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
        mThumbDrawable = thumb;
    }
	
    public void setRange(int start, int end){
    	this.start = start;
    	this.end = end;
    }
    
    public void setMax(){
    	super.setMax(100);
    }
    
	private OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			mBubbleIndicator.hideIndicator();
			if(mOnBubbleSeekBarChangeListener != null)
				mOnBubbleSeekBarChangeListener.onStopTrackingTouch(seekBar);
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			mBubbleIndicator.showIndicator(seekBar, mThumbDrawable.getBounds());
			if(mOnBubbleSeekBarChangeListener != null)
				mOnBubbleSeekBarChangeListener.onStartTrackingTouch(seekBar);
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
			if(fromUser)
				mBubbleIndicator.moveIndicator(mThumbDrawable.getBounds(), start + (int) ((end - start) * progress / 100.0f));
			if(mOnBubbleSeekBarChangeListener != null)
				mOnBubbleSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
		}
	};
	
	/**
     * Use OnBubbleSeekBarChangeListener instead of OnSeekBarChangeListener
     * 
     * @param l The seek bar notification listener
     * 
     * @see BubbleSeekBar.OnBubbleSeekBarChangeListener
     */
	public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
		if(mIsListenerSet)
			Log.e("BubbleSeekBar","Use OnBubbleSeekBarChangeListener instead of OnSeekBarChangeListener!!!!!");
		else
			super.setOnSeekBarChangeListener(l);
        mIsListenerSet = true;
    }

	public void setOnBubbleSeekBarChangeListener(OnBubbleSeekBarChangeListener l){
		this.mOnBubbleSeekBarChangeListener = l;
	}

	public interface OnBubbleSeekBarChangeListener{
		void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);

        void onStartTrackingTouch(SeekBar seekBar);

        void onStopTrackingTouch(SeekBar seekBar);
	}

	public class BubbleIndicator {

		private final WindowManager mWindowManager;
	    private boolean mShowing;

	    private int[] mDrawingLocation = new int[2];
	    Point screenSize = new Point();
		private Floater mPopupView;

		public BubbleIndicator(Context context) {
	        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	        mPopupView = new Floater(context);

	        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
	        screenSize.set(displayMetrics.widthPixels, displayMetrics.heightPixels);
	    }

		public void showIndicator(View parent, Rect touchBounds) {
	        if (isShowing()) {
	            return;
	        }

	        IBinder windowToken = parent.getWindowToken();
	        if (windowToken != null) {
	            WindowManager.LayoutParams p = createPopupLayout(windowToken);

	            p.gravity = Gravity.TOP | GravityCompat.START;
	            updateLayoutParamsForPosiion(parent, p);
	            mShowing = true;

	            translateViewIntoPosition(touchBounds.centerX());
	            invokePopup(p);
	        }
	    }

		private WindowManager.LayoutParams createPopupLayout(IBinder windowToken) {
	        WindowManager.LayoutParams p = new WindowManager.LayoutParams();
	        p.gravity = Gravity.START | Gravity.TOP;
	        p.width = ViewGroup.LayoutParams.MATCH_PARENT;
	        p.height = ViewGroup.LayoutParams.MATCH_PARENT;
	        p.format = PixelFormat.TRANSLUCENT;
	        p.flags = computeFlags(p.flags);
	        p.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
	        p.token = windowToken;
	        p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
	        return p;
	    }

		private void invokePopup(WindowManager.LayoutParams p) {
	        mWindowManager.addView(mPopupView, p);
	    }

		public void moveIndicator(Rect touchBounds, int progress) {
			if (!isShowing()) {
	            return;
	        }
			translateViewIntoPosition(touchBounds.centerX());
			mPopupView.setProgressText(progress);
		}

		public void hideIndicator(){
			if (!isShowing()) {
	            return;
	        }
			mShowing = false;
			mWindowManager.removeView(mPopupView);
		}

		private void translateViewIntoPosition(final int x) {
	        mPopupView.setFloatOffset(x + mDrawingLocation[0]);
	    }

		private void updateLayoutParamsForPosiion(View anchor, WindowManager.LayoutParams p) {
	        measureFloater();
	        int measuredHeight = mPopupView.getMeasuredHeight();
	        anchor.getLocationInWindow(mDrawingLocation);
	        p.x = 0;
	        p.y = mDrawingLocation[1] - measuredHeight;
	        p.width = screenSize.x;
	        p.height = measuredHeight;
	    }

		private void measureFloater() {
	        int specWidth = View.MeasureSpec.makeMeasureSpec(screenSize.x, View.MeasureSpec.EXACTLY);
	        int specHeight = View.MeasureSpec.makeMeasureSpec(screenSize.y, View.MeasureSpec.AT_MOST);
	        mPopupView.measure(specWidth, specHeight);
	    }
		
		private boolean isShowing() {
			return mShowing;
		}
		
		private int computeFlags(int curFlags) {
	        curFlags &= ~(
	                WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES |
	                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
	                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
	                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
	                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
	                        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
	        curFlags |= WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES;
	        curFlags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
	        curFlags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
	        curFlags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
	        return curFlags;
	    }
		
	}
	private class Floater extends FrameLayout {
        public TextView mMarker;
        private int mOffset;

        public Floater(Context context) {
            super(context);
            mMarker = new TextView(context);
            mMarker.setText("0%");
            mMarker.setGravity(Gravity.CENTER);
            mMarker.setBackgroundResource(R.drawable.tooltip_bg);
            addView(mMarker, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            measureChildren(widthMeasureSpec, heightMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSie = mMarker.getMeasuredHeight();
            setMeasuredDimension(widthSize, heightSie);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            int centerDiffX = (mMarker.getMeasuredWidth() - mMarker.getPaddingLeft()) / 2;
            int offset = mOffset - centerDiffX;
            mMarker.layout(offset, 0, offset + mMarker.getMeasuredWidth(), mMarker.getMeasuredHeight());
        }

        public void setFloatOffset(int x) {
            mOffset = x;
            int centerDiffX = (mMarker.getMeasuredWidth() - mMarker.getPaddingLeft()) / 2;
            int offset = mOffset - centerDiffX;
            mMarker.offsetLeftAndRight(offset - mMarker.getLeft());
        }
        
        public void setProgressText(int progress){
        	mMarker.setText(""+progress+"%");
        }
    }
}
