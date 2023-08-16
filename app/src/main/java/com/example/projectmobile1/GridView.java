package com.example.projectmobile1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class GridView extends View {

    private int[][] gridColors; // Le tableau des couleurs
    private int cellSize;
    private Paint paint;

    public GridView(Context context) {
        super(context);
        paint = new Paint();
    }

    public void setGridColors(int[][] gridColors) {
        this.gridColors = gridColors;
        invalidate(); // Redessiner la grille avec les nouvelles couleurs
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        cellSize = getWidth() / 5; // Taille de cellule basée sur la largeur de la vue

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                if (gridColors[row][col] == 0) {
                    paint.setColor(Color.WHITE);
                } else {
                    paint.setColor(Color.BLACK);
                }
                canvas.drawRect(col * cellSize, row * cellSize,
                        (col + 1) * cellSize, (row + 1) * cellSize, paint);
            }
        }
    }
}
