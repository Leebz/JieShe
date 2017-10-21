package com.whut.JieShe.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by sukai on 2017/04/19.
 */

public class NoScrollableViewPager extends ViewPager{

    private boolean isPagingEnabled = true;

    public NoScrollableViewPager(Context context) {
        super(context);
    }

    public NoScrollableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        float x = event.getX();
//        if (x<100||x>900){
//            return super.onTouchEvent(event);
//        }
//        return false;

        return isPagingEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
//        float x = event.getX();
//        if (x<100||x>900){
//            return super.onInterceptTouchEvent(event);
//        }
//        return false;
        return isPagingEnabled && super.onInterceptTouchEvent(event);
    }

}
