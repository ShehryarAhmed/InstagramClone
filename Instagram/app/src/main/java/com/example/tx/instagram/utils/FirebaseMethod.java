package com.example.tx.instagram.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.tx.instagram.R;
import com.example.tx.instagram.model.User;
import com.example.tx.instagram.model.UserAccountSetting;
import com.example.tx.instagram.model.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
        firebaseDatabase = FirebaseDatabase.getInstance();
        mRef = firebaseDatabase.getReference();
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
                            sendVerficationEmail();
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

//    public boolean checkIfUserNameExist(String username,DataSnapshot dataSnapshot){
//        Log.d(TAG, "checkIFUserNameExist: check if user "+username+" already exists or not");
//
//        User user = new User();
//
//        for (DataSnapshot ds: dataSnapshot.child(userId).getChildren()){
//            Log.d(TAG, "checkIFUserNameExist: datasnapshot "+ds);
//
//            user.setUsername(ds.getValue(User.class).getUsername());
//            Log.d(TAG, "checkIFUserNameExist: username : "+user.getUsername());
//
//            if(StringManipulation.expandUsername(user.getUsername()).equals(username)){
//                Log.d(TAG, "checkIFUserNameExist: Found A match "+user.getUsername());
//                return true;
//            }
//        }
//
//        return false;
//    }

    /**
     * Add ne user on firebase
     * @param email
     * @param username
     * @param description
     * @param website
     * @param profile_photo
     */
    public  void addNewUser(String email, String username, String description, String website, String profile_photo){

        User user = new User(userId,1,email,StringManipulation.condenseUsername(username));

        mRef.child(mContext.getString(R.string.dbname_user)).child(userId).setValue(user);

        UserAccountSetting userAccountSetting = new UserAccountSetting(
                description,
                username,
                0,
                0,
                0,
                profile_photo,
                StringManipulation.condenseUsername(username),
                website);

        mRef.child(mContext.getString(R.string.db_user_account_setting)).child(userId).setValue(userAccountSetting  );

    }

    public void sendVerficationEmail(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                            }else{
                                Toast.makeText(mContext, "Cloudnt send Verfication email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public UserSettings getUserSettings(DataSnapshot dataSnapshot) {
        Log.d(TAG, "getUserAccountSetting: reterieving user account setting firebase.");

        UserAccountSetting settings = new UserAccountSetting();
        User user = new User();

        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            // user account settings node
            if (ds.getKey().equals(mContext.getString(R.string.db_user_account_setting))) {
                Log.d(TAG, "getUserAccountSetting: datasnapshot : " + ds);

                try {

                    settings.setDisplay_name(
                            ds.child(userId)
                                    .getValue(UserAccountSetting.class)
                                    .getDisplay_name()
                    );
                    settings.setUsername(
                            ds.child(userId)
                                    .getValue(UserAccountSetting.class)
                                    .getUsername()
                    );
                    settings.setWebsite(
                            ds.child(userId)
                                    .getValue(UserAccountSetting.class)
                                    .getWebsite()
                    );
                    settings.setDescription(
                            ds.child(userId)
                                    .getValue(UserAccountSetting.class)
                                    .getDescription()
                    );
                    settings.setProfile_photo(
                            ds.child(userId)
                                    .getValue(UserAccountSetting.class)
                                    .getProfile_photo()
                    );
                    settings.setPosts(
                            ds.child(userId)
                                    .getValue(UserAccountSetting.class)
                                    .getPosts()
                    );
                    settings.setFollowing(
                            ds.child(userId)
                                    .getValue(UserAccountSetting.class)
                                    .getFollowing()
                    );
                    settings.setFollowers(
                            ds.child(userId)
                                    .getValue(UserAccountSetting.class)
                                    .getFollowers()
                    );
                } catch (NullPointerException e) {
                    Log.d(TAG, "getUserAccountSetting: NullPointerException " + e.getMessage());
                }
            }
                // user node
                if (ds.getKey().equals(mContext.getString(R.string.dbname_user))) {
                    Log.d(TAG, "getUserAccountSetting: datasnapshot : " + ds);

                    user.setUsername(
                            ds.child(userId)
                                    .getValue(User.class)
                                    .getUsername()
                    );
                    user.setEmail(
                            ds.child(userId)
                                    .getValue(User.class)
                                    .getEmail()
                    );
                    user.setPhone_number(
                            ds.child(userId)
                                    .getValue(User.class)
                                    .getPhone_number()
                    );
                    user.setUser_id(
                            ds.child(userId)
                                    .getValue(User.class)
                                    .getUser_id()
                    );

                    Log.d(TAG, "getUserAccountSetting: retried users information : " + toString());
                }


        }
        return new UserSettings(user,settings);
    }


    /**
     * Update user account setting node for the current user
     * @param displayName
     * @param description
     * @param website
     * @param phoneNumber
     */
    public void updateUserAccountSettings(String displayName,String description,String website, long phoneNumber){
        Log.d(TAG, "updateUserAccountSettings: updating user account setting");

        if(displayName != null){
            mRef.child(mContext.getString(R.string.db_user_account_setting))
                .child(userId)
                .child(mContext.getString(R.string.filed_display_name))
                .setValue(displayName);}
        if(description != null){
            mRef.child(mContext.getString(R.string.db_user_account_setting))
                .child(userId)
                .child(mContext.getString(R.string.filed_website))
               .setValue(description);}
        if(website != null){
            mRef.child(mContext.getString(R.string.db_user_account_setting))
                .child(userId)
                .child(mContext.getString(R.string.filed_description))
                .setValue(website);}
        if(phoneNumber != 0){
            mRef.child(mContext.getString(R.string.dbname_user))
                .child(userId)
                .child(mContext.getString(R.string.filed_phone_number))
                .setValue(phoneNumber);}
    }
    /**
     * Update username the email in the user's and user account setting node
     * @param username
     */

    public void updateUsername(String username) {
        Log.d(TAG, "updateUsername: update username to "+username);

        mRef.child(mContext.getString(R.string.dbname_user))
                .child(userId)
                .child(mContext.getString(R.string.filed_username))
                .setValue(username);
        mRef.child(mContext.getString(R.string.db_user_account_setting))
                .child(userId)
                .child(mContext.getString(R.string.filed_username))
                .setValue(username);
    }

    /**
     * Update email the email in the user's node
     * @param email
     */
    public void updateEmail(String email) {
        Log.d(TAG, "update User email: to "+email);

        mRef.child(mContext.getString(R.string.dbname_user))
                .child(userId)
                .child(mContext.getString(R.string.filed_email))
                .setValue(email);
        }


}
