package com.example.johan.myfriends;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.JsonWriter;
import android.util.Log;

import com.example.johan.myfriends.Modules.Member;
import com.example.johan.myfriends.Modules.TextMessage;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by johan on 2017-10-10.
 */

public class Controller implements Serializable
{
    private TCPConnection mService;
    private boolean bound = false;
    private MainActivity activity;
    private ServiceConnection serviceConn;
    private Listener listener;
    private GroupFragment groupFragment;
    private MapsFragment mapsFragment;
    private MessageFragment messageFragment;
    private ArrayList <TextMessage> messages;

    public Controller(MainActivity mapsActivity, GroupFragment groupFragment, MapsFragment mapsFragment, MessageFragment messageFragment, Bundle savedInstanceState)
    {
        activity = mapsActivity;
        this.groupFragment = groupFragment;
        this.mapsFragment = mapsFragment;
        this.messageFragment = messageFragment;
        Intent intent = new Intent(activity, TCPConnection.class);

        if (savedInstanceState == null)
            activity.startService(intent);

        serviceConn = new ServiceConn();
        boolean result = activity.bindService(intent, serviceConn, 0);
        if (!result)
            Log.d("Controller-constructor", "No binding");

        else
            Log.d("Controller-constructor", "Service has been bound");

        messages = new ArrayList<TextMessage>();
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

                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                    listener = null;
                }
            }
        }
    }

    public List getMessages()
    {
        if (!messages.isEmpty())
            return messages;

        return null;
    }

    /** Messages received from the server are sent here where the message is dealt
     *  with depending on the type of message
     *
     * @param json
     */

    public void readJson(String json)
    {
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
            }

            if (type.equals("locations"))
            {
                JSONArray array = reader.getJSONArray("location");
                Member[] members = new Member[array.length()];

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

            if (type.equals("textchat"))
            {
                String group = reader.getString("group");
                String member = reader.getString("member");
                String text = reader.getString("text");

                messages.add(new TextMessage(group, member, text));
                activity.runOnUiThread(new UpdateMessages(getMessages()));
                Log.d("Listener", "Textmessage received");
            }

            if (type.equals("imagechat"))
            {
                String group = reader.getString("group");
                String member = reader.getString("member");
                String text = reader.getString("text");
                String imageid = reader.getString("imageid");
//                String text = reader.getString("text");

//                messages.add(new TextMessage(group, member, text));
//                activity.runOnUiThread(new UpdateMessages(getMessages()));
                Log.d("Listener", "Image received");
            }

            if (type.equals("upload"))
            {
                String imageId = reader.getString("imageid");
                String port = reader.getString("port");
                mService.sendImage(messageFragment.getImage(), imageId, port);

//                activity.runOnUiThread(new UpdateMessages(getMessages()));
//                Log.d("Listener", "Textmessage received");
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
                    .name("member").value("Bobby Briggs").endObject();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        mService.send(stringWriter.toString());
        activity.startLocationListener();
    }

    /** Unregisters the users from the current group
     *
     * @param id
     */

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

    /** Sends a text message to the server
     *
     * @param text
     */

    public void sendText(String text)
    {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);

        try
        {
            writer.beginObject()
                    .name("type").value("textchat")
                    .name("id").value(MainActivity.id)
                    .name("text").value(text).endObject();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        mService.send(stringWriter.toString());
    }

    public void sendImage(String text)
    {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        Location location = MainActivity.lastLocation;
        String lat = ("" + location.getLatitude());
        String lon = ("" + location.getLongitude());
        try
        {
            writer.beginObject()
                    .name("type").value("imagechat")
                    .name("id").value(MainActivity.id)
                    .name("text").value(text)
                    .name("longitude").value(lon)
                    .name("latitude").value(lat).endObject();
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

    /** When locations from a group is received from the server this method puts the markers
     *  on the mapFragment
     *
     */

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

    /** Updates the ListView in MessageFragment with new messages
     *
     */

    private class UpdateMessages implements Runnable
    {
        private List <TextMessage> list;

        public UpdateMessages(List <TextMessage> list)
        {
            this.list = list;
        }
        @Override
        public void run()
        {
            messageFragment.updateMessages(list);
        }
    }
}
