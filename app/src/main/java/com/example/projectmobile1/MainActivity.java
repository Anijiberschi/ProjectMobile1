package com.example.projectmobile1;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String permissions[] = new String []{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        btScanner = findViewById(R.id.btScanner);
        btLogin = findViewById(R.id.bt_goToLoginActivity);

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        btScanner.setOnClickListener(view ->
        {
            scanCode();
        });

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));



        map = (MapView) findViewById(R.id.map);

        Marker marker = new Marker(map);

        marker.setPosition(new GeoPoint(50.818048, 4.395909));
        marker.setTitle("HELB");

// Add the marker to the map
        map.getOverlays().add(marker);

// Refresh the map
        map.invalidate();


        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        map.setMultiTouchControls(true);
        map.setTileSource(TileSourceFactory.MAPNIK);

        MapController mapController = (MapController) map.getController();
        mapController.setZoom(18);




        GeoPoint geoPoint = new GeoPoint(50.81822811183402,4.396181149208416);
        mapController.setCenter(geoPoint);

        requestPermissionsIfNecessary(permissions);

        btAddMarker = findViewById(R.id.btAddMarker);
        btAddMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Set an OnMapClickListener for your map
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null)
                {
                    Toast.makeText(MainActivity.this, "Please log in to use this feature", Toast.LENGTH_SHORT).show();
                }else {
                map.getOverlays().add(new ItemizedIconOverlay<OverlayItem>(
                        MainActivity.this, new ArrayList<OverlayItem>(), null) {
                    @Override
                    public boolean onSingleTapConfirmed(final MotionEvent event, final MapView mapView) {
                        // Get the clicked location's coordinates
                        GeoPoint clickedGeoPoint = (GeoPoint) mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());

                        // Reverse geocode the coordinates to get a human-readable address
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses = null;
                        String markerTitle = "New Marker";
                        try {
                            addresses = geocoder.getFromLocation(clickedGeoPoint.getLatitude(), clickedGeoPoint.getLongitude(), 1);
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

                        // Generate a unique code for the marker to access the grid
                        String markerCode = UUID.randomUUID().toString();
                        String id = UUID.randomUUID().toString();
                        String pixelGridId = UUID.randomUUID().toString();

                        // Create a new Marker object with the clicked location's coordinates and desired properties
                        PixelGrid markerPixelGrid = new PixelGrid();
                        DatabaseReference pixelGridRef = FirebaseDatabase.getInstance().getReference("pixelGrids").child(pixelGridId);
                        pixelGridRef.setValue(markerPixelGrid);

                        MarkerData newMarkerData = new MarkerData(id, markerTitle, clickedGeoPoint.getLatitude(),clickedGeoPoint.getLongitude(), pixelGridId, markerCode);

                        // Add the new Marker object to your list of markers
                        markerDataList.add(newMarkerData);
                        DatabaseReference markerRef = FirebaseDatabase.getInstance().getReference("markers").child(id);
                        markerRef.setValue(newMarkerData);

                        // Create a new OverlayItem with the marker's title and location
                        OverlayItem overlayItem = new OverlayItem(markerTitle, "", clickedGeoPoint);

//                        ItemizedIconOverlay<OverlayItem> markerOverlay = new ItemizedIconOverlay<>(this, OverlayItem, null);
//                        markerOverlay.addItem(overlayItem);
//                        map.getOverlays().add(markerOverlay);

                        // Notify the ItemizedIconOverlay that the data has changed
                        populate();

                        DatabaseReference markersRef = FirebaseDatabase.getInstance().getReference("markers");
                        DatabaseReference newMarkerRef = markersRef.child(id);
                        newMarkerRef.setValue(newMarkerData);

                        DatabaseReference newPixelGridRef = pixelGridRef.child(markerCode);
                        newPixelGridRef.setValue(markerPixelGrid);

                        return true;
                    }
                });
            }
        }});
        map.getOverlays().add(new ItemizedIconOverlay<>(this, new ArrayList<OverlayItem>(), null));
    }

    public void writeToFirebase(DatabaseReference databaseReference, MarkerData markerData) {
        DatabaseReference markerRef = databaseReference.child("markers").push();
        markerRef.child("title").setValue(markerData.getTitle());
        markerRef.child("latitude").setValue(markerData.getLatitude());
        markerRef.child("longitude").setValue(markerData.getLongitude());
        markerRef.child("markerCode").setValue(markerData.getCode());

        //gridList = marker.getPixelGridId().getGrid();
        //markerRef.child("grid").setValue(gridList);
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

}