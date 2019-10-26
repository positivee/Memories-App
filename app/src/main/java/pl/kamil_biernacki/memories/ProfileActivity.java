package pl.kamil_biernacki.memories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {


    private TextView mPhone,mName,mEmail;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth fAuth;
    private FirebaseUser user;

    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mEmail =findViewById(R.id.email);
        mName =findViewById(R.id.profileFullName);
        mPhone =findViewById(R.id.phone);

        fAuth=FirebaseAuth.getInstance();
        user =fAuth.getCurrentUser();
        reference =FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
      /*  FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid()).setValue(user)*/

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               String fullName = dataSnapshot.child("name").getValue().toString();
               String email = dataSnapshot.child("email").getValue().toString();
               String phone = dataSnapshot.child("phone").getValue().toString();
               mEmail.setText(email);

               mName.setText(fullName);
                mPhone.setText(phone);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


       /*fAuth=FirebaseAuth.getInstance();
       mFirebaseDatabase =FirebaseDatabase.getInstance();
       myRef =mFirebaseDatabase.getReference();
        FirebaseUser user = fAuth.getCurrentUser();
        userID =user.getUid();

        mAuthListener =new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){

                }
                else{

                }
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                showData(dataSnapshot);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        private void showData(DataSnapshot dataSnapshot) {
            for(DataSnapshot ds : dataSnapshot.getChildern()){
                User uInfo = new User();
                uInfo.setName(ds.child(userID).getValue(User.class).getName());
                uInfo.setEmail(ds.child(userID).getValue(User.class).getEmail());
                uInfo.setPhone(ds.child(userID).getValue(User.class).getPhone());

                ArrayList<String> array = new ArrayList<>();
                array.add(uInfo.getName());
                array.add(uInfo.getPhone());
                array.add(uInfo.getEmail());
            }

        }
*/


    }

}
