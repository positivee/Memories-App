package pl.kamil_biernacki.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText mFullName,mEmail,mPassword,mPhone;
    Button mRegisterButton;
    TextView mLoginText;
    FirebaseAuth fAuth;

    ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmail =findViewById(R.id.email);
        mPassword =findViewById(R.id.password);

        mFullName =findViewById(R.id.fullName);
        mPhone = findViewById(R.id.phone);

        mRegisterButton= findViewById(R.id.regiterButton);
        mLoginText = findViewById(R.id.loginText);

        fAuth   = FirebaseAuth.getInstance();
        progressBar =findViewById(R.id.progressBar);


        //jestli jest zalogowany
        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

        mRegisterButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                final String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                final String phone = mPhone.getText().toString().trim();
                final String name = mFullName.getText().toString().trim();


                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email jest wymagany!");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Hasło jest wymagane!");
                    return;
                }

                if(password.length() <6 ){
                    mPassword.setError("Hasło musi być >= 6 znaków");
                }

                if(phone.length() != 10 ){
                    mPhone.setError("Niepoprawny numer telefonu !");
                }

               

                progressBar.setVisibility(View.VISIBLE);
                //zarejestrowac w firebase
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()){

                            User user = new User(name,
                                                email,
                                                phone);

                            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid()).setValue(user)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                Toast.makeText(RegisterActivity.this, "Utworzono konto",Toast.LENGTH_SHORT).show();
                                                                                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                                                            }
                                                                            else
                                                                            {
                                                                                Toast.makeText(RegisterActivity.this,"Błąd!" +task.getException().getMessage(),Toast.LENGTH_SHORT).show();

                                                                            }
                                                                        }
                                                                    });



                            /*String user_id = fAuth.getCurrentUser().getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

                            Map newPost = new HashMap();
                            newPost.put("name",name);
                            newPost.put("phone",phone);

                            current_user_db.setValue(newPost);

                            Toast.makeText(RegisterActivity.this, "Utworzono konto",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));*/



                        }
                        else
                        {
                            Toast.makeText(RegisterActivity.this,"Błąd!" +task.getException().getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }

        });

        mLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
            }
        });

    }
}
