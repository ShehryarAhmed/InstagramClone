package com.example.tx.instagram.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.tx.instagram.Home.HomeActivity;
import com.example.tx.instagram.Profile.AccountSettingActivity;
import com.example.tx.instagram.R;
import com.example.tx.instagram.model.Photo;
import com.example.tx.instagram.model.User;
import com.example.tx.instagram.model.UserAccountSetting;
import com.example.tx.instagram.model.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FirebaseMethod {

    private static final String TAG = "FirebaseMethod";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference mRef;
    private StorageReference  mStorageReference;
    private double mPhotoloadProgress = 0;

    private String userId;
    private Context mContext;

    public FirebaseMethod(Context context){

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        mRef = firebaseDatabase.getReference();

        mStorageReference = FirebaseStorage.getInstance().getReference();
        mContext = context;

        if (mAuth.getCurrentUser() != null){
            userId = mAuth.getCurrentUser().getUid();
        }

    }

    public void uploadNewPhoto(String photoType, final String caption, int count, String imgUrl,Bitmap bm){
        Log.d(TAG, "uploadNewPhoto: attempting to upload new photo.");
        FilePaths filePaths = new FilePaths();
        //case 1 new photo
        if(photoType.equals(mContext.getString(R.string.new_photo))){
            Log.d(TAG, "uploadNewPhoto: uploading new photo");

            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE+"/"+userId+"/photo"+(count+1));

            //convert image url to bitmap
            if(bm == null){
                bm = ImageManager.getBitmap(imgUrl);
            }
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Uri firebaseUrl = taskSnapshot.getDownloadUrl();

                    Toast.makeText(mContext, "photo upload Success", Toast.LENGTH_SHORT).show();
                    //add the new photo to photos node and  user_photo_node
                    addPhotoToDatabase(caption, firebaseUrl.toString());
                    //navigate to the main feed so the user can see their photo
                    Intent intent = new Intent(mContext, HomeActivity.class);
                    mContext.startActivity(intent);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: photo upload failed");
                    Toast.makeText(mContext, "photo upload failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 *  taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if(progress - 15 > mPhotoloadProgress){
                        Toast.makeText(mContext, "photo upload progress "+String.format("%.0f",progress)+"%", Toast.LENGTH_SHORT).show();
                        mPhotoloadProgress = progress;
                    }
                    Log.d(TAG, "onProgress:upload progress "+progress+"% done");
                }
            });
        }
        //case 2 new profile photo
        else if(photoType.equals(mContext.getString(R.string.profile_photo))){

            Log.d(TAG, "uploadNewPhoto: uploading new profile photo");

            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE+"/"+userId+"/profile_photo");

            //convert image url to bitmap
            if(bm == null){
                bm = ImageManager.getBitmap(imgUrl);
            }            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Uri firebaseUrl = taskSnapshot.getDownloadUrl();
                    Toast.makeText(mContext, "photo upload Success", Toast.LENGTH_SHORT).show();
                    // insert into user account settings node
                    setProfilePhoto(firebaseUrl.toString());

                    ((AccountSettingActivity)mContext).setViewPager(
                            ((AccountSettingActivity)mContext).pagerAdapter
                                    .getFragmentNumber(mContext.getString(R.string.edit_profile))
                    );


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: photo upload failed");
                    Toast.makeText(mContext, "photo upload failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 *  taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if(progress - 15 > mPhotoloadProgress){
                        Toast.makeText(mContext, "photo upload progress "+String.format("%.0f",progress)+"%", Toast.LENGTH_SHORT).show();
                        mPhotoloadProgress = progress;
                    }
                    Log.d(TAG, "onProgress:upload progress "+progress+"% done");
                }
            });
        }
    }

    private void setProfilePhoto(String url) {
        Log.d(TAG, "setProfilePhoto: setting new profile image "+url);

        mRef.child(mContext.getString(R.string.dbname_user_account_setting))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo)).setValue(url);


    }

    private String getTimestamp(){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss'Z'", Locale.CANADA);
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Karachi"));
            return sdf.format(new Date());
        }

        private void addPhotoToDatabase(String caption, String url){
            Log.d(TAG, "addPhotoToDatabase: adding photo to database.");

            String tags = StringManipulation.getTags(caption);
            String newPhotoKey = mRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
            Photo photo = new Photo();
            photo.setCaption(caption);
            photo.setDate_created(getTimestamp());
            photo.setImage_path(url);
            photo.setTags(tags);
            photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
            photo.setPhoto_id(newPhotoKey);

            //insert into database
            mRef.child(mContext.getString(R.string.dbname_user_photos))
                    .child(FirebaseAuth.getInstance().getCurrentUser()
                            .getUid()).child(newPhotoKey).setValue(photo);
            mRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);

        }


    public int getImageCount(DataSnapshot dataSnapshot){
        int count = 0;
        for(DataSnapshot  ds : dataSnapshot.child(mContext.getString(R.string.dbname_user_photo))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .getChildren()){
            count++;
        }
        return count;
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

        mRef.child(mContext.getString(R.string.dbname_user_account_setting)).child(userId).setValue(userAccountSetting  );

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
            if (ds.getKey().equals(mContext.getString(R.string.dbname_user_account_setting))) {
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
            mRef.child(mContext.getString(R.string.dbname_user_account_setting))
                .child(userId)
                .child(mContext.getString(R.string.filed_display_name))
                .setValue(displayName);}
        if(description != null){
            mRef.child(mContext.getString(R.string.dbname_user_account_setting))
                .child(userId)
                .child(mContext.getString(R.string.filed_website))
               .setValue(description);}
        if(website != null){
            mRef.child(mContext.getString(R.string.dbname_user_account_setting))
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
        mRef.child(mContext.getString(R.string.dbname_user_account_setting))
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
