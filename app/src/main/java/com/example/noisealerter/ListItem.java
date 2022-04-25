package com.example.noisealerter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

public class ListItem {
    private String displayName, fileName;
    private int y;
    private RectF rect;
    private int screenWidth, screenHeight;
    private int height;
    private AlerterData data;
    private boolean alreadyPlayed, isPlaying;

    public ListItem(AlerterData data, int screenWidth, int screenHeight) {
        this.data = data;
        this.displayName = data.getDateAndTime();
        this.fileName = data.getLocalFilePath();
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        height = screenHeight/7;
        alreadyPlayed = false;
        isPlaying = false;
    }

    public void draw(Canvas canvas, int y, Bitmap playbutton, Bitmap pausebutton) {
        rect = new RectF(0, y, screenWidth, y + screenHeight/7);
        this.y = y;
        //draw rectangle
        Paint outlinePaint = new Paint();
        outlinePaint.setColor(Color.GRAY);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(10);
        canvas.drawRect(rect, outlinePaint);

        //draw text
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(80);
        canvas.drawText("Noise", screenWidth/9, y + height/2 + 20, textPaint);
        textPaint.setTextSize(60);
        textPaint.setColor(Color.GRAY);
        String date = data.getDateAndTime().substring(0, 8);
        date = date.substring(4, 6) + "/" + date.substring(6) + "/" + date.substring(0, 4);
        canvas.drawText(date, screenWidth*5/12, y + height/2 - 30, textPaint);
        String time = data.getDateAndTime().substring(8);
        time = time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4, 6);
        canvas.drawText(time, screenWidth*5/12, y + height/2 + 50, textPaint);

        //draw play or pause button
        Paint paint = new Paint();
        if (!isPlaying) {
            canvas.drawBitmap(playbutton, screenWidth * 4 / 5, y + height / 2 - playbutton.getHeight() / 2, paint);
        } else {
            canvas.drawBitmap(pausebutton, screenWidth * 4 / 5, y + height / 2 - pausebutton.getHeight() / 2, paint);
        }

        //draw blue dot if not already played
        paint.setColor(Color.BLUE);
        if (!alreadyPlayed) {
            int radius = 10;
            canvas.drawCircle(screenWidth/18, y + height/2 - radius/2, radius, paint);
        }
    }

    public boolean isClicked (float px, float py, Bitmap playbutton) {
        RectF playRect = new RectF(screenWidth*4/5, y+height/2-playbutton.getHeight()/2, screenWidth*4/5 + playbutton.getWidth(), y+height/2-playbutton.getHeight()/2 + playbutton.getHeight());
        if (playRect.contains(px, py)) {
            alreadyPlayed = true;
            isPlaying = true;
        }
        return playRect.contains(px, py);
    }

    public void setIsPlaying(boolean tempIsPlaying) {
        isPlaying = tempIsPlaying;
    }

    public AlerterData getAlerterData() {
        return data;
    }
}
