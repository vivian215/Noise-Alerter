package com.example.noisealerter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Button {
    private int x, y, width, height;
    private String text;
    private RectF rect;
    private boolean isHome;
    private Paint paint;

    //button constructor
    public Button(int x, int y, int screenWidth, int screenHeight, String text) {
        this.x = x;
        this.y = y;
        this.width = (int) (screenWidth/3.5);
        this.height = screenHeight/15;
        this.text = text;
        rect = new RectF(x, y, x + width, y + height);
        isHome = false;
        this.paint = new Paint();
        paint.setARGB(255, 44, 118, 201);
    }

    //checks if the button is clicked
    public boolean isClicked (float px, float py) {
        return px > x && px < x+width && py > y && py < y +height;
    }

    //draws the button
    public void draw(Canvas canvas, int paddingX, Paint outlinePaint, Paint textPaint) {
        canvas.drawRect(rect, paint);
        canvas.drawRect(rect, outlinePaint);
        canvas.drawText(text, x + paddingX, y + height/2 + 30, textPaint);
    }

    public void clickButton() {

        if (isHome) {
            paint.setARGB(255, 44, 118, 201);
            this.text = "Away";
        } else {
            paint.setARGB(255, 126, 86, 175);
            this.text = "Home";
        }
        isHome = !isHome;
    }

    public boolean isHome() {
        return isHome;
    }
}

