package com.skyskew.sampledeliveryapp;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MapsSampleMainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener ,OnMapReadyCallback{

    static  final private String LOC_RESULT = "com.skyskew.sampledeliveryapp.result";
    static final private  String LOC_LAT = "com.skyskew.sampledeliveryapp.lat";
    static final private  String LOC_LONG = "com.skyskew.sampledeliveryapp.long";

    GoogleMap myGMap;
    private LocationRequest myLocReq;

    private GoogleApiClient myGApiClient;

    double lat;
    double longitude;
    //private TrackGps gps;
    private LatLng curentpoint;
    private Button addConsigneeBtn ;
    boolean myUpdatesRequested = false;
    private TextView markText;
    private LinearLayout markLay;
    private ImageView imageView;
    private LatLng center;

    private EditText consignee_add_edit_text;

    private  ConsigneeDetail consigneeDetail;

    private  BroadcastReceiver receiver;

    ConsigneeDataSource datasource;

    private void initializer()
    {
        startServiceInMaps();
        longitude = 80.2707184;
        lat =       13.0827;
        consigneeDetail = new ConsigneeDetail();

        consignee_add_edit_text= (EditText)findViewById(R.id.consignee_add_edit_text);
        markText =(TextView)findViewById(R.id.marker_text);
        markLay = (LinearLayout) findViewById(R.id.marker_linear);
        imageView = (ImageView) findViewById(R.id.marker_img);

        addConsigneeBtn = (Button)findViewById(R.id.add_consignee_btn);
        Toolbar toolbar = (Toolbar) findViewById(R.id.maps_toolbar);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);


//
//        Intent intent = getIntent();
//        Bundle args = intent.getBundleExtra("BUNDLE");
//        objectList = (ArrayList<ConsigneeDetail>) args.getSerializable("ARRAYLIST");

        datasource = new ConsigneeDataSource(this);
        datasource.open();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps_sample_main);


            initializer();



        addConsigneeBtn.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View v) {
                String name;
                if((name=consignee_add_edit_text.getText().toString())!=null&&center!=null)
                {
                    consigneeDetail.setName(name);
                    consigneeDetail.setLat(center.latitude);
                    consigneeDetail.setLon(center.longitude);
                    consigneeDetail.setDistance(8);

                    datasource.createConsignee(name,center.latitude,center.longitude);

                    Log.d("Add Consigness btn","clicked");
                    Intent showHomeActivity = new Intent(v.getContext(),HomeActivity.class);
//
//                    Bundle args = new Bundle();
//                    args.putSerializable("ARRAYLIST",(Serializable)objectList);
//                    showHomeActivity.putExtra("BUNDLE",args);
                    startActivity(showHomeActivity);
                    finish();

                }
            }
        });


        //reciever to get current location from Track Gps service


        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                lat = intent.getDoubleExtra(LOC_LAT,lat);
                longitude = intent.getDoubleExtra(LOC_LONG,longitude);

                checkAndConnectClient();

                Log.d("Broadcast","Recieved in maps Sample Activity");
                stopServiceInMaps();


                // do something here.
            }
        };






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

            myLocReq = LocationRequest.create();
            myUpdatesRequested = false;
            myLocReq.setInterval(10000);
            myLocReq.setFastestInterval(10000);
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
                    R.id.add_map_fragment)).getMapAsync(this);



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




//        PendingResult<Status> result = LocationServices.FusedLocationApi
//                .requestLocationUpdates(myGApiClient, myLocReq,
//                        new LocationListener() {
//
//                            @Override
//                            public void onLocationChanged(Location location) {
//                                lat = location.getLatitude();
//                                longitude =location.getLongitude();
//
//                            }
//                        });
//
//        result.setResultCallback(new ResultCallback<Status>() {
//
//            @Override
//            public void onResult(Status status) {
//
//                if (status.isSuccess()) {
//
//                    Log.e("Result", "success");
//
//                } else if (status.hasResolution()) {
//                    // Google provides a way to fix the issue
//                    try {
//                        status.startResolutionForResult(MapsSampleMainActivity.this,
//                                100);
//                    } catch (IntentSender.SendIntentException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });






        //gps tracking code
        trackGps();
        handleMarkerPosition();

    }


    private void trackGps() {







            curentpoint = new LatLng(lat, longitude);


            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(curentpoint).
                            zoom(19f).
                            build();

            myGMap.setMyLocationEnabled(true);
            myGMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));





    }

    private  void handleMarkerPosition()
    {


        // This is to resize the bitmapdescriptorFactory icon ,on clicking the layout
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.pin);
        Bitmap b=bitmapdraw.getBitmap();
        final Bitmap smallMarker = Bitmap.createScaledBitmap(b, 80, 80, false);



        //getting the location when the map is dragged
        myGMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition arg0) {

                //center = myGMap.getCameraPosition().target;
                center=arg0.target;

                markText.setText(center.latitude+" , "+center.longitude);
                myGMap.clear();
                markLay.setVisibility(View.VISIBLE);


            }
        });


        //handles on selecting a position

        imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
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

                    markLay.setVisibility(View.GONE);
                } catch (Exception e) {
                }

            }
        });
    }

    private void startServiceInMaps()
    {
        startService(new Intent(getBaseContext(), TrackGps.class));
    }
    private void stopServiceInMaps() {

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

        stopService(new Intent(getBaseContext(), TrackGps.class));
    }

    @Override
    protected  void onStart()
    {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(LOC_RESULT)
        );
    }
    @Override
    protected  void onStop()
    {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_consignee, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_back_consignee:
                Intent goToHome = new Intent(this,HomeActivity.class);
                startActivity(goToHome);
                finish();

        }
        return true;
    }
}
