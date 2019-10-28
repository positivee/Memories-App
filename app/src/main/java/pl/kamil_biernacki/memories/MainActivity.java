package pl.kamil_biernacki.memories;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter adapter;
    private FirebaseAuth fAuth;
    private DatabaseReference reference;
    private TextView mlat, mlng;
    private boolean mLocationPermissionGranted = false;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9003;
    String TAG = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mlat = findViewById(R.id.lat);
        mlng = findViewById(R.id.lng);
        recyclerView = findViewById(R.id.list);





        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        fetch_from_database();




        FloatingActionButton fab = findViewById(R.id.add_memory_button);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, NewMemoryActivity.class);
                intent.putExtra("latitude", "" + 0);
                intent.putExtra("longitude", "" + 0);
                startActivity(intent);

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));

        }
        if (id == R.id.action_logout) {

            FirebaseAuth.getInstance().signOut();
            Toast.makeText(MainActivity.this, "Wylogowano", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkMapServices()) {
            if (mLocationPermissionGranted) {

            } else {
                getLocationPermission();
            }
        }

    }


    private void fetch_from_database() {
        fAuth = FirebaseAuth.getInstance();
        Query query = FirebaseDatabase.getInstance().getReference().child("Memories").child(fAuth.getCurrentUser().getUid())/*.orderByChild("")*/;


        FirebaseRecyclerOptions<MemoryModel> options =
                new FirebaseRecyclerOptions.Builder<MemoryModel>()
                        .setQuery(query, new SnapshotParser<MemoryModel>() {
                            @NonNull
                            @Override
                            public MemoryModel parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new MemoryModel(
                                        snapshot.child("title").getValue().toString(),
                                        snapshot.child("content").getValue().toString(),
                                        snapshot.child("image").getValue().toString(),
                                        snapshot.child("timestamp").getValue().toString());

                            }
                        })
                        .build();

        adapter = new FirebaseRecyclerAdapter<MemoryModel, MemoryViewHolder>(options) {
            @Override
            public MemoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_memory_layout, parent, false);


                return new MemoryViewHolder(view);

            }


            @Override
            protected void onBindViewHolder(MemoryViewHolder holder, final int position, MemoryModel model) {

                holder.setMemoryTitle(model.getTitle());
                holder.setMemoryContent(model.getContent());

                holder.setMemoryImg(model.getImage());
                GetTimeAgo getTimeAgo = new GetTimeAgo();
                holder.setMemoryTime(model.getMemoryTime());
                holder.setMemoryTime(getTimeAgo.getTimeAgo(Long.parseLong(model.getMemoryTime()), getApplicationContext()));


                holder.root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        /* Toast.makeText(MainActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();*/
                        final String noteID = getRef(position).getKey();
                        Log.d("noteID", noteID);


                        Intent intent = new Intent(MainActivity.this, UpdateDeleteMemoryActivity.class);
                        intent.putExtra("noteID", noteID);
                        startActivity(intent);


                    }
                });

                holder.root.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        String noteID = getRef(position).getKey();
                        reference = FirebaseDatabase.getInstance().getReference().child("Memories").child(fAuth.getCurrentUser().getUid());
                        reference.child(noteID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("lat") && dataSnapshot.hasChild("lng")) {
                                    String lat = dataSnapshot.child("lat").getValue().toString();
                                    String lng = dataSnapshot.child("lng").getValue().toString();
                                /*    mlat.setText(lat);
                                    mlng.setText(lng);*/
                                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);

                                    intent.putExtra("latitude", "" + lat);
                                    intent.putExtra("longitude", "" + lng);
                                    startActivity(intent);

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();

                            }
                        });


                        Log.d("LAT", mlat.getText().toString());
                        Log.d("LNG", mlng.getText().toString());
                        Toast.makeText(MainActivity.this, "WHAT A LONG PRESS", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });

            }


        };
        recyclerView.setAdapter(adapter);

    }


    //Maps
    private boolean checkMapServices() {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Ta aplikacje wymaga GPS,aby działała poprawnie, czy chcesz właczyć ?")
                .setCancelable(false)
                .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            /* getChatrooms();*/
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();
            /*Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();*/
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (mLocationPermissionGranted) {
                    /*getChatrooms();*/
                } else {
                    getLocationPermission();
                }
            }
        }

    }

}
