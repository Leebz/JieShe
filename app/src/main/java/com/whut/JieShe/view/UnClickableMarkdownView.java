package com.whut.JieShe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.mukesh.MarkdownView;

/**
 * 在MarkdownView的基础上，去掉点击事件和长按事件，使得这些事件可以继续向下分发
 * Created by sukai on 2017/04/21.
 */

public class UnClickableMarkdownView extends MarkdownView {
    public UnClickableMarkdownView(Context context) {
        super(context);
        init();
    }

    public UnClickableMarkdownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UnClickableMarkdownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return false;
    }

    private void init(){
        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });
    }
}
