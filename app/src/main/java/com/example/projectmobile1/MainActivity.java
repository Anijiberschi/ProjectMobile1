package com.example.projectmobile1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    private MapView mapView;
    private DatabaseReference markersDatabase;
    private FirebaseUser currentUser;
    private EmailSender emailSender;

    private ActivityResultLauncher<ScanOptions> barcodeScannerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.map);
        markersDatabase = FirebaseDatabase.getInstance().getReference("markers");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        configureMap();
        setupButtons();
        requestPermissionsIfNecessary();
        retrieveMarkers();
    }

    private void configureMap() {
        MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);

        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        mapView.setMultiTouchControls(true);
        mapView.setTileSource(TileSourceFactory.MAPNIK);

        MapController mapController = (MapController) mapView.getController();
        mapController.setZoom(18);
        GeoPoint helbLocation = new GeoPoint(50.81822811183402, 4.396181149208416);
        mapController.setCenter(helbLocation);
    }

    private void setupButtons() {
        Button scannerButton = findViewById(R.id.btScanner);
        Button addMarkerButton = findViewById(R.id.btAddMarker);
        Button chatButton = findViewById(R.id.bt_toChat);
        Button loginButton = findViewById(R.id.bt_goToLoginActivity);

        if (currentUser != null) {
            setupLoggedInButtons(addMarkerButton, chatButton);
        } else {
            setupLoggedOutButtons(addMarkerButton, chatButton);
        }

        setupScannerButton(scannerButton);

        loginButton.setOnClickListener(view -> openLoginOrProfileActivity());
        chatButton.setOnClickListener(view -> openChatActivity());
    }

    private void setupLoggedInButtons(Button addMarkerButton, Button chatButton) {
        addMarkerButton.setOnClickListener(view -> addMarker());
        chatButton.setOnClickListener(view -> openChatActivity());
    }

    private void setupLoggedOutButtons(Button addMarkerButton, Button chatButton) {
        addMarkerButton.setOnClickListener(view -> showNotConnectedAlert());
        chatButton.setOnClickListener(view -> openChatActivity());
    }

    private void openLoginOrProfileActivity() {
        Class<?> targetActivity = currentUser != null ? Profile.class : LoginActivity.class;
        Intent intent = new Intent(MainActivity.this, targetActivity);
        startActivity(intent);
    }

    private void showNotConnectedAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("You have to be connected to use this feature. Do you want to sign up ?")
                .setTitle("Not Logged !")
                .setCancelable(true)
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.cancel())
                .create()
                .show();
    }

    private void setupScannerButton(Button scannerButton) {
        barcodeScannerLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                handleScannedQRCode(result.getContents());
            }
        });

        scannerButton.setOnClickListener(view -> scanQRCode());
    }

    private void scanQRCode() {
        ScanOptions options = new ScanOptions();
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barcodeScannerLauncher.launch(options);
    }

    private void handleScannedQRCode(String uidOfMarker) {
        DatabaseReference markerRef = markersDatabase.child(uidOfMarker);

        markerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Intent intent = new Intent(MainActivity.this, GridActivity.class);
                    intent.putExtra("user_uid", uidOfMarker);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Invalid QR code = " + uidOfMarker, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to retrieve marker: " + databaseError.getMessage());
            }
        });
    }

    private void addMarker() {
        GeoPoint userLocation = getUserLocation();

        if (userLocation != null) {
            String markerTitle = getAddressFromLocation(userLocation);
            MarkerData markerData = new MarkerData(markerTitle, userLocation.getLatitude(), userLocation.getLongitude(), currentUser.getUid());

            markersDatabase.push().setValue(markerData)
                    .addOnSuccessListener(aVoid -> {
                        sendQRCodeEmail();
                        Toast.makeText(MainActivity.this, "Marker added to Firebase and QR code sent.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to add marker to Firebase", Toast.LENGTH_SHORT).show());
        }
    }

    private GeoPoint getUserLocation() {
        MyLocationNewOverlay myLocationOverlay = (MyLocationNewOverlay) mapView.getOverlays().get(0);
        return myLocationOverlay.getMyLocation();
    }

    private String getAddressFromLocation(GeoPoint location) {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        List<Address> addresses;
        String markerTitle = "New Marker";

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
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

        return markerTitle;
    }

    private void sendQRCodeEmail() {
        emailSender = new EmailSender(currentUser.getEmail().toString(), "Your qr code", markersDatabase.push().getKey());
        emailSender.execute();
    }

    private void openChatActivity() {
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        startActivity(intent);
    }

    private void retrieveMarkers() {
        markersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot markerSnapshot : dataSnapshot.getChildren()) {
                    double latitude = markerSnapshot.child("latitude").getValue(Double.class);
                    double longitude = markerSnapshot.child("longitude").getValue(Double.class);
                    String title = markerSnapshot.child("title").getValue(String.class);

                    Marker marker = new Marker(mapView);
                    marker.setPosition(new GeoPoint(latitude, longitude));
                    marker.setTitle(title);

                    mapView.getOverlays().add(marker);
                }

                mapView.invalidate();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to retrieve markers: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Configuration.getInstance().save(this, PreferenceManager.getDefaultSharedPreferences(this));
        mapView.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
}

