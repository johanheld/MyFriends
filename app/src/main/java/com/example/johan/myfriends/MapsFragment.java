package com.example.johan.myfriends;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback
{
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;
    OnGroupsPressed mListener;
    private Button btnGroups;
    private Button btnMessages;

    public MapsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        try {
            mListener = (OnGroupsPressed) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnGroupsPressed");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_map, container, false);
        init(mView);
        return mView;
    }

    private void init(View mView)
    {
        btnGroups = (Button)mView.findViewById(R.id.btnGroups);
        btnGroups.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.onGroupPressed("GROUPS");
            }
        });
        btnMessages = (Button)mView.findViewById(R.id.btnMessages);
        btnMessages.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mListener.onGroupPressed("MESSAGES");
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        mMapView = (MapView) mView.findViewById(R.id.map);

        if (mMapView != null)
        {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    public interface OnGroupsPressed
    {
        public void onGroupPressed(String fragment);
    }

    public void addMarker(Member[] members)
    {
        mGoogleMap.clear();
        for (int n = 0; n < members.length; n++)
        {
            LatLng position = members[n].getPosition();
            String name = members[n].getName();

            MarkerOptions mo = new MarkerOptions().position(position).title(name);
            mGoogleMap.addMarker(mo);
        }
//        MarkerOptions mo = new MarkerOptions().position(latLng).title("My position");
//        mGoogleMap.addMarker(mo);

//        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 3));
    }
}
