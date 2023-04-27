package com.example.projectmobile1;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PixelGrid {
    private List<List<Integer>> grid;
    private int x =10;
    private int y =10;
    public PixelGrid() {
        // Initialize the grid with all cells set to 0 (white)
        grid = new ArrayList<>();
        for (int i = 0; i < x; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < y; j++) {
                row.add(0);
            }
            grid.add(row);
        }
    }

    public PixelGrid getGrid(DataSnapshot dataSnapshot) {
        List<List<Integer>> gridList = new ArrayList<>();
        for (DataSnapshot rowSnapshot : dataSnapshot.child("grid").getChildren()) {
            List<Integer> rowList = new ArrayList<>();
            for (DataSnapshot pixelSnapshot : rowSnapshot.getChildren()) {
                int pixel = pixelSnapshot.getValue(Integer.class);
                rowList.add(pixel);
            }
            gridList.add(rowList);
        }
        int numRows = gridList.size();
        int numCols = gridList.get(0).size();
        int[][] grid = new int[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                grid[i][j] = gridList.get(i).get(j);
            }
        }
        return new PixelGrid();
    }



    public void setPixel(int x, int y, int color) {
        grid.get(x).set(y, color);
    }


}

