package com.example.tx.instagram.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tx.instagram.R;
import com.example.tx.instagram.Share.ShareActivity;
import com.example.tx.instagram.dialogs.ConfirmPassworodDialog;
import com.example.tx.instagram.model.User;
import com.example.tx.instagram.model.UserAccountSetting;
import com.example.tx.instagram.model.UserSettings;
import com.example.tx.instagram.utils.FirebaseMethod;
import com.example.tx.instagram.utils.UniversalImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment implements ConfirmPassworodDialog.OnConfirmPasswordListener {


    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG, "onConfirmPassword: got password");
        AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(), password);
        Log.d(TAG, "onConfirmPassword: "+credential);

        //*********************************Promt the user to re-provide their sign in credential
        mAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "user re-authenticated.");

                    //check to see if the email is not already exists in database
                    mAuth.fetchProvidersForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                            if (task.isSuccessful()) {
                                try {
                                    if (task.getResult().getProviders().size() == 1) {
                                        Log.d(TAG, "onComplete: that email is already in use");
                                        Toast.makeText(getActivity(), "That email is already in use", Toast.LENGTH_SHORT).show();
                                    } else if (task.getResult().getProviders().size() == 0) {
                                        Log.d(TAG, "onComplete: that email is available ");
                                        ///the email is available so update it
                                        mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d(TAG, "User email address updated");
                                                            firebaseMethod.updateEmail(mEmail.getText().toString());
                                                            Toast.makeText(getActivity(), "email updated ", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                } catch (NullPointerException e) {
                                    Log.d(TAG, "onComplete: NullPointerException" + e.getMessage());
                                }
                            } else {
                                Log.d(TAG, "onComplete: re-authenticate failed ");
                            }
                        }
                    });
                }
                else{
                    Log.d(TAG, "onComplete: authentication failed");
                    Toast.makeText(getActivity(), "Authentication failed enter correct password", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private static final String TAG = "EditProfileFragment";
    //    private ImageView mProfilePhoto;
    private ImageView mBackArrow;
    private ImageView mCheckMark;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private FirebaseDatabase mFirebaseDBRef;
    private DatabaseReference mRef;
    private FirebaseMethod firebaseMethod;

    //editProfile Fragment widgets
    private EditText mDisplayName;
    private EditText mUserName;
    private EditText mWebsite;
    private EditText mDescription;
    private EditText mEmail;
    private EditText mPhoneNumber;

    private TextView mChangeProfilePhoto;
    private CircleImageView mProfilePhoto;

    private String userID;
    private UserSettings mUserSetting;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        initFragmentWidgets(view);

        setUpFirebaseAuth();

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        mCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to save changes");
                saveProfileSettings();
            }
        });
//        setProfileImg();
        return view;
    }

    private void setProfileImg() {
        Log.d(TAG, "setProfileImg: setting profile image");
        String imgUrl = "www.androidcentral.com/sites/androidcentral.com/files/styles/xlarge/public/article_images/2016/08/ac-lloyd.jpg?itok=bb72IeLf";
        UniversalImageLoader.setImage(imgUrl, mProfilePhoto, null, "https://");

    }

    private void saveProfileSettings() {
        final String displayname = mDisplayName.getText().toString();
        final String username = mUserName.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
        final long phoneNumber = Long.parseLong(mPhoneNumber.getText().toString());

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!mUserSetting.getUser().getUsername().equals(username)) {
                    checkIfUserExists(username);
                }
                //case 2 the user changed thier user email
                if (!mUserSetting.getUser().getEmail().equals(email)) {
                    //step1 Reauthenticate Confirm the password and email
                    ConfirmPassworodDialog dialog = new ConfirmPassworodDialog();
                    dialog.show(getFragmentManager(), getString(R.string.confirmPassword));
                    dialog.setTargetFragment(EditProfileFragment.this, 1);

                    // Step 2 check if the email already is registered
                    // - fetch ProvidersForEmail(String email)
                    //Step 3 change  the email
                    // -submit  the new email  to the database and authentication
                }
                /**
                 * Change the rest of the settings that do not required uniqueness
                 */
                if(!mUserSetting.getUserAccountSetting().getDisplay_name().equals(displayname)){
                    //update display name
                    firebaseMethod.updateUserAccountSettings(displayname, null, null,0);
                }
                if(!mUserSetting.getUserAccountSetting().getWebsite().equals(website)){
                    //update display website
                    firebaseMethod.updateUserAccountSettings(null, null, website,0);
                }
                if(!mUserSetting.getUserAccountSetting().getDescription().equals(description)){
                    //update display description
                    firebaseMethod.updateUserAccountSettings(null, description, null,0);
                }
                if(!mUserSetting.getUserAccountSetting().getProfile_photo().equals(phoneNumber)){
                    //update display phoneNumber
                    firebaseMethod.updateUserAccountSettings(displayname, null, null,phoneNumber);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * check is param username already exists in the database
     *
     * @param username
     */

    private void checkIfUserExists(final String username) {
        Log.d(TAG, "checkIfUserExists: checking if " + username + " already exists");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user))
                .orderByChild(getString(R.string.filed_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    //add the username
//                    Log.d(TAG, "checkIfUserExists: FOUND A MATCH : "+singalSnapshot.getValue(User.class).getUsername());
                    firebaseMethod.updateUsername(username);
                    Toast.makeText(getActivity(), "saved username", Toast.LENGTH_SHORT).show();

                }
                for (DataSnapshot singalSnapshot : dataSnapshot.getChildren()) {
                    if (singalSnapshot.exists()) {
                        Log.d(TAG, "checkIfUserExists: FOUND A MATCH : " + singalSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(getActivity(), "The user name is already exits", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void setProfileWidgets(UserSettings userSettings) {
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database :" + userSettings.toString());
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database :" + userSettings.getUserAccountSetting().getDisplay_name());

        mUserSetting = userSettings;
        UserAccountSetting settings = userSettings.getUserAccountSetting();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");

        mDisplayName.setText(settings.getDisplay_name());
        mUserName.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(userSettings.getUser().getEmail());
        mPhoneNumber.setText(String.valueOf(userSettings.getUser().getPhone_number()));
        mPhoneNumber.setText(String.valueOf(userSettings.getUser().getPhone_number()));




    }
    /*
     ***********************************************Firebase******************************************************
     */

    /**
     * setup Firebase auth object
     */

    private void setUpFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDBRef = FirebaseDatabase.getInstance();
        mRef = mFirebaseDBRef.getReference();
        userID = mAuth.getCurrentUser().getUid();
        mAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if user is logged in
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged: " + user.getUid());
                } else {
                    Log.d(TAG, "signout: ");
                }
            }
        };
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //retrieve user information from database
                setProfileWidgets(firebaseMethod.getUserSettings(dataSnapshot));
                //retrieve image  from database


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListner);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListner != null) {
            mAuth.removeAuthStateListener(mAuthListner);
        }
    }

    private void initFragmentWidgets(View view) {
        firebaseMethod = new FirebaseMethod(getActivity());
        mDisplayName = (EditText) view.findViewById(R.id.display_name);
        mUserName = (EditText) view.findViewById(R.id.username);
        mWebsite = (EditText) view.findViewById(R.id.display_url);
        mDescription = (EditText) view.findViewById(R.id.display_desc);
        mEmail = (EditText) view.findViewById(R.id.display_email);
        mPhoneNumber = (EditText) view.findViewById(R.id.display_phonenumber);
        mChangeProfilePhoto = (TextView) view.findViewById(R.id.changeProfilePhoto);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mBackArrow = (ImageView) view.findViewById(R.id.backArraw);
        mCheckMark = (ImageView) view.findViewById(R.id.saveChanges);

        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: change profile photo");

                Intent intent = new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//3242442
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });
    }

}
