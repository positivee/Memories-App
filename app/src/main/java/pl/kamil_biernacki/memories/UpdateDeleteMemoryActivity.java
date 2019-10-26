package pl.kamil_biernacki.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;



public class UpdateDeleteMemoryActivity extends AppCompatActivity {

    private Button mButtonDelete,mButtonUpdate;
    private EditText mTitile,mContent;
    private Toolbar mToolbar;
    private FirebaseAuth fAuth;
    private String noteID;
    private boolean isExist;
    private DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_delete_memory);
        /*ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);*/
        getSupportActionBar().setHomeButtonEnabled(true);


        try {
            noteID = getIntent().getStringExtra("noteID");


            if (!noteID.trim().equals("")) {
                isExist = true;
            } else {
                isExist = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        mTitile = findViewById(R.id.memoryTitle);
        mContent = findViewById(R.id.memoryContent);
        mButtonUpdate = findViewById(R.id.update_memory);
        mButtonDelete = findViewById(R.id.delete_memory);

        fAuth =FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Memories").child(fAuth.getCurrentUser().getUid());

        mButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mTitile.getText().toString();
                String content = mContent.getText().toString();
                updateMemory(title,content);

                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();

            }
        });
        mButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteMemory();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        });

        putData();
    }

    private void updateMemory(String title, String content) {

        if (fAuth.getCurrentUser() != null) {

            if (isExist) {
                // UPDATE A NOTE
                Map updateMap = new HashMap();
                updateMap.put("title", mTitile.getText().toString().trim());
                updateMap.put("content", mContent.getText().toString().trim());
                updateMap.put("timestamp", ServerValue.TIMESTAMP);

                reference.child(noteID).updateChildren(updateMap);

                Toast.makeText(this, "Note updated0", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void deleteMemory() {

        reference.child(noteID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(UpdateDeleteMemoryActivity.this, "Note Deleted", Toast.LENGTH_SHORT).show();
                    noteID = "no";
                    finish();
                } else {
                    Log.e("MainActivity", task.getException().toString());
                    Toast.makeText(UpdateDeleteMemoryActivity.this, "ERROR: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void putData() {

        if (isExist) {
            reference.child(noteID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("content")) {
                        String title = dataSnapshot.child("title").getValue().toString();
                        String content = dataSnapshot.child("content").getValue().toString();

                        mTitile.setText(title);
                        mContent.setText(content);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(UpdateDeleteMemoryActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();

                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.memory_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.delete_memory_button) {
            deleteMemory();
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

}