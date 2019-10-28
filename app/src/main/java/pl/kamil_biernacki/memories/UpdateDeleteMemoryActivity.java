package pl.kamil_biernacki.memories;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class UpdateDeleteMemoryActivity extends AppCompatActivity {

    private Button mButtonUpdate;
    private ImageView mImage;
    private EditText mTitile, mContent;

    private FirebaseAuth fAuth;
    private String noteID;
    private boolean isExist;
    private DatabaseReference reference;
    public Uri imageUri;
    public String myUrl = "";
    StorageTask uploadTask;
    FirebaseStorage storage;
    private StorageReference storageReference;

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
        mImage = findViewById(R.id.memoryImage);
        mButtonUpdate = findViewById(R.id.update_memory);


        fAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Memories").child(fAuth.getCurrentUser().getUid());
        storage = FirebaseStorage.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Images");

        mButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mTitile.getText().toString();
                String content = mContent.getText().toString();
                updateMemory(title, content, myUrl);


                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(content) && myUrl != "") {
                    if (uploadTask != null && uploadTask.isInProgress()) {
                        Toast.makeText(UpdateDeleteMemoryActivity.this, "Zdjecie jest wysyłane!", Toast.LENGTH_SHORT).show();

                        updateMemory(title, content, myUrl);

                    }
                } else {
                    Snackbar.make(v, "Uzupełnij puste pola", Snackbar.LENGTH_SHORT).show();

                }
            }


        });

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileChooser();
            }
        });

        putData();
    }

    private void updateMemory(String title, String content, String myUrl) {

        if (fAuth.getCurrentUser() != null) {

            if (isExist) {
                // UPDATE A NOTE


                Map updateMap = new HashMap();
                updateMap.put("title", mTitile.getText().toString().trim());
                updateMap.put("content", mContent.getText().toString().trim());
                if (myUrl != "") updateMap.put("image", myUrl);
                updateMap.put("timestamp", ServerValue.TIMESTAMP);

                reference.child(noteID).updateChildren(updateMap);

                Toast.makeText(this, "Zaktualizowano wspomnienie", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
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
                        String image = dataSnapshot.child("image").getValue().toString();

                        mTitile.setText(title);
                        mContent.setText(content);
                        Picasso.get().load(image).into(mImage);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(UpdateDeleteMemoryActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();

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
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

    private String getExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    private void FileUploader() {

        final StorageReference Ref = storageReference.child(fAuth.getCurrentUser().getUid()).child(System.currentTimeMillis() + "." + getExtension(imageUri));
        uploadTask = Ref.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                myUrl = uri.toString();


                            }
                        });


                        Toast.makeText(UpdateDeleteMemoryActivity.this, "Zdjecie dodane", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                    }
                });


    }

    private void FileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) ;
        imageUri = data.getData();

        mImage.setImageURI(imageUri);
        FileUploader();
    }

}