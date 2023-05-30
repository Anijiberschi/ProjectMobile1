package com.example.projectmobile1;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity  {
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private Button btScanner;
    private Button btLogin;
    private Button btAddMarker;
    private MapView map = null;
    private List<MarkerData> markerDataList = new ArrayList<>();
    private DatabaseReference databaseReference;
    private GridLayout gridLayout;
    private FirebaseDatabaseManager firebaseDatabaseManager;
    DatabaseReference markersRef;
    private static final int NUM_COLUMNS = 15; // Example value, adjust according to your grid layout
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btScanner = findViewById(R.id.btScanner);
        btLogin = findViewById(R.id.bt_goToLoginActivity);
        btAddMarker = findViewById(R.id.btAddMarker);
        map = (MapView) findViewById(R.id.map);
        Marker marker = new Marker(map);
        MyLocationNewOverlay myLocationNewOverlay = new MyLocationNewOverlay(map);
        myLocationNewOverlay.enableMyLocation();
        map.getOverlays().add(myLocationNewOverlay);
        markersRef = FirebaseDatabase.getInstance().getReference("markers");



        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String permissions[] = new String []{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        Button addMarkerButton = findViewById(R.id.btAddMarker);

        if (currentUser != null ) {
            addMarkerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the user's current location
                    GeoPoint userLocation = myLocationNewOverlay.getMyLocation();

                    if (userLocation != null) {
                        // Reverse geocode the coordinates to get a human-readable address
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses = null;
                        String markerTitle = "New Marker";
                        try {
                            addresses = geocoder.getFromLocation(userLocation.getLatitude(), userLocation.getLongitude(), 1);
                            if (addresses != null && addresses.size() > 0) {
                                Address address = addresses.get(0);
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                                    sb.append(address.getAddressLine(i));
                                    if (i < address.getMaxAddressLineIndex()) {
                                        sb.append(", ");
                                    }
                                }
                                markerTitle = sb.toString();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        MarkerData marker = new MarkerData(markerTitle, userLocation.getLatitude(), userLocation.getLongitude(), currentUser.getEmail());

                        // Add the marker to Firebase
                        DatabaseReference markersRef = FirebaseDatabase.getInstance().getReference("markers");
                        markersRef.push().setValue(marker)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MainActivity.this, "Marker added to Firebase", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "Failed to add marker to Firebase", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                }
            });
        }else if (currentUser == null)
        {
            addMarkerButton.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v){
                    NotConnectedAlertBox();
                }
            });

        }




        btScanner.setOnClickListener(view ->
        {
            scanCode();
        });

        LoginOrLogout(currentUser, btLogin);


        AddMarkerHELB(map, marker);
        CenteratHelb(map);
        requestPermissionsIfNecessary(permissions);
        retrieveMarkers();




    }

    public void NotConnectedAlertBox()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        // Set the message show for the Alert time
        builder.setMessage("You have to be connected to use this feature. Do you want to sign up ?");


        // Set Alert Title
        builder.setTitle("Not Logged !");

        // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
        builder.setCancelable(true);

        // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
        builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
            // When the user click yes button then app will close
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
        builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
            // If user click no then dialog box is canceled.
            dialog.cancel();
        });

        // Create the Alert dialog
        AlertDialog alertDialog = builder.create();
        // Show the Alert Dialog box
        alertDialog.show();
    }


    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up


    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


    private void scanCode()
    {
        ScanOptions options = new ScanOptions();
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLaucher.launch(options);
    }

    private void LoginOrLogout (FirebaseUser currentUser, Button login)
    {
        if(currentUser != null){
            login.setText("Logout");
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MainActivity.this, "Logout", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
            });
        }else
        {
            login.setText("Login");
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(),result ->
    {
        if (result.getContents() != null)
        {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null)
            {
                Toast.makeText(MainActivity.this, "Please log in to use this feature", Toast.LENGTH_SHORT).show();
            }else {
            Intent intent = new Intent(this, LoginActivity.class); // Replace RedirectActivity with your desired activity class
            startActivity(intent);
        }}
    });


    private void AddMarkerHELB (MapView map, Marker marker)
    {
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        marker.setPosition(new GeoPoint(50.818048, 4.395909));
        marker.setTitle("HELB");

// Add the marker to the map
        map.getOverlays().add(marker);
        map.invalidate();
    }

    private void CenteratHelb(MapView map){
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        map.setMultiTouchControls(true);
        map.setTileSource(TileSourceFactory.MAPNIK);

        MapController mapController = (MapController) map.getController();
        mapController.setZoom(18);

        GeoPoint geoPoint = new GeoPoint(50.81822811183402,4.396181149208416);
        mapController.setCenter(geoPoint);
    }

    private void updateCellUI(GridCell cell) {
        // Get the reference to the corresponding view in the grid layout based on cell's row and column
        View cellView = gridLayout.getChildAt(cell.getRow() * NUM_COLUMNS + cell.getColumn());

        // Update the background color of the cell view based on the cell's state
        int backgroundColor = ContextCompat.getColor(cellView.getContext(), cell.isState() ? R.color.black : R.color.white);

    }
    private void retrieveMarkers() {
        markersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                // Iterate over the dataSnapshot to get each marker
                for (DataSnapshot markerSnapshot : dataSnapshot.getChildren()) {
                    // Retrieve the marker object
                    Marker marker = new Marker(map);

                    double latitude = markerSnapshot.child("latitude").getValue(Double.class);
                    double longitude = markerSnapshot.child("longitude").getValue(Double.class);
                    String title = markerSnapshot.child("title").getValue(String.class);

                    marker.setPosition(new GeoPoint(latitude, longitude));
                    marker.setTitle(title);

                    map.getOverlays().add(marker);
                }

                map.invalidate();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to retrieve markers: " + databaseError.getMessage());
            }
        });
    }

}