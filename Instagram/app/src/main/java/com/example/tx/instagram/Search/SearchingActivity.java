package com.example.tx.instagram.Search;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import com.example.tx.instagram.R;
import com.example.tx.instagram.Share.ShareActivity;
import com.example.tx.instagram.model.User;
import com.example.tx.instagram.utils.BottomNavigationViewHelper;
import com.example.tx.instagram.utils.Permissions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.List;

public class SearchingActivity extends AppCompatActivity {
    private static final String TAG = "SearchingActivity";

    public static final int ACTIVITY_NUM = 1;

    private Context  mContext = SearchingActivity.this;
    //widgets
    private EditText mSearchParam;
    private ListView mListVIew;
    //vars
    private List<User> mUserList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: started");

        hideSoftKeyboaord();
        setUpBottomNavigationView();

    }

    private void searchForMatch(String keyword){
        Log.d(TAG, "searchForMatch: searching for a match : "+keyword);
        mUserList.clear();
        //update the users list
        if(keyword.length() == 0){
            mUserList.clear();
        }else{
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.child(getString(R.string.dbname_user))
                    .orderByChild(getString(R.string.filed_username)).equalTo(keyword);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: found user "+singleSnapshot.getValue(User.class).toString());

                        mUserList.add(singleSnapshot.getValue(User.class));
                        // update the users list view
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    private void hideSoftKeyboaord(){
        if(getCurrentFocus() != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }
    }

    private void setUpBottomNavigationView() {
        Log.d(TAG, "setUpBottomNavigationView: ");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottom_navigation_bar);
        BottomNavigationViewHelper.setUpBottomNavigation(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(SearchingActivity.this,this, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);

    }
}
