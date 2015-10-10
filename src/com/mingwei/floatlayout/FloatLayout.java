package com.mingwei.floatlayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.OverScroller;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

/**
 * 自定义的有悬浮layout的容器，类似微博，美团，点评的效果
 * 
 * @author mingwei
 * 
 */
public class FloatLayout extends LinearLayout {

	private RelativeLayout mHeaderLayout;
	private LinearLayout mFloatLayout;
	private ViewPager mContent;

	private int mHeaderHeight;
	private boolean isHeaderHidden;
	private ViewGroup mInnerScrollview;

	private OverScroller mScroller;
	private VelocityTracker mVelocityTracker;
	private int mTouchSlop;
	private int mMaximumVelocity, mMinimumVelocity;

	private float mLastY;
	private boolean isDragging;
	private boolean isMove = false;

	public FloatLayout(Context context) {
		this(context, null);
	}

	public FloatLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FloatLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mScroller = new OverScroller(context);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mMaximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
		mMinimumVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mHeaderLayout = (RelativeLayout) findViewById(R.id.float_layout_top);
		mFloatLayout = (LinearLayout) findViewById(R.id.float_layout_float);
		mContent = (ViewPager) findViewById(R.id.float_layout_content);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		ViewGroup.LayoutParams layoutParams = mContent.getLayoutParams();
		layoutParams.height = getMeasuredHeight() - mFloatLayout.getMeasuredHeight();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mHeaderHeight = mHeaderLayout.getMeasuredHeight();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		float y = ev.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			float moveY = y - mLastY;
			getCurrentScrollView();
			if (mInnerScrollview instanceof ScrollView) {
				if (mInnerScrollview.getScrollY() == 0 && isHeaderHidden && moveY > 0 && !isMove) {
					isMove = true;
					return dispatchInnerChild(ev);
				}
			} else if (mInnerScrollview instanceof ListView) {
				ListView listView = (ListView) mInnerScrollview;
				View viewItem = listView.getChildAt(listView.getFirstVisiblePosition());
				if (viewItem != null && viewItem.getTop() == 0 && isHeaderHidden && moveY > 0 && !isMove) {
					isMove = true;
					return dispatchInnerChild(ev);
				}
			}
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	private boolean dispatchInnerChild(MotionEvent ev) {
		ev.setAction(MotionEvent.ACTION_CANCEL);
		MotionEvent newMotionEvent = MotionEvent.obtain(ev);
		dispatchTouchEvent(ev);
		newMotionEvent.setAction(MotionEvent.ACTION_DOWN);
		return dispatchTouchEvent(newMotionEvent);
	}

	/**
	 * 事件拦截，来处理什么时候应该滑动那个部分的容器
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		float y = ev.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			float moveY = y - mLastY;
			getCurrentScrollView();
			if (Math.abs(moveY) > mTouchSlop) {
				isDragging = true;
				if (mInnerScrollview instanceof ScrollView) {
					if (!isHeaderHidden || (mInnerScrollview.getScrollY() == 0 && isHeaderHidden && moveY > 0)) {
						initVelocityTracker();
						mVelocityTracker.addMovement(ev);
						mLastY = y;
						return true;
					}
				} else if (mInnerScrollview instanceof ListView) {
					ListView listView = (ListView) mInnerScrollview;
					View viewItem = listView.getChildAt(listView.getFirstVisiblePosition());
					if (!isHeaderHidden || (viewItem != null && viewItem.getTop() == 0 && moveY > 0)) {
						initVelocityTracker();
						mVelocityTracker.addMovement(ev);
						mLastY = y;
						return true;
					}
				}
			}

		case MotionEvent.ACTION_CANCEL:

		case MotionEvent.ACTION_UP:
			isDragging = false;
			recycleVelocityTracker();
			break;
		default:
			break;
		}
		return super.onInterceptTouchEvent(ev);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		initVelocityTracker();
		mVelocityTracker.addMovement(event);
		int action = event.getAction();
		float y = event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mLastY = y;
			return true;
		case MotionEvent.ACTION_MOVE:
			float moveY = y - mLastY;
			if (!isDragging && Math.abs(moveY) > mTouchSlop) {
				isDragging = true;
			}
			if (isDragging) {
				scrollBy(0, (int) -moveY);
			}
			mLastY = y;
			break;
		case MotionEvent.ACTION_CANCEL:
			isDragging = false;
			recycleVelocityTracker();
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			break;
		case MotionEvent.ACTION_UP:
			isDragging = false;
			mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
			int velocity = (int) mVelocityTracker.getYVelocity();
			if (Math.abs(velocity) > mMinimumVelocity) {
				fling(-velocity);
			}
			recycleVelocityTracker();
			break;

		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 重写scrollTo,用来控制在滚动的过程中不至于 超出范围.
	 * 
	 * y<0,当Header完全显示在父容器时就不再允许Header能继续滑动.
	 * 
	 * y>mHeaderHeight,当Header部分完全画出父控件时，y能到达的最大值就是就是Header的高度.
	 * 
	 * y!=getScrollY(),调用父类的scrollTo,当y发生变化时，调用父类scrollTo滚动.
	 */
	@Override
	public void scrollTo(int x, int y) {
		y = (y < 0) ? 0 : y;
		y = (y > mHeaderHeight) ? mHeaderHeight : y;
		if (y != getScrollY()) {
			super.scrollTo(x, y);
		}
		isHeaderHidden = getScrollY() == mHeaderHeight;
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(0, mScroller.getCurrY());
			invalidate();
		}
	}

	/**
	 * 容器滚动时松开手指后根据velocity自动滚到到指定位置
	 * 
	 * @param velocityY
	 *            松开时的速度，OverScroll类帮助我们计算要滑多远
	 */
	public void fling(int velocityY) {
		mScroller.fling(0, getScrollY(), 0, velocityY, 0, 0, 0, mHeaderHeight);
		invalidate();
	}

	/**
	 * 根据当前的View来处理事件分发,例如容器当中是ScrollView，或者ListView时
	 */
	private void getCurrentScrollView() {
		int cuttentItem = mContent.getCurrentItem();
		PagerAdapter pagerAdapter = mContent.getAdapter();
		if (pagerAdapter instanceof FragmentPagerAdapter) {
			FragmentPagerAdapter adapter = (FragmentPagerAdapter) pagerAdapter;
			Fragment fragment = adapter.getItem(cuttentItem);
			mInnerScrollview = (ViewGroup) fragment.getView().findViewById(R.id.float_layout_inner_view);
		} else if (pagerAdapter instanceof FragmentStatePagerAdapter) {
			FragmentStatePagerAdapter adapter = (FragmentStatePagerAdapter) pagerAdapter;
			Fragment fragment = adapter.getItem(cuttentItem);
			mInnerScrollview = (ViewGroup) fragment.getView().findViewById(R.id.float_layout_inner_view);
		}
	}

	/**
	 * 初始化VelocityTracker
	 */
	private void initVelocityTracker() {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
	}

	/**
	 * 回收VelocityTracker
	 */
	private void recycleVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

}
