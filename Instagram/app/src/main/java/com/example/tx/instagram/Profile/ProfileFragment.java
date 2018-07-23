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

import com.example.tx.instagram.R;
import com.example.tx.instagram.utils.BottomNavigationViewHelper;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment{

    public static final int ACTIVITY_NUM = 4;

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



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mDisplayName = (TextView) view.findViewById(R.id.display_name);
        mUserName = (TextView) view.findViewById(R.id.username);
        mWebsite = (TextView) view.findViewById(R.id.website);
        mDescription = (TextView) view.findViewById(R.id.description);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mPosts = (TextView) view.findViewById(R.id.tvPosts);
        mFollower = (TextView) view.findViewById(R.id.tvFollowers);
        mFollowing = (TextView) view.findViewById(R.id.tvFollowing);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        gridView = (GridView) view.findViewById(R.id.gridView);
        mProfileMenu = (ImageView) view.findViewById(R.id.profile_menu);
        bottomNavigationViewEx = (BottomNavigationViewEx) view.findViewById(R.id.bottom_navigation_bar);

        mContext = getActivity();
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

    }
