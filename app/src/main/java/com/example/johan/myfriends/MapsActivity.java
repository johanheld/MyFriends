package com.example.johan.myfriends;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

public class MapsActivity extends AppCompatActivity implements MapsFragment.OnGroupsPressed
{

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Controller controller;
    public static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private Button btnGroups;
    public static String id;
    public static String group;
    MapsFragment mapFragment;
    GroupFragment groupFragment;
    private boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mapFragment = new MapsFragment();
        groupFragment = new GroupFragment();
        setFragment(mapFragment, false);

//        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//        locationListener = new LocList(this);
//
//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_DENIED) {
//            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
//        } else {
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
//        }

        controller = new Controller(this, groupFragment, mapFragment);
        groupFragment.setController(controller);
//        init();
    }


//    private void init()
//    {
//        btnGroups = (Button) findViewById(R.id.btnGroups);
//        btnGroups.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                if (!connected)
//                {
//                    controller.connect();
//                    connected = true;
//                }
//                controller.getGroups();
//                setFragment(groupFragment, true);
//            }
//        });
//    }

    @Override
    protected void onResume()
    {
        super.onResume();

//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_DENIED) {
//            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
//        } else {
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
//        }
    }

    @Override
    protected void onDestroy()
    {
        controller.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onPause()
    {
        locationManager.removeUpdates(locationListener);
        super.onPause();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
            case REQUEST_ACCESS_FINE_LOCATION:
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
                }
                break;
        }
    }

    public void setFragment(android.app.Fragment fragment, boolean backstack)
    {
        android.app.FragmentManager fm = getFragmentManager();
        android.app.FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, fragment);

        if (backstack)
            ft.addToBackStack(null);

        ft.commit();
    }

    public void startLocationListener()
    {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocList(this, id);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
        } else
        {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, locationListener);
        }
    }

    public void stopLocationListener()
    {
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onGroupPressed()
    {
        if (!connected)
        {
            controller.connect();
            connected = true;
        }
        controller.getGroups();
        setFragment(groupFragment, true);
    }

    private class LocList implements LocationListener
    {
        Activity activity;
        String id;

        public LocList(Activity a, String id)
        {
            this.activity = a;
            this.id = id;
        }

        @Override
        public void onLocationChanged(Location location)
        {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            String lati = ("" + latitude);
            String lon = ("" + longitude);
            Log.d("onLocChanged", "Lng=" + longitude + ",Lat=" + latitude);
            controller.setPosition(id, lon, lati);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

        @Override
        public void onProviderEnabled(String provider)
        {

        }

        @Override
        public void onProviderDisabled(String provider)
        {

        }
    }
}
