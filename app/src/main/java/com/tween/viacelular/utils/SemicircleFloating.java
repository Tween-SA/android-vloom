package com.tween.viacelular.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Path;
import com.ufreedom.floatingview.transition.BaseFloatingPathTransition;
import com.ufreedom.floatingview.transition.FloatingPath;
import com.ufreedom.floatingview.transition.PathPosition;
import com.ufreedom.floatingview.transition.YumFloating;

/**
 * Animación semicircular aplicable al toque de un elemento visible
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar) on 24/04/2017
 */
public class SemicircleFloating extends BaseFloatingPathTransition
{
	@Override
	public FloatingPath getFloatingPath()
	{
		Path path = new Path();
		path.rLineTo(-100,0);
		path.quadTo(0,-200,100,0);
		path.quadTo(0,200,-100,0);
		return FloatingPath.create(path, false);
	}
	
	@Override
	public void applyFloating(final YumFloating yumFloating)
	{
		ValueAnimator translateAnimator = ObjectAnimator.ofFloat(0, 500);
		translateAnimator.setDuration(600);
		translateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
		{
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator)
			{
				float value = (float) valueAnimator.getAnimatedValue();
				PathPosition floatingPosition = getFloatingPosition(value);
				yumFloating.setTranslationX(floatingPosition.x);
				yumFloating.setTranslationY(floatingPosition.y);
			}
		});
		
		ValueAnimator alphaAnimation = ObjectAnimator.ofFloat(1f,0f);
		alphaAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
		{
			@Override
			public void onAnimationUpdate(ValueAnimator animation)
			{
				yumFloating.setAlpha((Float) animation.getAnimatedValue());
			}
		});
		alphaAnimation.addListener(new AnimatorListenerAdapter()
		{
			@Override
			public void onAnimationEnd(Animator animation)
			{
				super.onAnimationEnd(animation);
				yumFloating.clear();
			}
		});
		
		alphaAnimation.setStartDelay(550);
		alphaAnimation.setDuration(300);
		translateAnimator.start();
		alphaAnimation.start();
	}
}