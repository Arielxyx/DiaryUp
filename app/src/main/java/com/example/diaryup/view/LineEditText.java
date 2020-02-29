package com.example.diaryup.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

public class LineEditText extends EditText {
	private Rect mRect;
	private Paint mPaint;

	public LineEditText(Context context, AttributeSet attrs) {
		// TODO Auto-generated constructor stub
		super(context,attrs);
		mRect = new Rect();
		mPaint = new Paint();
		mPaint.setColor(Color.GRAY);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//得到EditText的总行数
		int lineCount = getLineCount();
		Rect r = mRect;
		Paint p = mPaint;
		//为每一行设置格式
//		for(int i = 0; i < lineCount;i++){
//			//取得每一行的基准Y坐标，并将每一行的界限值写到r中
//			int baseline = getLineBounds(i, r);
//			//设置每一行的文字带下划线
//			canvas.drawLine(r.left, baseline+20, r.right, baseline+20, p);
//		}
	}
}

