package com.example.johan.myfriends;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import java.util.List;

public class MainActivity extends AppCompatActivity implements MapsFragment.OnGroupsPressed
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
    MessageFragment messageFragment;
    private boolean connected = false;
    public static boolean connectedToGroup;
    private static String TAG_MAP_FRAGMENT = "mapFragment";
    private static String TAG_GROUP_FRAGMENT = "groupFragment";
    private static String TAG_MESSAGE_FRAGMENT = "messageFragment";
    public static Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapFragment = new MapsFragment();
        groupFragment = new GroupFragment();
        messageFragment = new MessageFragment();
        setFragment(mapFragment, false);

        if (savedInstanceState != null)
        {
            connectedToGroup = savedInstanceState.getBoolean("connectedToGroup");
            id = savedInstanceState.getString("userid");
        }
        controller = new Controller(this, groupFragment, mapFragment, messageFragment, savedInstanceState);
        groupFragment.setController(controller);
        messageFragment.setController(controller);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }




    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putBoolean("connectedToGroup", connectedToGroup);
        outState.putString("userid", id);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
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
        if(connectedToGroup)
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
        locationListener = new LocList(this);//, id);

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
    public void onGroupPressed(String fragment)
    {
        if (!connected)
        {
            controller.connect();
            connected = true;
        }

        if (fragment.equals("GROUPS"))
        {
            controller.getGroups();
            setFragment(groupFragment, true);
        }

        if (fragment.equals("MESSAGES"))
        {
            List messages = controller.getMessages();

            if (messages != null)
            {
                messageFragment.updateMessages(messages);
                Log.d("AAA", "FINNS");
            }
            setFragment(messageFragment, true);
        }
    }

//    public void setId(String id)
//    {
//        locationListener.
//    }

    private class LocList implements LocationListener
    {
        Activity activity;
//        public String id;

        public LocList(Activity a)//, String id)
        {
            this.activity = a;
//            this.id = id;
        }

//        public void setId()
//        {
//            this.id = id;
//        }

        @Override
        public void onLocationChanged(Location location)
        {
            lastLocation = location;
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            String lati = ("" + latitude);
            String lon = ("" + longitude);
            Log.d("onLocChanged", "Lng=" + longitude + ",Lat=" + latitude);

            if (connectedToGroup)
                controller.setPosition(id, lon, lati);

            Log.d("Connected to Group", " " + connectedToGroup);
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
