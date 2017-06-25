package com.skyskew.sampledeliveryapp;


import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class ViewConsignmentActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,LocationListener,GoogleApiClient.OnConnectionFailedListener ,OnMapReadyCallback {








    GoogleMap myGMap;
    private LocationRequest myLocReq;

    private GoogleApiClient myGApiClient;

    double latitude;
    double longitude;
    private TrackGps gps;
    private LatLng makerPoint;
    private Location currentpoint;
    private Button viewConsigneeBtn;
    boolean myUpdatesRequested = false;


    private ImageView imageView;
    private LatLng center;

    private TextView viewConsigneeNameText;
    private  TextView viewDistText;

    private  ConsigneeDetail consigneeDetail;
    public ArrayList<ConsigneeDetail> objectList;

    ConsigneeDataSource datasource;
    private void initializer()
    {

        consigneeDetail = new ConsigneeDetail();

        viewConsigneeNameText = (TextView) findViewById(R.id.view_consignee_text);


        imageView = (ImageView) findViewById(R.id.view_marker_img);
        viewDistText = (TextView)findViewById(R.id.view_dist_text);

        viewConsigneeBtn = (Button)findViewById(R.id.view_consignee_btn);
        Toolbar toolbar = (Toolbar) findViewById(R.id.view_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        datasource = new ConsigneeDataSource(this);
        datasource.open();

//
//        Intent intent = getIntent();
//        Bundle args = intent.getBundleExtra("BUNDLE");
//        objectList = (ArrayList<ConsigneeDetail>) args.getSerializable("ARRAYLIST");




    }

    private  void  listeners()
        {

            viewConsigneeBtn.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View v) {


                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" +latitude+","+ longitude));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

                }

                });

        }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_consignment);

        initializer();
        listeners();


       getData();

        checkAndConnectClient();

    }

    private void getData()
    {
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE FROM CARD");

        if(args != null)
        {


            consigneeDetail = (ConsigneeDetail) args.getSerializable("CONSIGNEE DETAIL");
            latitude = (double) consigneeDetail.getLat();
            longitude = (double) consigneeDetail.getLon();
            viewConsigneeNameText.setText(consigneeDetail.getName());

            float dist = intent.getFloatExtra("dist",0f);
            putDistance(dist);
        }
        else
        {
            Log.d("bundle","yes");
        }
    }

    private void checkAndConnectClient()
    {
        // checking google plays availability

        int status = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getBaseContext());

        if (status != ConnectionResult.SUCCESS) {
            // Google Play Services are
            // not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
                    requestCode);
            dialog.show();
        }
        else
        {
            //play services are available

            // this
            myLocReq = LocationRequest.create();
            myUpdatesRequested = false;
            myLocReq.setInterval(2000);
            myLocReq.setFastestInterval(2000);
            myLocReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            myGApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            myGApiClient.connect();
        }
    }

    private  void handleMap()
    {

        ((MapFragment) getFragmentManager().findFragmentById(
                R.id.view_map_fragment)).getMapAsync(this);



    }

    @Override
    public void onConnected(Bundle bundle) {
        handleMap();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myGMap=googleMap;
        myGMap.setMyLocationEnabled(true);
        myGMap.getUiSettings().setZoomControlsEnabled(false);
        myGMap.getUiSettings().setMyLocationButtonEnabled(true);
        myGMap.getUiSettings().setCompassEnabled(true);
        myGMap.getUiSettings().setRotateGesturesEnabled(true);
        myGMap.getUiSettings().setZoomGesturesEnabled(true);




        PendingResult<Status> result = LocationServices.FusedLocationApi
                .requestLocationUpdates(myGApiClient, myLocReq,
                        new LocationListener() {

                            @Override
                            public void onLocationChanged(Location location) {

                              currentpoint = location;
                                float distInFloat = calculateDistance();
                                putDistance(distInFloat);


                            }
                        });

        result.setResultCallback(new ResultCallback<Status>() {

            @Override
            public void onResult(Status status) {

                if (status.isSuccess()) {

                    Log.e("Result", "success");

                } else if (status.hasResolution()) {
                    // Google provides a way to fix the issue
                    try {
                        status.startResolutionForResult(ViewConsignmentActivity.this,
                                100);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        //gps tracking code
        trackGps();
    }

    private void trackGps() {



        // This is to resize the bitmapdescriptorFactory icon ,on clicking the layout
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.pin);
        Bitmap b=bitmapdraw.getBitmap();
        final Bitmap smallMarker = Bitmap.createScaledBitmap(b, 80, 80, false);


            makerPoint = new LatLng(latitude, longitude);


            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(makerPoint).
                            zoom(19f).
                            build();

            myGMap.setMyLocationEnabled(true);
            myGMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


            center = makerPoint;


            //handles the marker position
            try {

                        LatLng latLng1 = new LatLng(center.latitude,
                                center.longitude);

                        Marker m = myGMap.addMarker(new MarkerOptions()
                                .position(latLng1)
                                .title(latLng1.toString())
                                .snippet("")
                                .icon(BitmapDescriptorFactory
                                        .fromBitmap(smallMarker)));

                        m.setDraggable(true);


                    } catch (Exception e) {

                        Toast.makeText(this,"Location not Recieved,Apologies",Toast.LENGTH_SHORT);
                    }


        //}

    }

    @Override
    public void onLocationChanged(Location location) {
        currentpoint = location;
        calculateDistance();
        Toast.makeText(this,"View Cons onLocation changed",Toast.LENGTH_SHORT);
    }

    public float calculateDistance()
    {
        int distance;
        float distInFloat;
        float results[]=new float[5];
        Location.distanceBetween(
                latitude,
                longitude,
                currentpoint.getLatitude(),
                currentpoint.getLongitude(),
                results);
        distInFloat = results[0];

       return distInFloat;

    }

    private void putDistance(float distInFloat)
    {
        if(distInFloat>1000)
        {
            distInFloat = distInFloat/1000;
            viewDistText.setText( String.format("%.1f",distInFloat) +" KM");
        }else {
            viewDistText.setText((int)distInFloat+" M");
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_consignee, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent goToHome;
        switch (item.getItemId())
        {
            case R.id.action_back_consignee:
                 goToHome = new Intent(this,HomeActivity.class);
                startActivity(goToHome);
                finish();
                break;
            case R.id.action_complete_consignee :
                consigneeDetail.setCompleted(true);
                int updateResult=datasource.updateConsignee(consigneeDetail);
                Log.d("Updating consignee",String.valueOf(updateResult));
                goToHome = new Intent(this,HomeActivity.class);
                startActivity(goToHome);
                finish();
                break;
            case R.id.action_delete_consignee:

                datasource.deleteConsignee(consigneeDetail);
                goToHome = new Intent(this,HomeActivity.class);
                //goToHome.putExtra("delete item",);
                startActivity(goToHome);
                finish();
                break;

        }
        return true;
    }
}
