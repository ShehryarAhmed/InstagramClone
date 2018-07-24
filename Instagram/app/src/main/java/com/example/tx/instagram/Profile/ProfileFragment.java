package com.example.tx.instagram.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tx.instagram.Login.LoginActivity;
import com.example.tx.instagram.R;
import com.example.tx.instagram.model.User;
import com.example.tx.instagram.model.UserAccountSetting;
import com.example.tx.instagram.model.UserSettings;
import com.example.tx.instagram.utils.BottomNavigationViewHelper;
import com.example.tx.instagram.utils.FirebaseMethod;
import com.example.tx.instagram.utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment{

    public static final int ACTIVITY_NUM = 4;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private FirebaseDatabase mFirebaseDBRef;
    private DatabaseReference mRef;
    private FirebaseMethod firebaseMethod;

    private static final String TAG = "ProfileFragment";
    private TextView mPosts;
    private TextView mFollower;
    private TextView mFollowing;
    private TextView mDisplayName;
    private TextView mUserName;
    private TextView mWebsite;
    private TextView mDescription;
    private ImageView mProfileMenu;
    private ProgressBar mProgressBar;
    private CircleImageView  mProfilePhoto;
    private GridView gridView;
    private Toolbar toolbar;
    private BottomNavigationViewEx bottomNavigationViewEx;
    private Context mContext;
    private TextView editProfileTv;



    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mDisplayName = (TextView) view.findViewById(R.id.display_name);
        mUserName = (TextView) view.findViewById(R.id.username);
        mWebsite = (TextView) view.findViewById(R.id.website);
        mDescription = (TextView) view.findViewById(R.id.description);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mPosts = (TextView) view.findViewById(R.id.tvPosts);
        mFollower = (TextView) view.findViewById(R.id.tvFollowers);
        mFollowing = (TextView) view.findViewById(R.id.tvFollowing);
        mProgressBar = (ProgressBar) view.findViewById(R.id.profile_progressBar);
        gridView = (GridView) view.findViewById(R.id.gridView);
        mProfileMenu = (ImageView) view.findViewById(R.id.profile_menu);
        bottomNavigationViewEx = (BottomNavigationViewEx) view.findViewById(R.id.bottom_navigation_bar);

        editProfileTv = (TextView) view.findViewById(R.id.textEditProfile);

        editProfileTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: edit profile activity");
                Intent intent = new Intent(getActivity(),AccountSettingActivity.class);
                intent.putExtra(getString(R.string.calling_activity),getString(R.string.profile_fragment));
                startActivity(intent);
            }
        });

        mContext = getActivity();
        firebaseMethod = new FirebaseMethod(mContext);
        setUpFirebaseAuth();
        Log.d(TAG, "onCreateView: started");

        setUpBottomNavigationView();
        setupToolbar();
        return view;
    }

        private void setUpBottomNavigationView(){

        Log.d(TAG, "setUpBottomNavigationView: ");
        BottomNavigationViewHelper.setUpBottomNavigation(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    private void setupToolbar(){

        ((ProfileActivity)getActivity()).setSupportActionBar(toolbar);
        mProfileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating too account setting");
                startActivity(new Intent(mContext,AccountSettingActivity.class));
            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings){
        Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database ");
        User user = userSettings.getUser();
        UserAccountSetting setting = userSettings.getUserAccountSetting();

        UniversalImageLoader.setImage(setting.getProfile_photo(), mProfilePhoto, null,"");

        mDisplayName.setText(setting.getDisplay_name());
        mUserName.setText(setting.getUsername());
        mWebsite.setText(setting.getWebsite());
        mDescription.setText(setting.getDescription());
        mPosts.setText(""+setting.getPosts());
        mFollowing.setText(""+setting.getFollowing());
        mFollower.setText(""+setting.getFollowers());

        mProgressBar.setVisibility(View.GONE);
    }

    /*
     ***********************************************Firebase******************************************************
     */
    /**
     * setup Firebase auth object
     */

    private void setUpFirebaseAuth(){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDBRef = FirebaseDatabase.getInstance();
        mRef = mFirebaseDBRef.getReference();
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
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //retrieve user information from database
                setProfileWidgets(firebaseMethod.getUserSettings(dataSnapshot));
                //retrieve image  from database


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
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
        if(mAuthListner != null){
            mAuth.removeAuthStateListener(mAuthListner);
        }
    }

    }
