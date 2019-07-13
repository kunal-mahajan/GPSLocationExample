package com.example.gpslocation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import se.trendtaxi.constants.CONSTANTS;

import static android.content.Context.LOCATION_SERVICE;
import static android.location.GpsStatus.GPS_EVENT_STARTED;
import static android.location.GpsStatus.GPS_EVENT_STOPPED;


public class GPSLocation {
    private final Context context;
    private final LocationLoadedListener loadedListener;
    private FusedLocationProviderClient mFusedLocationClient;

    public GPSLocation(Context context, LocationLoadedListener loadedListener) {
        this.context = context;
        this.loadedListener = loadedListener;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        initProcess();
    }

    public void initProcess() {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (checkIfLocationOpened()) {
            loadLocation();
            return;
        }

        askToTurnOnGPS();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.addGpsStatusListener(new android.location.GpsStatus.Listener() {
            public void onGpsStatusChanged(int event) {
                switch (event) {
                    case GPS_EVENT_STARTED:
                        loadLocation();
                        break;
                    case GPS_EVENT_STOPPED:
                        break;
                }
            }
        });
    }

    private boolean checkIfLocationOpened() {
        String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (provider.contains("gps") || provider.contains("network"))
            return true;
        return false;
    }

    private void loadLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                loadedListener.locationLoaded(location);
            }
        });
    }

    private void askToTurnOnGPS() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        ((Activity) context).startActivityForResult(intent, CONSTANTS.REQUEST_CODE_CHECK_LOCATION_ENABLED);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.turn_on_location_title).setPositiveButton(R.string.generic_yes, dialogClickListener).setNegativeButton(R.string.generic_no, dialogClickListener).show();
    }

    public interface LocationLoadedListener {
        void locationLoaded(Location location);
    }
}
