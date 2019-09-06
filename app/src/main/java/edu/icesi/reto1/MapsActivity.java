package edu.icesi.reto1;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, LocationListener,
        GoogleMap.OnMarkerClickListener, AddDialog.AddDialogListener {

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private Marker locationUser;
    private Geocoder geocoder;
    private ArrayList<Marker> markers;
    private Button addMarker;
    private TextView siteText;
    private boolean selectionMode;
    private String lastNameMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        addMarker = findViewById(R.id.add_btn);
        siteText = findViewById(R.id.site_tv);

        addMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               openDialog();

            }
        });

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
        }, 11);

        markers = new ArrayList<Marker>();
        selectionMode = false;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        geocoder = new Geocoder(this, Locale.getDefault());
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);

        // Add a marker in Sydney and move the camera
        LatLng icesi = new LatLng(3.342262, -76.529901);
        locationUser = mMap.addMarker(new MarkerOptions().position(icesi).title("Usted").icon(
                BitmapDescriptorFactory.fromResource(R.drawable.marker_user)
        ));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(icesi, 15));

        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,  1000, 0, this);
    }


    @Override
    public void onLocationChanged(Location location) {

        LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
        locationUser.setPosition(pos);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));

        if(!selectionMode){
            showMarkerNearest();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if(selectionMode){
            Marker marker =  mMap.addMarker(new MarkerOptions().position(latLng).title(lastNameMarker));
            markers.add(marker);
            selectionMode = false;
            showMarkerNearest();
        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.equals(locationUser)){
            try {
                ArrayList<Address> addresses = (ArrayList<Address>) geocoder.getFromLocation(locationUser.getPosition().latitude, locationUser.getPosition().longitude,1);
                locationUser.setSnippet(addresses.get(0).getAddressLine(0));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        DecimalFormat df = new DecimalFormat("#.##");

        boolean found = false;
        for(int i = 0; i<markers.size() && !found; i++){
            if(marker.equals(markers.get(i))){
                found = true;
                double distance = calculateDistance(locationUser, markers.get(i));
                markers.get(i).setSnippet("Este marcador esta a : " + df.format(distance) + " metros" );
            }
        }
        return false;
    }


    public double calculateDistance(Marker a, Marker b){

        double distance = Math.sqrt(Math.pow(a.getPosition().latitude-b.getPosition().latitude, 2)
                + Math.pow(a.getPosition().longitude-b.getPosition().longitude,2));

        distance = distance* 111.2 * 1000;


        return distance;
    }

    public void openDialog(){
        AddDialog addDialog = new AddDialog();
        addDialog.show(getSupportFragmentManager(), "AddDialog");
    }

    @Override
    public void getMarkerName(String markerName) {
        selectionMode = true;
        lastNameMarker = markerName;
        siteText.setText("Seleccione un punto");
    }


    public void showMarkerNearest(){
        if(markers.size()> 0 ) {

            Marker nearest = markers.get(0);
            double distanceNearest = calculateDistance(locationUser, nearest);

            for (Marker marker : markers
            ) {
                double distance = calculateDistance(locationUser, marker);
                if (distance < distanceNearest) {
                    distanceNearest = distance;
                    nearest = marker;

                }
            }

            if (distanceNearest < 50.0) {
                siteText.setText("Usted se encuentra en " + nearest.getTitle());
            } else {
                siteText.setText("El sitio mas cerca es " + nearest.getTitle());
            }
        }else{
            siteText.setText("No hay marcadores agregados");
        }
    }
}
