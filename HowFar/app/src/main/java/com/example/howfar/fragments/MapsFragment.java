package com.example.howfar.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.howfar.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsFragment extends Fragment {
    private GoogleMap googleMap;
    private int mapType = GoogleMap.MAP_TYPE_NORMAL;
    private ArrayList<LatLng> pendingMarkers = new ArrayList();

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap map) {
            googleMap = map;
            for (LatLng marker : pendingMarkers) {
                setMarker(marker);
            }
            googleMap.setMapType(mapType);
            pendingMarkers = new ArrayList<>();
        }
    };

    public void setMarker(LatLng location) {
        if (googleMap == null) {
            pendingMarkers.add(location);
            return;
        }
        googleMap.addMarker(new MarkerOptions()
                .position(location)
                .title("location")
        );
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(14.0F));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.normal);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}