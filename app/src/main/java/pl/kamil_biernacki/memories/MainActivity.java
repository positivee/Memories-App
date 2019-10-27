package pl.kamil_biernacki.memories;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity {



    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter adapter;
    private FirebaseAuth fAuth;
    private DatabaseReference reference;
    private TextView mlat,mlng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mlat =findViewById(R.id.lat);
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
                intent.putExtra("latitude",""+ 0);
                intent.putExtra("longitude",""  + 0);
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
            startActivity(new Intent(getApplicationContext(),ProfileActivity.class));

        }
        if (id == R.id.action_logout) {

            FirebaseAuth.getInstance().signOut();
            Toast.makeText(MainActivity.this, "Wylogowano", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
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


    private void fetch_from_database() {
        fAuth =FirebaseAuth.getInstance();
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
                holder.setMemoryTime(getTimeAgo.getTimeAgo(Long.parseLong(model.getMemoryTime()),getApplicationContext()));


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

                holder.root.setOnLongClickListener( new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    String noteID = getRef(position).getKey();
                    reference=FirebaseDatabase.getInstance().getReference().child("Memories").child(fAuth.getCurrentUser().getUid());
                    reference.child(noteID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("lat") && dataSnapshot.hasChild("lng")) {
                                String lat = dataSnapshot.child("lat").getValue().toString();
                                String lng = dataSnapshot.child("lng").getValue().toString();
                                mlat.setText(lat);
                                mlng.setText(lng);
                            }
                            }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(MainActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();

                        }
                    });

                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);

                   intent.putExtra("latitude",""+ mlat.getText().toString());
                    intent.putExtra("longitude","" + mlng.getText().toString());
                    startActivity(intent);
                    finish();
                   Log.d("LAT",mlat.getText().toString());
                    Log.d("LNG",mlng.getText().toString());
                    Toast.makeText(MainActivity.this, "WHAT A LONG PRESS", Toast.LENGTH_SHORT).show();
                    return true;
                    }
                });

            }


        };
        recyclerView.setAdapter(adapter);

    }

    public interface MyCallback {
        String onCallbackLat(String value);
        void onCallbackLng(String value);
    }
}
