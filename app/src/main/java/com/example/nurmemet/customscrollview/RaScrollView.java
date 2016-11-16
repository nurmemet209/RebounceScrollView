package com.example.nurmemet.customscrollview;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

/**
 * Created by nurmemet on 2016/11/15.
 */

public class RaScrollView extends ScrollView {

    private float mInitionalY;
    private boolean mDragging = false;
    private int mActivePointerId = -1;
    private ViewGroup mContainer;
    private static final float DRAG_RATE = .6f;
    private ScrollerCompat mScroller;
    private static final int VALIDE_DELTA = 120;
    private OnScrollEndListener mOnScrollEndListener;
    private int mDirection=0;
    public void setOnScrollEndListener(OnScrollEndListener onScrollEndListener){
        this.mOnScrollEndListener=onScrollEndListener;
    }

    public interface OnScrollEndListener{
        void onScrollEnd(int direction);
    }

    public RaScrollView(Context context) {
        super(context);
    }

    public RaScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    private void init() {
        mScroller = ScrollerCompat.create(getContext());
    }

    public RaScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mContainer = (ViewGroup) getChildAt(0);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        int action = MotionEventCompat.getActionMasked(ev);

        if (getMeasuredHeight() <= getHeight()) {
            if (canScrollVertically(-1) && canScrollVertically(1)) {
                return super.onTouchEvent(ev);
            }
        }
        System.out.println("test");
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:

//                if (mActivePointerId == -1) {
//                    return super.onTouchEvent(ev);
//                }
                final float y = ev.getY();
                if (!mDragging) {
                    mInitionalY = y;
                    mDragging = true;
                    break;
                }
                final float overscroll = (y - mInitionalY) * DRAG_RATE;
                if (overscroll<0&&canScrollVertically(1)){
                    mDragging=false;
                    return super.onTouchEvent(ev);
                }
                else if (overscroll>0&&canScrollVertically(-1)){
                    mDragging=false;
                    return super.onTouchEvent(ev);
                }
                mDirection=overscroll>0?1:-1;
                ViewCompat.offsetTopAndBottom(mContainer, (int) overscroll);
                mInitionalY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = -1;
                mDragging = false;
                final float top = mContainer.getTop();
                mScroller.abortAnimation();
                mScroller.startScroll(0, (int) top, 0, (int) -top);
                postOnAnimation(new Runnable() {
                    @Override
                    public void run() {
                        if (mScroller.computeScrollOffset()) {
                            final float y = mScroller.getCurrY();
                            final float top = mContainer.getTop();
                            ViewCompat.offsetTopAndBottom(mContainer, (int) (y - top));
                            postOnAnimation(this);
                        } else {
                            if (VALIDE_DELTA <= Math.abs(top)) {
                                onAnimEnd(mDirection);
                            }
                        }
                    }
                });
                break;
            case MotionEvent.ACTION_POINTER_UP:
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    mActivePointerId = -1;
                    return false;
                }
                break;
            default:
                break;
        }

        return true;
    }

    public void onAnimEnd(int direction) {
        if (mOnScrollEndListener!=null){
            mOnScrollEndListener.onScrollEnd(mDirection);
        }
    }


}