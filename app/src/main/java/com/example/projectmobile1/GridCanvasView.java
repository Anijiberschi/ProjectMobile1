package com.example.projectmobile1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

public class GridCanvasView extends View {

    private int numColumns = 10;
    private int numRows = 10;
    private boolean[][] cellState;

    private int cellWidth;
    private int cellHeight;

    private Drawable whiteCellDrawable;
    private Drawable blackCellDrawable;

    public GridCanvasView(Context context) {
        super(context);
        initialize();
    }

    private void initialize() {
        cellState = new boolean[numRows][numColumns];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                cellState[i][j] = false;
            }
        }

        whiteCellDrawable = getResources().getDrawable(R.drawable.white_cell_drawable);
        blackCellDrawable = getResources().getDrawable(R.drawable.black_cell_drawable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int availableWidth = MeasureSpec.getSize(widthMeasureSpec);
        int availableHeight = MeasureSpec.getSize(heightMeasureSpec);

        cellWidth = availableWidth / numColumns;
        cellHeight = availableHeight / numRows;

        int desiredWidth = cellWidth * numColumns;
        int desiredHeight = cellHeight * numRows;

        setMeasuredDimension(
                resolveSize(desiredWidth, widthMeasureSpec),
                resolveSize(desiredHeight, heightMeasureSpec)
        );
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int cellWidth = getWidth() / numColumns;
        int cellHeight = getHeight() / numRows;

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                Drawable cellDrawable = cellState[i][j] ? blackCellDrawable : whiteCellDrawable;
                cellDrawable.setBounds(j * cellWidth, i * cellHeight, (j + 1) * cellWidth, (i + 1) * cellHeight);
                cellDrawable.draw(canvas);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int cellWidth = getWidth() / numColumns;
        int cellHeight = getHeight() / numRows;

        int row = (int) (event.getY() / cellHeight);
        int col = (int) (event.getX() / cellWidth);

        if (row >= 0 && row < numRows && col >= 0 && col < numColumns) {
            cellState[row][col] = !cellState[row][col];
            invalidate();  // Redraw the view
        }

        return super.onTouchEvent(event);
    }
}

