package com.example.johan.myfriends.Modules;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by johan on 2017-10-12.
 */

public class Member
{
    private String name;
    private LatLng position;

    public Member(String name, LatLng position)
    {
        this.name = name;
        this.position = position;
    }

    public String getName()
    {
        return name;
    }

    public LatLng getPosition()
    {
        return position;
    }
}
