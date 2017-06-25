package com.skyskew.sampledeliveryapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class TrackGps extends Service implements LocationListener {


    // flag for GPS Status
    static  final private String LOC_RESULT = "com.skyskew.sampledeliveryapp.result";
    static final private  String LOC_LAT = "com.skyskew.sampledeliveryapp.lat";
    static final private  String LOC_LONG = "com.skyskew.sampledeliveryapp.long";



    LocalBroadcastManager broadcaster;


    // flag for network status
    boolean isNetworkEnabled = false;


    Location location;
    double latitude;
    double longitude;
    boolean isGPSEnabled = false;



    boolean possible_to_get_location = false;

    private static final long MIN_UPDATES_DISTANCE = 10;


    private static final long MIN_TIME_UPDATES = 1000 * 10 * 1; // 10 SEC


    protected LocationManager locationManager;





    public Location getLocation() {
        try {
            locationManager = (LocationManager)this
                    .getSystemService(LOCATION_SERVICE);


            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);


            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.possible_to_get_location = true;

                // First get location from Network Provider
                if (isNetworkEnabled) {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.



                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_UPDATES,
                            MIN_UPDATES_DISTANCE, this);

                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }

                }
                // get location from gps if it enabled
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_UPDATES,
                                MIN_UPDATES_DISTANCE, this);

                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
            Log.d("Broadcast","sending from get location");
            sendResult(latitude,longitude);
        } catch (Exception e) {
            // e.printStackTrace();
            Log.e("Location error",
                    "Not able to access location manager", e);
        }

        return location;
    }













    @Override
    public void onLocationChanged(Location location) {
        this.location= location;
       latitude= location.getLatitude();
        longitude = location.getLongitude();
        sendResult(latitude,longitude);



    }


    public void sendResult(double latitude,double longitude)
        {
        Intent intent = new Intent(LOC_RESULT);

            intent.putExtra(LOC_LAT, latitude);
            intent.putExtra(LOC_LONG,longitude);
        Log.d("Broadcast","sendResult");
        broadcaster.sendBroadcast(intent);
    }


    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
       // Toast.makeText(this, " MyService Created ", Toast.LENGTH_LONG).show();
        Log.d("TRACKGPS","oncreate");




        broadcaster = LocalBroadcastManager.getInstance(this);
        getLocation();
    }

    @Override
    public int onStartCommand(Intent intent, int flags,int startId) {
        Log.d("TRACKGPS","onstart"+startId+","+flags);
        //Toast.makeText(this, " MyService Started", Toast.LENGTH_LONG).show();
        if(startId>1)
        {
            sendResult(latitude,longitude);
        }
        return flags;
    }

    @Override
    public void onDestroy() {

        Log.d("TRACKGPS","ondestroy");
        //Toast.makeText(this, "MyService Stopped", Toast.LENGTH_LONG).show();
    }
}
