package com.blues.smallestwidthapp.ui.home;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.customview.widget.ViewDragHelper;

public class ViewDragLayout extends ConstraintLayout {
    private static final String TAG = "MyWidget";
    private ViewDragHelper mDragger;
    private View child;

    public ViewDragLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public ViewDragLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewDragLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ViewDragLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public synchronized void setChild(View view) {
        this.child = view;
    }

    private void init() {
        mDragger = ViewDragHelper.create(this, new DragCallback());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isIntercept(event, child)) {
            return super.onInterceptTouchEvent(event);
        }
        return mDragger.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isIntercept(event, child)) {
            return super.onTouchEvent(event);
        }
        try {
            mDragger.processTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean isIntercept(MotionEvent event, View child) {
        if ((event == null) || (child == null)) {
            return false;
        }
        float lowX = child.getX();
        float aboveX = lowX + child.getWidth();
        float lowY = child.getY();
        float aboveY = lowY + child.getHeight();
        Log.d(TAG, "isIntercept = " + child.getWidth() + " - " + lowX + " - "
                + aboveX + " - " + child.getHeight() + " - " + lowY + " - " + aboveY);
        return ((event.getX() > lowX) && (event.getX() < aboveX) && (event.getY() > lowY) && (event.getY() < aboveY));
    }

    private class DragCallback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            // 返回true表示该view可被拖动
            return true;
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            // 限制水平位置不超出边界
            final int width = getWidth();
            final int maxLeft = Math.max(0, width - child.getWidth());

            if (left < 0) {
                return 0;
            } else if (left > maxLeft) {
                return maxLeft;
            } else {
                return left;
            }
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            // 限制垂直位置不超出边界
            final int height = getHeight();
            final int maxTop = Math.max(0, height - child.getHeight());

            if (top < 0) {
                return 0;
            } else if (top > maxTop) {
                return maxTop;
            } else {
                return top;
            }
        }
    }
}