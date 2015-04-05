package com.photoshare.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.photoshare.R;

/**
 * Created by longjianlin on 15/3/22.
 */
public class ShareActionBarView extends RelativeLayout implements Animation.AnimationListener {
    private final Animation mCycleFadeAnimation;

    public ShareActionBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCycleFadeAnimation = AnimationUtils.loadAnimation(context, R.anim.cycle_fade);
        mCycleFadeAnimation.setAnimationListener(this);
    }

    public void animateBackground() {
        View animationBackground = getAnimationBackgroundView();
        if (null != animationBackground && animationBackground.getVisibility() != View.VISIBLE) {
            animationBackground.startAnimation(mCycleFadeAnimation);
            animationBackground.setVisibility(View.VISIBLE);
        }
    }

    public void stopAnimatingBackground() {
        View animationBackground = getAnimationBackgroundView();
        if (null != animationBackground && animationBackground.getVisibility() == View.VISIBLE) {
            animationBackground.setVisibility(View.GONE);
            animationBackground.clearAnimation();
        }
    }

    private View getAnimationBackgroundView() {
        return findViewById(R.id.v_action_upload_bg);
    }

    public void onAnimationEnd(Animation animation) {
        View animationBackground = getAnimationBackgroundView();
        if (null != animationBackground && animationBackground.getVisibility() == View.VISIBLE) {
            animationBackground.startAnimation(animation);
        }
    }

    public void onAnimationRepeat(Animation animation) {
        // NO-OP
    }

    public void onAnimationStart(Animation animation) {
        // NO-OP
    }
}
