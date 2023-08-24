package com.example.projectmobile1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class GridView extends View {

    private int[][] gridColors;
    private int cellSize;
    private Paint paint;
    private MarkerData marker= new MarkerData();


    public GridView(Context context) {
        super(context);
        paint = new Paint();
    }

    public void setGridColors(int[][] gridColors) {
        this.gridColors = gridColors;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int sizeSquare = marker.getSizeOfTheSquare();
        cellSize = getWidth() / sizeSquare;

        for (int row = 0; row < sizeSquare; row++) {
            for (int col = 0; col < sizeSquare; col++) {
                int color = gridColors[row][col] == 0 ? Color.WHITE : Color.BLACK;
                paint.setColor(color);

                float left = col * cellSize;
                float top = row * cellSize;
                float right = (col + 1) * cellSize;
                float bottom = (row + 1) * cellSize;

                canvas.drawRect(left, top, right, bottom, paint);
            }
        }
    }
}
