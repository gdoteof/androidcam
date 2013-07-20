package com.vmx.vmxapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.View;

public class RectangleView extends View {
	private ShapeDrawable mDrawable;
	
	public RectangleView(Context context, AttributeSet attrs, int defStyle) {

		super(context,attrs,defStyle);
		
		int x = 10;
		int y = 10;
		int width = 300;
		int height = 50;
		
		mDrawable = new ShapeDrawable(new RectShape());
		mDrawable.getPaint().setColor(0xff74AC23);
		mDrawable.setBounds(300,0,0,0);
	}
	
	protected void onDraw(Canvas canvas) {
		mDrawable.draw(canvas);
	}
}