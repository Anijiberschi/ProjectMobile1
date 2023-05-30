package com.example.projectmobile1;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.osmdroid.views.overlay.Marker;

public class FirebaseDatabaseManager {
    private DatabaseReference markersRef;
    private DatabaseReference gridCellsRef;

    public FirebaseDatabaseManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        markersRef = database.getReference("markers");
        gridCellsRef = database.getReference("gridCells");
    }

    public void saveMarker(Marker marker) {
        String markerId = markersRef.push().getKey(); // Generate a unique identifier for the marker
        markersRef.child(markerId).setValue(marker);
    }

    public void saveGridCell(GridCell gridCell) {
        String gridCellKey = generateGridCellKey(gridCell.getRow(), gridCell.getColumn());
        gridCellsRef.child(gridCellKey).setValue(gridCell);
    }

    private String generateGridCellKey(int row, int column) {
        return "row_" + row + "_column_" + column;
    }
}