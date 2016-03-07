package com.mwang.slideselectlistview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.CheckBox;
import android.widget.FrameLayout;

/**
 * a scalable checkbox
 */
public class ScalableCheckBox extends FrameLayout {

    private Context mContext;
    private CheckBox mCheckBox;
    private View mSmallCheckBox;
    public ScalableCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.scalable_checkbox, this);
        mCheckBox=(CheckBox)findViewById(R.id.my_checkbox);
        mSmallCheckBox=findViewById(R.id.my_small_checkbox);
        mContext=context;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    public void setChecked(boolean isChecked){
        mCheckBox.setChecked(isChecked);
    }

    public void expandCheckBox(boolean isAnim){
        if(isAnim){
            mSmallCheckBox.setVisibility(GONE);

            mCheckBox.setScaleX(0f);
            mCheckBox.setScaleY(0f);
            mCheckBox.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(200)
                    .setListener(null);
        }else{
            mSmallCheckBox.setVisibility(GONE);
            mCheckBox.setScaleX(1f);
            mCheckBox.setScaleY(1f);
        }

    }

    public void shrinkCheckBox(boolean isAnim){

        if(isAnim){
            mCheckBox.setScaleX(1f);
            mCheckBox.setScaleY(1f);
            mCheckBox.animate()
                    .scaleX(0f).scaleY(0f)
                    .setDuration(200)
                    .setListener(null);

            mSmallCheckBox.setAlpha(0f);
            mSmallCheckBox.setVisibility(VISIBLE);
            mSmallCheckBox.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .setInterpolator(new AccelerateInterpolator())
                    .setListener(null);
        } else{
            mCheckBox.setScaleX(0f);
            mCheckBox.setScaleY(0f);

            mSmallCheckBox.setVisibility(VISIBLE);
        }

    }

}
