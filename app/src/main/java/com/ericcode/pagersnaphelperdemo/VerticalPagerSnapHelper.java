package com.ericcode.pagersnaphelperdemo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.OverScroller;

import java.util.Arrays;

/**
 * 实现分页效果的SnapHelper
 * 目前只实现了垂直方向，类似于抖音的效果，但是
 */
public class VerticalPagerSnapHelper extends PagerSnapHelper {
    private static final String TAG = "MyPagerSnapHelper";
    @Nullable
    private OrientationHelper mVerticalHelper;
    @Nullable
    private OrientationHelper mHorizontalHelper;
    private RecyclerView mRecyclerView;
    private int lastPosition = 0;

    /**
     * 计算需要滚动的距离
     *
     * @param layoutManager
     * @param targetView
     * @return
     */
    @Nullable
    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        int[] out = new int[2];
        if (layoutManager.canScrollHorizontally()) {
            out[0] = this.distanceToTop(layoutManager, targetView, this.getHorizontalHelper(layoutManager));
        } else {
            out[0] = 0;
        }

        if (layoutManager.canScrollVertically()) {
            out[1] = this.distanceToTop(layoutManager, targetView, this.getVerticalHelper(layoutManager));
        } else {
            out[1] = 0;
        }

        Log.i(TAG, "calculateDistanceToFinalSnap:" + Arrays.toString(out));
        return out;
    }

    /**
     * 计算到需要滑动距离的实现
     *
     * @param layoutManager
     * @param targetView
     * @param helper
     * @return
     */
    private int distanceToTop(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView, OrientationHelper helper) {
        Log.d(TAG, "targetView MeasuredHeight:" + targetView.getMeasuredHeight());

        int containerSize;
        if (layoutManager.getClipToPadding()) {
            containerSize = helper.getStartAfterPadding() + helper.getTotalSpace();
        } else {
            containerSize = helper.getEnd();
        }
        Log.d(TAG, "containerSize:" + containerSize);

        int decoratedStart = helper.getDecoratedStart(targetView);
        Log.d(TAG, "decoratedStart:" + decoratedStart);

        int range = containerSize - targetView.getMeasuredHeight();
        Log.d(TAG, "rang:" + range);

        int position = layoutManager.getPosition(targetView);

        // 没有划出本item view，则返回0，这样RecyclerView就会停在滑动的位置
        if (decoratedStart >= range && decoratedStart <= 0) {
            Log.i(TAG, "停留");
            decoratedStart = 0;
        } else if (decoratedStart < range && lastPosition == position) {
            Log.i(TAG, "底部还原");
            decoratedStart = decoratedStart - range; // 底部还原
        }

        if (lastPosition != position) {
            if (lastPosition > position) {
                Log.i(TAG, "向上滑");
            } else {
                Log.i(TAG, "向下滑");
            }
            lastPosition = position;
        }

        return decoratedStart;
    }

    @NonNull
    private OrientationHelper getVerticalHelper(@NonNull RecyclerView.LayoutManager layoutManager) {
        if (this.mVerticalHelper == null || this.mVerticalHelper.getLayoutManager() != layoutManager) {
            this.mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }

        return this.mVerticalHelper;
    }

    @NonNull
    private OrientationHelper getHorizontalHelper(@NonNull RecyclerView.LayoutManager layoutManager) {
        if (this.mHorizontalHelper == null || this.mHorizontalHelper.getLayoutManager() != layoutManager) {
            this.mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }

        return this.mHorizontalHelper;
    }

    @Override
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) throws IllegalStateException {
        super.attachToRecyclerView(recyclerView);
        this.mRecyclerView = recyclerView;
    }

    @Override
    public boolean onFling(int velocityX, int velocityY) {
        RecyclerView.LayoutManager layoutManager = this.mRecyclerView.getLayoutManager();
        if (layoutManager == null) {
            return false;
        } else {
            RecyclerView.Adapter adapter = this.mRecyclerView.getAdapter();
            if (adapter == null) {
                return false;
            } else {
                int minFlingVelocity = this.mRecyclerView.getMinFlingVelocity();
                return (Math.abs(velocityY) > minFlingVelocity || Math.abs(velocityX) > minFlingVelocity) && this.snapFromFling(layoutManager, velocityX, velocityY);
            }
        }
    }

    /**
     * 重写此方法，防止一次fling滑动多页
     * 现在的实现是：每次fling最多滑动到本item的顶部或者底部，如果已经在顶部或者底部了，可以换到其他页。
     *
     * @param layoutManager
     * @param velocityX
     * @param velocityY
     * @return
     */
    private boolean snapFromFling(@NonNull RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
            return false;
        } else {
            RecyclerView.SmoothScroller smoothScroller = this.createScroller(layoutManager);
            if (smoothScroller == null) {
                return false;
            } else {
                int targetPosition = this.findTargetSnapPosition(layoutManager, velocityX, velocityY);
                Log.i(TAG, "targetPosition:" + targetPosition);
                View startView = findStartView(layoutManager, this.getVerticalHelper(layoutManager));
                Log.i(TAG, "startView:" + startView);
                if (startView != null) {
                    int centerPosition = layoutManager.getPosition(startView);
                    Log.i(TAG, "centerPosition:" + centerPosition);
                    if (centerPosition != -1) {
                        int top = startView.getTop();
                        int bottom = startView.getBottom();

                        boolean forwardDirection;
                        if (layoutManager.canScrollHorizontally()) {
                            forwardDirection = velocityX > 0;
                        } else {
                            forwardDirection = velocityY > 0;
                        }
                        Log.i(TAG, "forwardDirection:" + forwardDirection);
                        int containerHeight = mRecyclerView.getMeasuredHeight();

                        if (forwardDirection) {
                            if (bottom > containerHeight) {
                                Log.i(TAG, "滑动到底部");

                                OverScroller overScroller = new OverScroller(mRecyclerView.getContext());
                                overScroller.fling(0, 0, velocityX, velocityY, Integer.MIN_VALUE,
                                        Integer.MAX_VALUE, -containerHeight, containerHeight);

                                int finalX = overScroller.getFinalX();
                                int finalY = overScroller.getFinalY();

                                Log.i(TAG, "finalX:" + finalX);
                                Log.i(TAG, "finalY:" + finalY);
//                                Log.i(TAG, "getSplineDeceleration:" + getSplineDeceleration(velocityY));
                                targetPosition = centerPosition;
                                int scrollByY = bottom - containerHeight;
                                if (scrollByY >= finalY) {
                                    scrollByY = finalY;
                                }
                                mRecyclerView.smoothScrollBy(0, scrollByY);
                            } else {
                                Log.i(TAG, "滑动到下一条");
                            }
                        } else {
                            if (top < 0) {
                                Log.i(TAG, "滑动到顶部");
                                targetPosition = centerPosition;
                                OverScroller overScroller = new OverScroller(mRecyclerView.getContext());
                                overScroller.fling(0, 0, velocityX, velocityY, Integer.MIN_VALUE,
                                        Integer.MAX_VALUE, -containerHeight, containerHeight);

                                int finalX = overScroller.getFinalX();
                                int finalY = overScroller.getFinalY();

                                Log.i(TAG, "finalX:" + finalX);
                                Log.i(TAG, "finalY:" + finalY);
//                                Log.i(TAG, "getSplineDeceleration:" + getSplineDeceleration(velocityY));
                                int scrollByY = top;
                                if (scrollByY <= finalY) {
                                    scrollByY = finalY;
                                }

                                mRecyclerView.smoothScrollBy(0, scrollByY);
                            } else {
                                Log.i(TAG, "滑动到上一条");
                            }
                        }

                        Log.i(TAG, "top:" + top);
                        Log.i(TAG, "bottom:" + bottom);
                    }
                }

                if (targetPosition == -1) {
                    return false;
                } else {
                    smoothScroller.setTargetPosition(targetPosition);
                    layoutManager.startSmoothScroll(smoothScroller);
                    return true;
                }
            }
        }
    }

    @Nullable
    private View findStartView(RecyclerView.LayoutManager layoutManager, OrientationHelper helper) {
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return null;
        } else {
            View closestChild = null;
            int startest = 2147483647;

            for (int i = 0; i < childCount; ++i) {
                View child = layoutManager.getChildAt(i);
                int childStart = helper.getDecoratedStart(child);
                if (childStart < startest) {
                    startest = childStart;
                    closestChild = child;
                }
            }

            return closestChild;
        }
    }

    static class Log {
        static final boolean DEBUG = true;

        static void i(String tag, String msg) {
            if (DEBUG) {
                android.util.Log.i(tag, msg);
            }
        }

        static void d(String tag, String msg) {
            if (DEBUG) {
                android.util.Log.d(tag, msg);
            }
        }
    }

}
