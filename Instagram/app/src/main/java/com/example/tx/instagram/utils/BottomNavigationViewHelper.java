package com.example.tx.instagram.utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.example.tx.instagram.Like.LikeActivity;
import com.example.tx.instagram.MainActivity;
import com.example.tx.instagram.Profile.ProfileActivity;
import com.example.tx.instagram.R;
import com.example.tx.instagram.Search.SearchingActivity;
import com.example.tx.instagram.Share.ShareActivity;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class BottomNavigationViewHelper {
    private static final String TAG = "BottomNavigation";

    public static void setUpBottomNavigation(BottomNavigationViewEx bottomNavigation){
        Log.d(TAG, "setUpBottomNavigation: ");

        bottomNavigation.enableAnimation(false);
        bottomNavigation.enableItemShiftingMode(false);
        bottomNavigation.enableShiftingMode(false);
        bottomNavigation.setTextVisibility(false);
    }
    public static void enableNavigation(final Context context,BottomNavigationViewEx view){
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.ic_house:
                        Intent intent1 = new Intent(context, MainActivity.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent1);
                        break;
                    case R.id.ic_serach:
                        Intent intent2 = new Intent(context, SearchingActivity.class);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent2);
                        break;
                    case R.id.ic_circle:
                        Intent intent3 = new Intent(context, ShareActivity.class);
                        intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent3);
                        break;
                    case R.id.ic_alert:
                        Intent intent4 = new Intent(context, LikeActivity.class);
                        intent4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent4);
                        break;
                    case R.id.ic_android:
                        Intent intent5 = new Intent(context, ProfileActivity.class);
                        intent5.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent5);
                        break;
                }
                return false;
            }
        });
    }
}
