package com.example.tx.instagram.Login;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toolbar;

import com.example.tx.instagram.MainActivity;
import com.example.tx.instagram.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private Context mContext;
    private EditText mEmail, mPassword;
    private TextView mPleasWait;
    private TextView mLinkSignUp;
    private ProgressBar mProgressBar;
    private Button btnLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate: started");
        initActivityWidget();

        mPleasWait.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);

        setUpFirebaseAuth();
        loginBtn();
        linkSingupBtn();
    }

    private void initActivityWidget(){

        mContext = LoginActivity.this;
        mEmail = (EditText) findViewById(R.id.input_email);
        mPassword = (EditText) findViewById(R.id.input_password);
        mPleasWait = (TextView) findViewById(R.id.tv_please_wait);
        mLinkSignUp = (TextView) findViewById(R.id.link_signup);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnLogin = (Button) findViewById(R.id.login_btn);
    }
    private  void loginBtn(){
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: attempting to log in");

                String email =  mEmail.getText().toString();
                String password =  mPassword.getText().toString();

                if (isStringNull(email) && isStringNull(password)){
                    Toast.makeText(mContext, "you must fill all field", Toast.LENGTH_SHORT).show();
                }
                else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mPleasWait.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    Log.d(TAG, "onComplete: "+task.isSuccessful());
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        try{
                                            if(user.isEmailVerified()){
                                                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                                            }
                                            else{
                                                Toast.makeText(mContext, "Email is not verfied \n check your email inbox", Toast.LENGTH_SHORT).show();
                                                mProgressBar.setVisibility(View.GONE);
                                                mPleasWait.setVisibility(View.GONE);
                                                mAuth.signOut();
                                            }

                                        }catch (NullPointerException e){
                                            Log.e(TAG, "onComplete: Nullpointer exception"+e.getMessage() );
                                        }
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    // ...
                                }
                            });


                }
                if (mAuth.getCurrentUser() != null){
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            }
        });
    }


    /*
    ****************************link on signup button ***********************************************
     */
    private void linkSingupBtn(){
        mLinkSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: naviagting to register");
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));

            }
        });
    }
    /*
      ********************************************Check String**************************************************
      *
     */
    private boolean isStringNull(String string){
        Log.d(TAG, "isStringNull: checking string if null");
        if (string.equals("")){
            return true;
        }
        else{
            return false;
        }
    }
    /*
     ***********************************************Firebase******************************************************
     */
    /**
     * setup Firebase auth object
     */

    private void setUpFirebaseAuth(){
        mAuth = FirebaseAuth.getInstance();
        mAuthListner =  new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if user is logged in
                if(user != null){
                    Log.d(TAG, "onAuthStateChanged: "+user.getUid());
                }
                else{
                    Log.d(TAG, "signout: ");
                }
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
