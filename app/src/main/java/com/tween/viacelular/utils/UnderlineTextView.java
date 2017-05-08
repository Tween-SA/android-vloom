package com.tween.viacelular.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.TypedValue;

/**
 * Re-implementación de librería para mostrar letras en cabecera de listados
 * @author Eric Frohnhoefer
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar)
 */
public class UnderlineTextView extends AppCompatTextView
{
	private int mUnderlineHeight	= 0;

	public UnderlineTextView(Context context)
	{
		this(context, null);
	}

	public UnderlineTextView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public UnderlineTextView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs)
	{
		Resources r			= getResources();
		mUnderlineHeight	= (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, r.getDisplayMetrics());
	}

	@Override
	public void setPadding(int left, int top, int right, int bottom)
	{
		super.setPadding(left, top, right, bottom + mUnderlineHeight);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
	}
}