package com.example.tx.instagram.Like;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.tx.instagram.R;
import com.example.tx.instagram.utils.BottomNavigationViewHelper;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class LikeActivity extends AppCompatActivity {
    private static final String TAG = "LikesActivity";
    public static final int ACTIVITY_NUM = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: started");
        setUpBottomNavigationView();
    }

    private void setUpBottomNavigationView(){
        Log.d(TAG, "setUpBottomNavigationView: ");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottom_navigation_bar);
        BottomNavigationViewHelper.setUpBottomNavigation(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(LikeActivity.this,this,bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
