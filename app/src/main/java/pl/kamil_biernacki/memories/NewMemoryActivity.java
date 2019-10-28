package pl.kamil_biernacki.memories;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class NewMemoryActivity extends AppCompatActivity {

    private Button mButtonCreate, mAddLocalization;
    private EditText mTitile, mContent;
    private ImageView mAdd_img;
    private TextView mLocationLat, mLocationLng;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    private FirebaseAuth fAuth;
    private DatabaseReference fMemoryDatabase;

    public Uri imageUri;
    public String myUrl = "";
    private StorageTask uploadTask;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    String lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_memory);
        getSupportActionBar().setHomeButtonEnabled(true);


        mAddLocalization = findViewById(R.id.add_localiztion);
        mButtonCreate = findViewById(R.id.create_memory);
        mTitile = findViewById(R.id.new_memory_title);
        mContent = findViewById(R.id.new_memory_content);
        mLocationLat = findViewById(R.id.locationTextLat);
        mLocationLng = findViewById(R.id.locationTextLng);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            lat = extras.getString("latitude");
            lng = extras.getString("longitude");
            mLocationLat.setText("Szerokość geograficzna: " + lat);
            mLocationLng.setText("długość geograficzna: " + lng);
        }


        mAdd_img = findViewById(R.id.add_img);


        fAuth = FirebaseAuth.getInstance();

        fMemoryDatabase = FirebaseDatabase.getInstance().getReference().child("Memories").child(fAuth.getCurrentUser().getUid());

        /* storageReference = FirebaseStorage.getInstance().getReference().child("Images");*/

        storage = FirebaseStorage.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("Images");

        mButtonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String title = mTitile.getText().toString().trim();
                final String content = mContent.getText().toString().trim();

                if (mAdd_img.getDrawable() == null) {
                    Snackbar.make(view, "Dodaj zdjęcie", Snackbar.LENGTH_SHORT).show();
                } else {
                    if (uploadTask != null && uploadTask.isInProgress()) {
                        Toast.makeText(NewMemoryActivity.this, "Zdjecie jest wysyłane!", Toast.LENGTH_SHORT).show();
                    } else {
                        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(content)) {

                            createNote(title, content, myUrl, lat, lng);
                            Log.d("myuri", myUrl);

                        } else {
                            Snackbar.make(view, "Uzupełnij puste pola", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }


            }
        });

        mAdd_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileChooser();
            }
        });
        mAdd_img.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TakePicture();
                return false;
            }
        });
        mAddLocalization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewMemoryActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });


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


                        Toast.makeText(NewMemoryActivity.this, "Zdjecie dodane", Toast.LENGTH_SHORT).show();
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
    private void TakePicture() {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            mAdd_img.setImageURI(imageUri);
            FileUploader();
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                assert extras != null;
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                mAdd_img.setImageBitmap(imageBitmap);
             /*  String linx = saveToInternalStorage(imageBitmap);
              *//*  imageUri=imageUri2;*//*
             *//*   imageUri= Uri.parse(linx);*//*
                String mystring="Hello";
                imageUri = Uri.parse(linx);
                FileUploader();*/
            }
        }

    }
    private String saveToInternalStorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File myPath = new File(directory, "kamil" + ".jpg");

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(myPath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return myPath.getAbsolutePath();
    }

    private void createNote(String title, String content, String myUrl, String lat, String lng) {
        if (fAuth.getCurrentUser() != null) {

            final DatabaseReference newMemoryReference = fMemoryDatabase.push();


            final Map noteMap = new HashMap();

            noteMap.put("title", title);
            noteMap.put("content", content);
            noteMap.put("image", myUrl);
            noteMap.put("lat", lat);
            noteMap.put("lng", lng);
            noteMap.put("timestamp", ServerValue.TIMESTAMP);


            Thread mainThread = new Thread(new Runnable() {
                @Override
                public void run() {


                    newMemoryReference.setValue(noteMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {


                                Toast.makeText(NewMemoryActivity.this, "Wspomneienie zostało dodane :) ", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();

                            } else {
                                Toast.makeText(NewMemoryActivity.this, "Błąd: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }
            });
            /*  if(myUrl!="")*/
            mainThread.start();


        } else {
            Toast.makeText(this, "Użytkownik nie zalogowany!", Toast.LENGTH_SHORT).show();
        }

    }
}
