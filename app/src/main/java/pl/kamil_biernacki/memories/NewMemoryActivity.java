package pl.kamil_biernacki.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;


public class NewMemoryActivity extends AppCompatActivity {

    private Button mButtonCreate;
    private EditText mTitile,mContent;
    private Toolbar mToolbar;

    private FirebaseAuth fAuth;
    private DatabaseReference fMemoryDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_memory);
        getSupportActionBar().setHomeButtonEnabled(true);


        mButtonCreate = findViewById(R.id.create_memory);
        mTitile = findViewById(R.id.new_memory_title);
        mContent= findViewById(R.id.new_memory_content);




        fAuth =FirebaseAuth.getInstance();
        fMemoryDatabase=FirebaseDatabase.getInstance().getReference().child("Memories").child(fAuth.getCurrentUser().getUid());


        mButtonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String title = mTitile.getText().toString().trim();
                String content =mContent.getText().toString().trim();
                if (!TextUtils.isEmpty(title)&& !TextUtils.isEmpty(content)) {
                    createNote( title,content);
                }
                else{
                    Snackbar.make(view,"Fill empty field",Snackbar.LENGTH_SHORT).show();
                }
            }
        });




    }

    private void createNote(String title,String content){
        if(fAuth.getCurrentUser()!=null){

            final DatabaseReference newMemoryReference = fMemoryDatabase.push();

            final Map noteMap = new HashMap();
            noteMap.put("title",title);
            noteMap.put("content",content);
            noteMap.put("timestamp", ServerValue.TIMESTAMP);

            Thread mainThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    newMemoryReference.setValue(noteMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {


                                Toast.makeText(NewMemoryActivity.this,"Wspomneienie zostało dodane :) " ,Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                finish();

                            }else{
                                Toast.makeText(NewMemoryActivity.this,"Błąd: "+task.getException().getMessage() ,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }
            });
            mainThread.start();


        }else{
            Toast.makeText(this,"Użytkownik nie zalogowany!" ,Toast.LENGTH_SHORT).show();
        }

    }
}
