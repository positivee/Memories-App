package pl.kamil_biernacki.memories;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button add_location,myLocation;
    String lat, lng;
    Double lati = 0.0, lngi = 0.0;
    LatLng geoPoint;
    private TextView mSetLat,mSetLong;

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSetLat=findViewById(R.id.lat_view);
        mSetLong=findViewById(R.id.long_view);



        Bundle extras = getIntent().getExtras();
        add_location = findViewById(R.id.add_locate_button);
        myLocation=findViewById(R.id.my_location);

        if (extras != null) {
            lat = extras.getString("latitude");
            lng = extras.getString("longitude");
            add_location.setVisibility(View.INVISIBLE);
            lati = Double.parseDouble(lat);
            lngi = Double.parseDouble(lng);
        } else {
            add_location.setVisibility(View.VISIBLE);
        }

       /*  lati =Float.parseFloat(lat);
        lngi = Float.parseFloat(lng);*/
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    /*    double lati =Double.parseDouble(lat);
        double lngi = Double.parseDouble(lng);*/

        getLastKnownLocation();


        geoPoint = new LatLng(lati, lngi);
        mSetLat.setText(""+geoPoint.latitude);
        mSetLong.setText(""+geoPoint.longitude);
        mMap.addMarker(new MarkerOptions().position(geoPoint).title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(geoPoint));

        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLastKnownLocation();
                geoPoint = new LatLng(geoPoint.latitude, geoPoint.longitude);
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(geoPoint).title("Twoja Lokalizacja"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(geoPoint));
                mSetLat.setText(""+geoPoint.latitude);
                mSetLong.setText(""+geoPoint.longitude);
            }
        });


        // Setting a click event handler for the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                // This will be displayed on taping the marker
                markerOptions.title(latLng.latitude + " : " + latLng.longitude);

                // Clears the previously touched position
                mMap.clear();

                // Animating to the touched position
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);
                mSetLat.setText(""+latLng.latitude);
                mSetLong.setText(""+latLng.longitude);

            }
        });

        add_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, NewMemoryActivity.class);
                intent.putExtra("latitude", "" + mSetLat.getText().toString().trim());
                intent.putExtra("longitude", "" + mSetLong.getText().toString().trim());
                startActivity(intent);
                finish();
            }
        });

    }

    private void getLastKnownLocation(){
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    Location location =task.getResult();
                     geoPoint = new LatLng(location.getLatitude(),location.getLongitude());
                    Log.d("LAT",""+ geoPoint.latitude);
                    Log.d("LONG", ""+geoPoint.longitude);

                }
            }
        });
    }

}


    /*@Override
    public void onLocationChange(Location location){
        String msg ="Updated Location: "+
                Double.toString(location.getLatitude())+ ", "+ Double.toString(location.getLongitude());
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
        latLng= new LatLng(location.getLatitude(),location.getLongitude());

        SupportMapFragment mapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

    }*/


