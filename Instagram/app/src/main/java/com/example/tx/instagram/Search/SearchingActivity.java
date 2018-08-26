package com.example.tx.instagram.Search;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.example.tx.instagram.Profile.ProfileActivity;
import com.example.tx.instagram.R;
import com.example.tx.instagram.Share.ShareActivity;
import com.example.tx.instagram.model.User;
import com.example.tx.instagram.utils.BottomNavigationViewHelper;
import com.example.tx.instagram.utils.Permissions;
import com.example.tx.instagram.utils.UserListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchingActivity extends AppCompatActivity {
    private static final String TAG = "SearchingActivity";

    public static final int ACTIVITY_NUM = 1;

    private Context  mContext = SearchingActivity.this;
    //widgets
    private EditText mSearchParam;
    private ListView mListView;
    private UserListAdapter mAdapter;
    //vars
    private List<User> mUserList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serach);
        mSearchParam = (EditText) findViewById(R.id.search);
        mListView = (ListView) findViewById(R.id.listView);

        Log.d(TAG, "onCreate: started");

        hideSoftKeyboaord();
        setUpBottomNavigationView();
        initTextlistner();
    }

    private void initTextlistner(){
        Log.d(TAG, "init Text listener : initializing");

        mUserList = new ArrayList<>();

        mSearchParam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = mSearchParam.getText().toString().toLowerCase(Locale.getDefault());
                searchForMatch(text);
            }
        });
    }
    

    private void searchForMatch(String keyword){
        Log.d(TAG, "searchForMatch: searching for a match : "+keyword);
        mUserList.clear();
        //update the users list
        if(keyword.length() == 0){

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
                        updateUserList();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    private void updateUserList(){
        Log.d(TAG, "updateUserList: updating user list");

        mAdapter = new UserListAdapter(SearchingActivity.this,R.layout.layout_user_listitme,mUserList);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: select user: "+mUserList.get(position).toString());

                //navigate to profile activity
                Intent intent = new Intent(SearchingActivity.this, ProfileActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.serach_activity));
            }
        });
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
