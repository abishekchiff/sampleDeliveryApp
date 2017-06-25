package com.skyskew.sampledeliveryapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.location.LocationListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



public class HomeActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerAdapter mAdapter;
    public ArrayList<ConsigneeDetail> myPeople;
    private  BroadcastReceiver receiver;
    private double currLat;
    private  double currLong;

    static  final private String LOC_RESULT = "com.skyskew.sampledeliveryapp.result";
    static final private  String LOC_LAT = "com.skyskew.sampledeliveryapp.lat";
    static final private  String LOC_LONG = "com.skyskew.sampledeliveryapp.long";

    private  void initializer(Bundle instance_state)
    {
        if (instance_state != null){
            myPeople= (ArrayList<ConsigneeDetail>)instance_state.getSerializable("param");
        }
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        myPeople = new ArrayList<ConsigneeDetail>();

        startServiceInHome();

        ConsigneeDetail a1= new ConsigneeDetail("abi",5,13.0827,80.2707184);
        ConsigneeDetail a2 = new ConsigneeDetail("abid",10,13.0827,80.2707184);

        myPeople.add(a1);
        myPeople.add(a2);




        Intent intent =getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");

        if(args!=null) {
            ArrayList<ConsigneeDetail> temp = (ArrayList<ConsigneeDetail>) args.getSerializable("ARRAYLIST");

            if (temp != null) {
                Log.d("passing people","recieved from maps");
                myPeople.clear();
                myPeople.addAll(temp);
            }
        }


        mAdapter = new RecyclerAdapter(myPeople);
        mRecyclerView.setAdapter(mAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
//        final ActionBar ab = getSupportActionBar();
//        //ab.setHomeAsUpIndicator(R.drawable.pin);
//        ab.setDisplayHomeAsUpEnabled(true);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    }


    private  void listeners()
    {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if (Build.VERSION.SDK_INT < 23) {

            Log.d("inside build version","yes");

        } else {
            checkAndRequestPermissions(this);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Context context = view.getContext();
                Log.d("inside float bar","yes");

                    Intent showAddIntent = new Intent(context, MapsSampleMainActivity.class);

                    Bundle args = new Bundle();
                    args.putSerializable("ARRAYLIST",(Serializable)myPeople);
                    showAddIntent.putExtra("BUNDLE",args);
                    context.startActivity(showAddIntent);
                    finish();




                }




        });


////////////////broad cast listener for location


        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                 currLat = intent.getDoubleExtra(LOC_LAT,currLat);
                 currLong = intent.getDoubleExtra(LOC_LONG,currLong);

                for(ConsigneeDetail c: myPeople)
                {
                    c.calculateDistance(currLat,currLong);

                }

                Log.d("Broadcast","Recieved");
                Snackbar.make(mRecyclerView,"Location getting changed",Snackbar.LENGTH_LONG);
                mAdapter.notifyDataSetChanged();


                // do something here.
            }
        };



        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                myPeople.remove(viewHolder.getAdapterPosition());
                mAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }

            @Override
            public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_main);

        initializer(savedInstanceState);
        listeners();
    }
    protected void onSaveInstanceState(Bundle instance_state) {
        super.onSaveInstanceState(instance_state);


        instance_state.putSerializable("param", myPeople);
    }

    @Override
    protected  void onStart()
    {
        super.onStart();
        if (myPeople.size() == 0) {
            requestDetail();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(LOC_RESULT)
        );
    }

    @Override
    protected  void onStop()
    {

        stopServiceInHome();
        super.onStop();
    }
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d("Home Activity life","DESTROYED");

    }
    private int getLastVisibleItemPosition() {
        return mLinearLayoutManager.findLastVisibleItemPosition();
    }
    private void setRecyclerViewScrollListener() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int totalItemCount = mRecyclerView.getLayoutManager().getItemCount();
                if (totalItemCount == getLastVisibleItemPosition() + 1) {
                  //  requestPhoto();
                }
            }
        });
    }
    private void requestDetail()
    {


    }


    private boolean checkAndRequestPermissions(Context context) {

        Log.d("inside check request","yes");
        List<String> listPermissionsNeeded = new ArrayList<>();

        int permissionInternet = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.INTERNET);


        int storagePermission = ContextCompat.checkSelfPermission(getApplicationContext(),


                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int permissionAccessNetworkState = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_NETWORK_STATE);
        int permissionAccessFineLocation = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionAccessCoarseLocation = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);



        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionInternet != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET);
        }

        if (permissionAccessFineLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionAccessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (permissionAccessNetworkState != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }

        //listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()])
        if (!listPermissionsNeeded.isEmpty()) {
          ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
            Log.d("list permiss if","yes");

            return false;
        }

        return true;


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        Log.d("inside on request","yes");
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Permission Granted Successfully. Write working code here.



                } else {
                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                    homeIntent.addCategory( Intent.CATEGORY_HOME );
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(homeIntent);
                }
                break;
        }
    }

    private void startServiceInHome()
    {
        startService(new Intent(getBaseContext(), TrackGps.class));
    }
    private void stopServiceInHome() {

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

        stopService(new Intent(getBaseContext(), TrackGps.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;

    }


}
