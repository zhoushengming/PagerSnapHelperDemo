package com.ericcode.pagersnaphelperdemo;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * 当调用RecyclerView的smoothScrollToPosition()方法时，
 * 默认会滑动到该position的底部。当该position的高度超出一屏时，
 * 就会让该position的底部与RecyclerView的底部对齐，本类实现了
 * 让该position的顶部与RecyclerView的顶部对齐。
 */
public class TopPositionLayoutManager extends LinearLayoutManager {
    public TopPositionLayoutManager(Context context) {
        super(context);
    }

    public TopPositionLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public TopPositionLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 重写本方法， RecyclerView#smoothScrollToPosition(int) 也是调用的本方法
     * 默认是使用了LinearSmoothScroller进行滑动
     *
     * @param recyclerView
     * @param state
     * @param position
     * @see RecyclerView#smoothScrollToPosition(int)
     */
    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        RecyclerView.SmoothScroller smoothScroller = new TopSmoothScroller(recyclerView.getContext());
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    /**
     * 实现RecyclerView顶部与item的顶部对齐
     *
     * @see RecyclerView#smoothScrollToPosition(int)
     */
    private static class TopSmoothScroller extends LinearSmoothScroller {

        TopSmoothScroller(Context context) {
            super(context);
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            int top = boxStart - viewStart;
            return top;
        }
    }

    /**
     * 实现RecyclerView中部与item的中部对齐
     *
     * @see RecyclerView#smoothScrollToPosition(int)
     */
    private static class CenterSmoothScroller extends LinearSmoothScroller {

        CenterSmoothScroller(Context context) {
            super(context);
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            int center = (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
            return center;
        }
    }
}
