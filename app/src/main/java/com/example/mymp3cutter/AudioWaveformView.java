package com.example.mymp3cutter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

public class AudioWaveformView extends SurfaceView{
	private static final float MAX_AMPLITUDE_TO_DRAW = 100f;
	private final Paint mPaint;
	public static boolean isWaveDrawn=false;


	public AudioWaveformView(Context context) {
		this(context, null, 0);
		//Toast.makeText(getContext(), "cons1", Toast.LENGTH_SHORT).show();
	}

	public AudioWaveformView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AudioWaveformView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		//Toast.makeText(getContext(), "cons3", Toast.LENGTH_SHORT).show();
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(getResources().getColor(R.color.bgBlue));
		mPaint.setStrokeWidth(0);
		mPaint.setAntiAlias(true);
	}

	public  synchronized void drawWaveform(int[] bytes) {
		Canvas canvas=getHolder().lockCanvas();
		if (canvas != null) {
//			Toast.makeText(getContext(), "not null", Toast.LENGTH_SHORT).show();
			canvas.drawColor(Color.BLACK);

			float width = getWidth();
			float height = getHeight();
			float centerY = height / 4;

			float lastX = -1;
			float lastY = -1;

			for (int x = 0; x < width; x++) {
				int index = (int) ((x / width) * bytes.length);
				int sample = bytes[index];
				float y = (sample / MAX_AMPLITUDE_TO_DRAW) * centerY + centerY;
				if (lastX != -1) {
//					Toast.makeText(getContext(), "drawing", Toast.LENGTH_SHORT).show();
					canvas.drawLine(lastX, lastY, x, y, mPaint);
				}else{
//					Toast.makeText(getContext(), "nope", Toast.LENGTH_SHORT).show();
				}

				lastX = x;
				lastY = y;
			}
			isWaveDrawn=true;
			getHolder().unlockCanvasAndPost(canvas);
		}else{
			//Toast.makeText(getContext(), "null", Toast.LENGTH_SHORT).show();
		}

	}
}
