package com.example.projectmobile1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GridActivity extends AppCompatActivity {

    private int[][] gridColors = new int[5][5];
    private GridLayout gridView;
    private DatabaseReference markerRef;
    private MarkerData marker;
    private String uidOfMarker;
    int sizeSquare;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);

        gridView = findViewById(R.id.gridView);

        uidOfMarker = getIntent().getStringExtra("user_uid");
        markerRef = FirebaseDatabase.getInstance().getReference().child("markers").child(uidOfMarker);
        marker = new MarkerData();
        sizeSquare = marker.getSizeOfTheSquare();

        markerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String colors = dataSnapshot.child("colors").getValue(String.class);
                    if (colors != null && colors.length() == 25) {
                        parseColorsString(colors);
                        setupGridView();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle cancellation if necessary
            }
        });
    }

    private void parseColorsString(String colors) {
        for (int i = 0; i < sizeSquare*sizeSquare; i++) {
            gridColors[i / sizeSquare][i % sizeSquare] = (colors.charAt(i) == 'b') ? 0 : 1;
        }
    }

    private void setupGridView() {

        for (int i = 0; i < sizeSquare; i++) {
            for (int j = 0; j < sizeSquare; j++) {
                final TextView textView = new TextView(this);
                textView.setTextSize(18);
                textView.setPadding(1, 1, 1, 1);

                textView.setBackgroundResource(R.drawable.white_cell_drawable);

                int colorResId = gridColors[i][j] == 0 ? android.R.color.black : android.R.color.white;
                textView.setBackgroundColor(ContextCompat.getColor(this, colorResId));

                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int row = gridView.indexOfChild(textView) / sizeSquare;
                        int col = gridView.indexOfChild(textView) % sizeSquare;
                        gridColors[row][col] = (gridColors[row][col] == 0) ? 1 : 0;
                        int newColorResId = gridColors[row][col] == 0 ? android.R.color.black : android.R.color.white;
                        textView.setBackgroundColor(ContextCompat.getColor(GridActivity.this, newColorResId));

                        updateColorsStringAndFirebase();

                        Intent intent = new Intent(GridActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(i, 1, 1f);
                params.columnSpec = GridLayout.spec(j, 1, 1f);
                textView.setLayoutParams(params);

                gridView.addView(textView);
            }
        }
    }


    private void updateColorsStringAndFirebase() {
        StringBuilder colorsBuilder = new StringBuilder();
        for (int i = 0; i < sizeSquare; i++) {
            for (int j = 0; j < sizeSquare; j++) {
                colorsBuilder.append(gridColors[i][j] == 0 ? 'b' : 'w');
            }
        }
        String newColors = colorsBuilder.toString();

        markerRef.child("colors").setValue(newColors);
    }
}
