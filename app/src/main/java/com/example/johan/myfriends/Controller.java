package com.example.johan.myfriends;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.JsonWriter;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;

/**
 * Created by johan on 2017-10-10.
 */

public class Controller implements Serializable
{
    private TCPConnection mService;
    private boolean bound = false;
    private MapsActivity activity;
    private ServiceConnection serviceConn;
    private Listener listener;
    private GroupFragment groupFragment;
    private MapsFragment mapsFragment;

    public Controller(MapsActivity mapsActivity, GroupFragment groupFragment, MapsFragment mapsFragment, Bundle savedInstanceState)
    {
        //TODO sätt MapsActivitys controller till this
        activity = mapsActivity;
        this.groupFragment = groupFragment;
        this.mapsFragment = mapsFragment;
        Intent intent = new Intent(activity, TCPConnection.class);

        if (savedInstanceState == null)
            activity.startService(intent);

        serviceConn = new ServiceConn();
        boolean result = activity.bindService(intent, serviceConn, 0);
        if (!result)
            Log.d("Controller-constructor", "No binding");

        else
            Log.d("Controller-constructor", "Service has been bound");

//        mService.connect();
    }


    public void onDestroy()
    {
        if (bound)
        {
            activity.unbindService(serviceConn);
            listener.stopListener();
            bound = false;
        }
    }

    public void connect()
    {
        mService.connect();
    }


    //Denna klassen har koll på anslutningen till servicen
    private class ServiceConn implements ServiceConnection
    {

        /**
         * When the Android system creates the mService between the client and service, it calls onServiceConnected() on the ServiceConnection.
         * The onServiceConnected() method includes an IBinder argument, which the client then uses to communicate with the bound service.
         *
         * @param arg0
         * @param service
         */
        public void onServiceConnected(ComponentName arg0, IBinder service)
        {
            TCPConnection.LocalBinder binder = (TCPConnection.LocalBinder) service;
            mService = binder.getService();
            bound = true;
            Log.d("AAAAAAAA", "" + bound);
            listener = new Listener();
            listener.start();
        }

        public void onServiceDisconnected(ComponentName arg0)
        {
            bound = false;
        }
    }

    /** Listens to incoming messages to the TCPConnection service
     *
     */

    private class Listener extends Thread
    {
        public void stopListener()
        {
            interrupt();
            listener = null;
        }

        public void run()
        {
            String message;
            Exception exception;
            while (listener != null)
            {
                try
                {
                    message = mService.receive();
                    Log.d("Controller - Listener", message);
                    readJson(message);
//                    activity.runOnUiThread(new UpdateUI(message));
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                    listener = null;
                }
            }
        }
    }

    public void readJson(String json)
    {

        //TODO ID måste hanteras och sparas för fortsatt kommunikation
        try
        {
            JSONObject reader = new JSONObject(json);


            String type = reader.getString("type");

            if (type.equals("groups"))
            {
                JSONArray array = reader.getJSONArray("groups");
                String [] groups = new String[array.length()];
                for (int i = 0; i < array.length(); i++)
                {
                    JSONObject g = array.getJSONObject(i);
                    groups[i] = g.getString("group");
                }

                activity.runOnUiThread(new UpdateGroups(groups));

            }

            if (type.equals("register"))
            {
                String groupName = reader.getString("group");
                String id = reader.getString("id");

                activity.runOnUiThread(new SetGroup(groupName));
                activity.id =id;
                activity.connectedToGroup = true;
                //activity.runOnUiThread(new StartLocationListener());
            }

            if (type.equals("locations"))
            {
                JSONArray array = reader.getJSONArray("location");
                Member [] members = new Member[array.length()];

                    for (int i = 0; i < array.length(); i++)
                    {
                        JSONObject g = array.getJSONObject(i);
                        String name = g.getString("member");
                        double lat = g.getDouble("latitude");
                        double lon = g.getDouble("longitude");

                        members[i] = new Member(name, new LatLng(lat, lon));
                    }

                    activity.runOnUiThread(new SetMarkers(members));
            }

        } catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /** Registers the user to a group on the server
     *
     *
     * @param groupName Name of group to join
     */

    public void register(String groupName)
    {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        try
        {
            writer.beginObject()
                    .name("type").value("register")
                    .name("group").value(groupName)
                    .name("member").value("Waldo").endObject();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        mService.send(stringWriter.toString());
    }

    public void unregister(String id)
    {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);

        try
        {
            writer.beginObject()
                    .name("type").value("unregister")
                    .name("id").value(id).endObject();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        mService.send(stringWriter.toString());
        activity.stopLocationListener();
    }

    /** Sends the current position of the user to the server
     *
     * @param id
     * @param longitude
     * @param latitude
     */

    public void setPosition(String id, String longitude, String latitude)
    {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);

        try
        {
            writer.beginObject()
                    .name("type").value("location")
                    .name("id").value(id)
                    .name("longitude").value(longitude)
                    .name("latitude").value(latitude).endObject();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        mService.send(stringWriter.toString());
    }

    /** Sends a request to the server to get current members of the group requested
     *
     * @param group
     * @return
     */

    public String getMembers(String group)
    {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        try
        {
            writer.beginObject()
                    .name("type").value("members")
                    .name("group").value(group).endObject();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    /** Sends a request to the server to get current groups
     *
     * @return
     */

    public String getGroups()
    {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        try
        {
            writer.beginObject()
                    .name("type").value("groups").endObject();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

//        Log.d("JSON TO SERVER", stringWriter.toString());

        mService.send(stringWriter.toString());
        return stringWriter.toString();
    }

    /** Takes an array of Strings representing the groups on the server
     *  and places them on the ListView in the GroupFragment
     */

    private class UpdateGroups implements Runnable
    {
        private String [] groups;

        public UpdateGroups(String [] groups)
        {
            this.groups = groups;
        }
        @Override
        public void run()
        {
            groupFragment.setGroups(groups);
        }
    }

    /** Updates the UI in GroupFragment to display the group the user
     *  is a member of
     */

    private class SetGroup implements Runnable
    {
        private String group;

        public SetGroup(String group)
        {
            this.group = group;
        }

        @Override
        public void run()
        {
            groupFragment.setCurrentGroup(group);
            activity.group = group;
        }
    }

    private class SetMarkers implements Runnable
    {
        private Member [] members;

        public SetMarkers(Member [] members)
        {
            this.members = members;
        }
        @Override
        public void run()
        {
            mapsFragment.addMarker(members);
        }
    }

    private class StartLocationListener implements Runnable
    {
        @Override
        public void run()
        {
            activity.startLocationListener();
        }
    }
}
