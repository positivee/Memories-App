package pl.kamil_biernacki.memories;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.fragment.app.FragmentActivity;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button add_location;
    String lat,lng;
    Double lati=0.0,lngi=0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Bundle extras =getIntent().getExtras();
        add_location = findViewById(R.id.add_locate_button);


        if (extras != null) {
            lat = extras.getString("latitude");
            lng = extras.getString("longitude");
            add_location.setVisibility(View.INVISIBLE);
            lati =Double.parseDouble(lat);
            lngi = Double.parseDouble(lng);
        }else {
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


        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(lati, lngi);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));




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
                    final double latitude=latLng.latitude;
                    final double longitude=latLng.longitude;
                    add_location.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(MapsActivity.this, NewMemoryActivity.class);
                            intent.putExtra("latitude",""+ latitude);
                            intent.putExtra("longitude","" + longitude);
                            startActivity(intent);
                            finish();
                        }
                    });
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


