package com.example.tx.instagram.Login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tx.instagram.R;
import com.example.tx.instagram.utils.FirebaseMethod;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActvityt";


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private Context mContext;
    private String email, username, password;
    private EditText mEmail, mPassword, mUsername;
    private TextView loadingPleaseWait;
    private Button btnRegister;
    private ProgressBar mProgressBar;

    private FirebaseMethod firebaseMethod;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mRef;

    private String append = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Log.d(TAG, "onCreate: started");
        initWidget();
        setUpFirebaseAuth();

        firebaseMethod = new FirebaseMethod(mContext);
        registerBtn();
    }



    /*
     ********************************************init widget**************************************************
     */
        private void initWidget(){
            Log.d(TAG, "initWidget: started");
            mContext = RegisterActivity.this;
            mEmail = (EditText) findViewById(R.id.input_email);
            mPassword = (EditText) findViewById(R.id.input_password);
            mUsername = (EditText) findViewById(R.id.input_username);
            mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
            loadingPleaseWait = (TextView) findViewById(R.id.tv_please_wait);
            btnRegister = (Button) findViewById(R.id.register_btn);

            mProgressBar.setVisibility(View.GONE);
            loadingPleaseWait.setVisibility(View.GONE);
        }


    /*
     ********************************************Check input**************************************************
     *
     */
    private boolean checkInput(String email, String password, String username){
        Log.d(TAG, "checkString: started");
        if (email.equals("") || password.equals("") || username.equals("")){
            return false;
        }
        else{
            return true;
        }

    }


    /*
     ***********************************************resgister button******************************************************
     */
    private void registerBtn(){
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = mEmail.getText().toString().trim();
                password = mPassword.getText().toString().trim();
                username = mUsername.getText().toString().trim();
                if(checkInput(email,password,username)){
                    mProgressBar.setVisibility(View.VISIBLE);
                    loadingPleaseWait.setVisibility(View.VISIBLE);

                    firebaseMethod.registerNewEmail(email,password,username);
                }
                else{
                    Toast.makeText(mContext, "All fields must be filled out", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /*
     ***********************************************Firebase******************************************************
     */
    /**
     * setup Firebase auth object
     */

    private void setUpFirebaseAuth(){
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        mRef =  firebaseDatabase.getReference();
        mAuthListner =  new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if user is logged in
                if(user != null){
                    Log.d(TAG, "onAuthStateChanged: "+user.getUid());
                    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (firebaseMethod.checkIfUserNameExist(username, dataSnapshot)){
                                append = mRef.push().getKey().substring(3,17);
                                Log.d(TAG, "onDataChange: username already exists. appending random string to name : "+append);
                            }
                            username = username + append;
                           //add  new user to the database
                            firebaseMethod.addNewUser(email, username, "","","");

                            Toast.makeText(mContext, "Signup Successfully, Sending Verification email", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    finish();
                }
                else{
                    Log.d(TAG, "onAuthStateChanged: signout");                }
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListner);

    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(mAuthListner != null){
            mAuth.removeAuthStateListener(mAuthListner);
        }
    }


}
