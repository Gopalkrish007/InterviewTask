package com.example.newtask.activity;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newtask.R;
import com.example.newtask.UserDatabase;
import com.example.newtask.UserImage;
import com.example.newtask.UserImageDao;
import com.example.newtask.view.UserAdapter;
import com.example.newtask.viewModel.UserViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class LaunchActivity extends AppCompatActivity {

    private UserViewModel userViewModel;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;

    private static final int PICK_IMAGE = 1;
    private static final int TAKE_PHOTO = 2;
    private Uri imageUri;

    private UserImageDao userImageDao;
    private int currentUserId; // This will hold the ID of the user being uploaded

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_launch);

        // Initialize the UserDatabase and UserImageDao
        UserDatabase db = UserDatabase.getDatabase(this);
        userImageDao = db.userImageDao();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(new ArrayList<>(), this::uploadImage, userImageDao);
        recyclerView.setAdapter(userAdapter);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getUsers().observe(this, users -> {
            userAdapter.updateUsers(users);
        });
        // Request location permission and fetch current location
        requestLocationPermission();

        checkNetworkAndLocation();

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            // Handle exception
        }
        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            // Handle exception
        }
        return gpsEnabled || networkEnabled;
    }

    private void checkNetworkAndLocation() {
        boolean networkAvailable = isNetworkAvailable();
        boolean locationEnabled = isLocationEnabled();

        if (!networkAvailable || !locationEnabled) {
            StringBuilder message = new StringBuilder();
            if (!networkAvailable) {
                message.append("Please turn on your network.\n");
            }
            if (!locationEnabled) {
                message.append("Please turn on your location.");
            }

            Toast.makeText(this, message.toString(), Toast.LENGTH_LONG).show();
            // Optionally direct user to settings
            if (!locationEnabled) {
                openLocationSettings();
            }
        }
    }

    private void openLocationSettings() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }


    private void uploadImage(int userId) {
        // Display a dialog or menu to choose between camera and gallery
        currentUserId = userId; // Set the current user ID

        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent chooser = Intent.createChooser(pickPhoto, "Select Image");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePhoto});
        startActivityForResult(chooser, PICK_IMAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE && data != null) {
                imageUri = data.getData();
                // Handle the selected image from the gallery
                if (imageUri != null) {
                    saveImageToDatabase(currentUserId, imageUri.toString());
                }
            } else if (requestCode == TAKE_PHOTO && data != null) {
                imageUri = data.getData();
                // Handle the photo taken by the camera
                if (imageUri != null) {
                    saveImageToDatabase(currentUserId, imageUri.toString());
                }
            } else {
                Log.e("MainActivity", "Image selection failed or no data.");
            }
        }
    }

    private void saveImageToDatabase(int userId, String imageUri) {
        // Create a UserImage object with the userId and imageUri
        UserImage userImage = new UserImage(userId, imageUri);

        // Insert the UserImage object into the database using Executors
        Executors.newSingleThreadExecutor().execute(() -> {
            userImageDao.insertUserImage(userImage);
            Log.d("Iguru", "Image saved: UserID = " + userId + ", URI = " + imageUri);


            // Optionally, update the UI after saving the image
            runOnUiThread(() -> {
                Toast.makeText(this, "Image saved successfully!", Toast.LENGTH_SHORT).show();
                // Update RecyclerView or any UI element here if needed
                // Notify adapter about the data change
                userViewModel.getUsers().observe(this, users -> {
                    userAdapter.updateUsers(users);  // Update the adapter's data
                });
            });
        });
    }

    public void deleteImage(int userId, Uri imageUri) {
        // Delete from the database
        Executors.newSingleThreadExecutor().execute(() -> {
            userImageDao.deleteByUserId(userId); // Assuming you want to delete by user ID

            // Update the UI
            runOnUiThread(() -> {
                Toast.makeText(this, "Image deleted successfully!", Toast.LENGTH_SHORT).show();
                // Refresh the RecyclerView or any UI element here if needed
                userViewModel.getUsers().observe(this, users -> {
                    userAdapter.updateUsers(users);  // Update the adapter's data
                });
            });
        });
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            // Permission already granted
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                getCurrentLocation();
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void getCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        String address = getAddressFromLocation(latitude, longitude);
                        // Update TextView with location details
                        TextView locationTextView = findViewById(R.id.user_location);
                        locationTextView.setText(String.format("Lat: %.6f, Lon: %.6f\nAddress: %s", latitude, longitude, address));
                    } else {
                        Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return address.getAddressLine(0); // or any other address components you want
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Address not found";
    }


}