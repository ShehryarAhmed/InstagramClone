package com.example.tx.instagram.Share;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.tx.instagram.R;
import com.example.tx.instagram.utils.BottomNavigationViewHelper;
import com.example.tx.instagram.utils.Permissions;
import com.example.tx.instagram.utils.SectionPagerAdapter;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class ShareActivity extends AppCompatActivity {

    private static final String TAG = "ShareActivity";

    //Constants
    public static final int ACTIVITY_NUM = 2;
    public static final int VERIFY_PERMISSION_REQUEST = 1;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Log.d(TAG, "onCreate: START");

//        setUpBottomNavigationView();
        if (checkPermissionArrays(Permissions.PERMISSIONS)) {
            setUpViewPager();
        } else {
            verifyPermission(Permissions.PERMISSIONS);
        }
        getTask();
    }

    /**
     * return the current tab number
     * 0 = GalleryFragment
     * 1 = PhotoFragment
     * @return
     */
    public int getCurrentTabNumber(){
        return mViewPager.getCurrentItem();
    }

    private void setUpViewPager(){
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GalleryFragment());
        adapter.addFragment(new PhotoFragment());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText(getString(R.string.gallery));
        tabLayout.getTabAt(1).setText(getString(R.string.photo));
    }

    /*
    get task method
     */
    public int getTask(){
        Log.d(TAG, "getTask: TASK : "+getIntent().getFlags());
        return getIntent().getFlags();
    }
    /**
     * verify all the permissions passed array
     * @param permission
     */
    public void verifyPermission(String[] permission){
        Log.d(TAG, "verifyPermission:  verifying permission");

        ActivityCompat.requestPermissions(
                ShareActivity.this,
                permission,
                VERIFY_PERMISSION_REQUEST
        );
    }
    /**
     * Check an array of permission
     * @param permission
     * @return
     */
    public boolean checkPermissionArrays(String[] permission) {
        Log.d(TAG, "checkPermissionArrays: checking permission array");

        for (int i = 0; i <permission.length; i++ ){
            String check = permission[i];
            if(!checkPermissions(check)){return false; }
        }
        return true;
    }

    /**
     * Check a single Permission is it has been verified
     * @param permission
     * @return
     */
    public boolean checkPermissions(String permission) {
        Log.d(TAG, "checkPermissionArrays: Checking  permission: " + permission);
        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this, permission);
        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermissionArrays: \n Permission was not granted for " + permission);
            return false;
        } else {
            Log.d(TAG, "checkPermissionArrays: \n Permission was granted for " + permission);
            return true;
        }
    }


    private void setUpBottomNavigationView(){
        Log.d(TAG, "setUpBottomNavigationView: ");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottom_navigation_bar);
        BottomNavigationViewHelper.setUpBottomNavigation(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(getApplicationContext(),bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
