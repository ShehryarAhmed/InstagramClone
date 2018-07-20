package com.example.tx.instagram.utils;

import android.content.Context;
import android.icu.lang.UScript;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.tx.instagram.R;
import com.example.tx.instagram.model.User;
import com.example.tx.instagram.model.UserAccountSetting;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseMethod {

    private static final String TAG = "FirebaseMethod";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mRef;

    private String userId;
    private Context mContext;

    public FirebaseMethod(Context context){

        mAuth = FirebaseAuth.getInstance();
        mContext = context;

        if (mAuth.getCurrentUser() != null){
            userId = mAuth.getCurrentUser().getUid();
        }

    }

    /**
     * Register a new email and password  to firebase authentication
     * @param email
     * @param password
     * @param username
     */

    public void registerNewEmail(String email, String password, String username){
        mAuth.createUserWithEmailAndPassword(email,password).
                addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "Create User with email : on complete"+task.isSuccessful());
                        if(task.isSuccessful()){
                            userId = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "on Complete Authstate changed"+task.isSuccessful());
                        }
                        else if(!task.isSuccessful()){
                            Toast.makeText(mContext, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }

    /**
     * Check username is exists
     * @param username
     * @param dataSnapshot
     */

    public boolean checkIfUserNameExist(String username,DataSnapshot dataSnapshot){
        Log.d(TAG, "checkIFUserNameExist: check if user "+username+" already exists or not");

        User user = new User();

        for (DataSnapshot ds: dataSnapshot.getChildren()){
            Log.d(TAG, "checkIFUserNameExist: datasnapshot "+ds);

            user.setUsername(ds.getValue(User.class).getUsername());
            Log.d(TAG, "checkIFUserNameExist: username : "+user.getUsername());

            if(StringManipulation.expandUsername(user.getUsername()).equals(username)){
                Log.d(TAG, "checkIFUserNameExist: Found A match "+user.getUsername());
                return true;
            }
        }

        return false;
    }

    public  void addNewUser(String email, String username, String description, String website, String profile_photo){

        User user = new User(userId,1,email,username);

        mRef.child(mContext.getString(R.string.db_user)).child(userId).setValue(user);

        UserAccountSetting userAccountSetting = new UserAccountSetting(
                description,
                username,
                0,
                0,
                0,
                profile_photo,
                username,
                website);

    }
}
