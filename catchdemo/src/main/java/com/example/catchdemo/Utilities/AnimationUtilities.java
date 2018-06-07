package com.example.catchdemo.Utilities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.renderscript.Sampler;
import android.view.View;
import android.view.ViewGroup;

public class AnimationUtilities {


    public static ValueAnimator getHeightAnimation(final View view , int startHeight , int endHeight, int duration, int startDelay) {
        ValueAnimator animatorHeight = ValueAnimator.ofInt(startHeight,endHeight);

        animatorHeight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.getLayoutParams().height = (int)animation.getAnimatedValue();
                view.requestLayout();
            }
        });


        animatorHeight.setDuration(duration);
        animatorHeight.setStartDelay(startDelay);

        return animatorHeight;
    }

    public static ValueAnimator getWidthAnimation(final View view , int startWidth, int endWidth, int duration, int startDelay) {
        ValueAnimator animatorWidth = ValueAnimator.ofInt(startWidth, endWidth);


        animatorWidth.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.getLayoutParams().width = (int)animation.getAnimatedValue();
                view.requestLayout();
            }
        });

        animatorWidth.setDuration(duration);
        animatorWidth.setStartDelay(startDelay);
        return animatorWidth;
    }

    public static ValueAnimator getMarginTopAnimation(final View view , int startMargin, int endMargin, int duration, int startDelay) {
        ValueAnimator animatorWidth = ValueAnimator.ofInt(startMargin, endMargin);


        animatorWidth.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                lp.topMargin = (int)animation.getAnimatedValue();
                view.requestLayout();
            }
        });

        animatorWidth.setDuration(duration);
        animatorWidth.setStartDelay(startDelay);
        return animatorWidth;
    }

    public static ValueAnimator getAlphaAnimation(final View view , float endAlpha, int duration, int startDelay) {
        ObjectAnimator animatorWidth = ObjectAnimator.ofFloat(view, "alpha", endAlpha);



        animatorWidth.setDuration(duration);
        animatorWidth.setStartDelay(startDelay);
        return animatorWidth;
    }

    public static ValueAnimator getScaleXAnimation(final View view, float startX, float endX, int duration, int startDelay) {
        ObjectAnimator animationY = ObjectAnimator.ofFloat(view, "translationX", startX, endX);
        animationY.setDuration(duration);
        animationY.setStartDelay(startDelay);

        return animationY;
    }

    public static ValueAnimator getScaleYAnimation(final View view, float startY, float endY, int duration, int startDelay) {
        ObjectAnimator animationY = ObjectAnimator.ofFloat(view, "translationY", startY, endY);
        animationY.setDuration(duration);
        animationY.setStartDelay(startDelay);

        return animationY;
    }
}
