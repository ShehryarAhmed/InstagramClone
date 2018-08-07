package com.example.tx.instagram.Profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.tx.instagram.R;
import com.example.tx.instagram.utils.BottomNavigationViewHelper;
import com.example.tx.instagram.utils.FirebaseMethod;
import com.example.tx.instagram.utils.SectionStatePagerAdapter;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

/**
 * Created by Admin on 7/17/2018.
 */

public class AccountSettingActivity extends AppCompatActivity {

    private static final String TAG = "AccountSettingActivity";
    public static final int ACTIVITY_NUM = 4;

    private Context mContext;
    private ImageView backArrow;
    public SectionStatePagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private RelativeLayout mRelativeLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);
        Log.d(TAG, "onCreate: started");
        mContext = AccountSettingActivity.this;

        viewPager = (ViewPager) findViewById(R.id.container);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.rellayout1);

        setUpSettingList();
        setUpBottomNavigationView();
        setUpFragments();
        getInComingIntent();
        backArrow = (ImageView) findViewById(R.id.backArraw);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private  void setUpSettingList(){
        Log.d(TAG, "setUpSettingList: ");
        ListView listView = (ListView) findViewById(R.id.lvAccountSetting);

        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.edit_profile));
        options.add(getString(R.string.sign_out));

        ArrayAdapter arrayAdapter = new ArrayAdapter(mContext,android.R.layout.simple_list_item_1,options);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                setViewPager(position);
            }
        });
    }

     public void setViewPager(int fragmenNumber){
        mRelativeLayout.setVisibility(View.GONE);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(fragmenNumber);
     }

    private void setUpFragments(){
        pagerAdapter = new SectionStatePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new EditProfileFragment(), getString(R.string.edit_profile));
        pagerAdapter.addFragment(new SignOutFragment(), getString(R.string.sign_out));
    }
    private void setUpBottomNavigationView() {
        Log.d(TAG, "setUpBottomNavigationView: ");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottom_navigation_bar);
        BottomNavigationViewHelper.setUpBottomNavigation(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(AccountSettingActivity.this,this, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    private void getInComingIntent(){
        Intent intent = getIntent();

        Log.d(TAG, "getInComingIntent: new in comming imgurl ");

        if(intent.hasExtra(getString(R.string.selected_image)) || intent.hasExtra(getString(R.string.selected_bitmap)) ){

        //if there is an imageurl attached as an extra  then it was chosen from the gallery/photo fragment
            Log.d(TAG, "getIncomingIntent: New incoming imgUrl");
        if(intent.getStringExtra(getString(R.string.return_to_fragment)).equals(getString(R.string.edit_profile))) {

                if (intent.hasExtra(getString(R.string.selected_image))){
                   //set the new profile picture
                    FirebaseMethod firebaseMethods = new FirebaseMethod(AccountSettingActivity.this);
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo),null, 0,
                            intent.getStringExtra(getString(R.string.selected_image)),null);
                }
                else if(intent.hasExtra(getString(R.string.selected_bitmap))){

                    FirebaseMethod firebaseMethods = new FirebaseMethod(AccountSettingActivity.this);
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo),null, 0,null,
                            (Bitmap) intent.getParcelableExtra(getString(R.string.selected_image)));
                }
            }
        }
        if (intent.hasExtra(getString(R.string.calling_activity))){
            Log.d(TAG, "getInComingIntent: recevied incominng intent  from");
            setViewPager(pagerAdapter.getFragmentNumber(getString(R.string.edit_profile)));
        }
    }
}
