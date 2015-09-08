package com.android.emergencyapp.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.emergencyapp.R;

import java.util.Random;

/**
 * Created by Nirajan on 8/15/2015.
 */
public class CircularImageView extends ImageView {

    // letter to be drawn inside the view
    private char letter;

    private Paint textPaint;
    private Paint backgroundPaint;
    // text color
    private int textColor = Color.WHITE;


    public CircularImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        // choose the primary color
        backgroundPaint.setColor(getResources().getColor(R.color.primary));
    }

    public char getLetter() {
        return letter;
    }

    public void setLetter(char letter) {
        this.letter = letter;
        invalidate();
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getDrawable() == null) {
            textPaint.setTextSize(canvas.getHeight() - 8 * getResources().getDisplayMetrics().density * 2);
            // draw the circle/oval shape
            canvas.drawCircle(canvas.getWidth() / 2f, canvas.getHeight() / 2f, Math.min(canvas.getWidth(), canvas.getHeight()) / 2f,
                    backgroundPaint);


            // Draw the text
            Rect text = new Rect();
            textPaint.getTextBounds(String.valueOf(letter), 0, 1, text);
            float textWidth = textPaint.measureText(String.valueOf(letter));
            float textHeight = text.height();
            canvas.drawText(String.valueOf(letter), canvas.getWidth() / 2f - textWidth / 2f,
                    canvas.getHeight() / 2f + textHeight / 2f, textPaint);
        }
    }


}
