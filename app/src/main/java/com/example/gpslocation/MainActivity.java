package com.example.gpslocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gettingLocation();
    }

    public static boolean checkLocationPermission(Activity activity) {
        if (activity != null) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkLocationAccessPermission(Activity activity) {
        if (activity != null) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CONSTANTS.REQUEST_CODE_CHECK_LOCATION_ENABLED);
                return false;
            }
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, CONSTANTS.REQUEST_CODE_CHECK_LOCATION_ENABLED);
                return false;
            }
        }
        return true;
    }

    void gettingLocation() {
        if (!checkLocationPermission(this)) {
            checkLocationAccessPermission(this);
        } else {
            loadLastLocation();
        }

        loadLastLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CONSTANTS.REQUEST_CODE_CHECK_LOCATION_ENABLED:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadLastLocation();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CONSTANTS.REQUEST_CODE_CHECK_LOCATION_ENABLED:
                loadLastLocation();
                break;
        }
    }

    private void loadLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ActivityHomeDrawer.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        GPSLocation gpsLocation = new GPSLocation(this, new GPSLocation.LocationLoadedListener() {
            @Override
            public void locationLoaded(Location location) {
                if (location == null) {
                    LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
                    boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    List<String> providers = service.getProviders(true);
                    Location bestLocation = null;
                    for (String provider : providers) {
                        @SuppressLint("MissingPermission") Location l = service.getLastKnownLocation(provider);
                        if (l == null) {
                            continue;
                        }
                        if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                            bestLocation = l;
                        }
                    }
                    if (bestLocation == null) {
                        return;
                    }
                    location = bestLocation;
                }
                latNew = location.getLatitude();
                lngNew = location.getLongitude();
                getAddress(latNew, lngNew);
                etSearchLocation.setBackground(ContextCompat.getDrawable(ActivityHomeDrawer.this, R.drawable.rounded_white_edittext));

                try {
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.actvity_home_map);
                    mapFragment.getMapAsync(ActivityHomeDrawer.this);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(ActivityHomeDrawer.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            buildGoogleApiClient();
                            if (gMap != null)
                                gMap.setMyLocationEnabled(false);
                        }
                    } else {
                        buildGoogleApiClient();
                        if (gMap != null)
                            gMap.setMyLocationEnabled(false);
                        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng latLng) {
                                hideLocationList();
                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
