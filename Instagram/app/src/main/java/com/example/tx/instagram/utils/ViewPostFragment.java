package com.example.tx.instagram.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tx.instagram.Profile.AccountSettingActivity;
import com.example.tx.instagram.R;
import com.example.tx.instagram.model.Like;
import com.example.tx.instagram.model.Photo;
import com.example.tx.instagram.model.User;
import com.example.tx.instagram.model.UserAccountSetting;
import com.example.tx.instagram.utils.BottomNavigationViewHelper;
import com.example.tx.instagram.utils.FirebaseMethod;
import com.example.tx.instagram.utils.GridImageAdapter;
import com.example.tx.instagram.utils.SquareImageView;
import com.example.tx.instagram.utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ViewPostFragment extends Fragment {

    private static final String TAG = "ViewPostFragment";

    public ViewPostFragment() {
        super();
        setArguments(new Bundle());
    }

    //widgets
    private SquareImageView mPostImage;
    private BottomNavigationViewEx bottomNavigationViewEx;

    private TextView mbackLabel;
    private TextView mCaption;
    private TextView mUsername;
    private TextView mTimeStamp;
    private TextView mLikes;

    private ImageView mbackArrow;
    private ImageView mEllipsee;
    private ImageView mHeartRed;
    private ImageView mHeartWhite;
    private ImageView mCommentBubble;
    private ImageView mProfileImage;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private FirebaseDatabase mFirebaseDBRef;
    private DatabaseReference mRef;
    private FirebaseMethod firebaseMethod;

    //vars
    private Photo mPhoto;
    private int mActivityNumber = 0;
    private String photoUsername;
    private String mPhotoUrl;
    private UserAccountSetting mUserAccountSettings;
    private GestureDetector mGestureDetector;
    private Heart heart;
    private Boolean mLikedByCurrentUser;
    private StringBuilder mUsers;
    private String mLikesString = "";

    public interface OnCommentThreadSelectedListner{
        void onCommentThreadSelectedListner(Photo photo);
    }

    OnCommentThreadSelectedListner mOnCThreadSelectedListner;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        widgetsInit(view);



        setUpFirebaseAuth();
        setUpBottomNavigationView();
//        getPhotoDetails();
////        setupWidgets();
        return view;
    }


    private void getLikeString(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUsers = new StringBuilder();

                for (DataSnapshot singleSnapShot : dataSnapshot.getChildren()){

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference
                            .child(getString(R.string.dbname_user))
                            .orderByChild(getString(R.string.filed_user_id))
                            .equalTo(singleSnapShot.getValue(Like.class).getUser_id());

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot singleSnapShot : dataSnapshot.getChildren()) {
                                Log.d(TAG, "onDataChange: found like : " + singleSnapShot.getValue(User.class).getUsername());

                                mUsers.append(singleSnapShot.getValue(User.class).getUsername());
                                mUsers.append(",");
                                }
                                String[] splitUser = mUsers.toString().split(",");

                                if (mUsers.toString().contains(mUserAccountSettings.getUsername()+",")){
                                    mLikedByCurrentUser = true;
                                }else{
                                     mLikedByCurrentUser = false;
                                }
                                int length = splitUser.length;
                                if(length == 1){
                                    mLikesString = "Liked by "+splitUser[0];
                                }
                                else if(length == 2){
                                    mLikesString = "Liked by "+splitUser[0]
                                            + " and "+splitUser[1];
                                }
                                else if(length == 3){
                                    mLikesString = "Liked by "+splitUser[0]
                                            + ", "+splitUser[1]
                                            + " and "+splitUser[2];

                                }
                                else if(length == 4){
                                    mLikesString = "Liked by "+splitUser[0]
                                            + ", "+splitUser[1]
                                            + ", "+splitUser[2]
                                            + " and "+splitUser[3];
                                }
                                else if(length > 4){
                                    mLikesString = "Liked by "+splitUser[0]
                                            + ", "+splitUser[1]
                                            + ", "+splitUser[2]
                                            + " and "+(splitUser.length - 3)+" others";
                                }

                            Log.d(TAG, "onDataChange: like String "+mLikesString);
                            setupWidgets();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
                if(!dataSnapshot.exists()){
                    mLikesString= "";
                    mLikedByCurrentUser = false;
                    setupWidgets();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    public class Gesturelistenier extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(getString(R.string.dbname_photos))
                    .child(mPhoto.getPhoto_id())
                    .child(getString(R.string.field_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapShot : dataSnapshot.getChildren()){
                        String keyID = singleSnapShot.getKey();

                        //case 1 then user already liked the photo
                        if(mLikedByCurrentUser &&
                                singleSnapShot.getValue(Like.class).getUser_id()
                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                            mRef.child(getString(R.string.dbname_photos))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            mRef.child(getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            heart.toggleLike();
                            getLikeString();
                        }

                        //case 2 then user has not liked the photo
                        else if(!mLikedByCurrentUser){
                            //add new like
                            addNewLike();
                            break;
                        }

                    }
                    if (!dataSnapshot.exists()){
                        addNewLike();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return true;

        }
    }


    private void addNewLike(){
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeID = mRef.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mRef.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        mRef.child(getString(R.string.dbname_user_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        heart.toggleLike();
        getLikeString();

    }

    private void getPhotoDetails() {
        Log.d(TAG, "getPhotoDetails: retrieving photo details.");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_account_setting))
                .orderByChild(getString(R.string.filed_user_id))
                .equalTo(mPhoto.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    mUserAccountSettings = singleSnapshot.getValue(UserAccountSetting.class);
                }
//                setupWidgets();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query cancelled");
            }
        });
    }

    /**
     * retrieve the activity number from the incoming bundle from profile Activity interface
     *
     * @return
     */
    private int getActivityNumBundle() {
        Log.d(TAG, "getActivityNumBundle: arguments " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getInt(getString(R.string.activity_number));
        } else {
            return 0;
        }
    }

    /**
     * retrieve the photo from the incoming bundle from profileActivity interface
     *
     * @return
     */

    private Photo getPhotoFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.photo));
        } else {
            return null;
        }
    }

    /**
     * Bottom navigation
     */
    private void setUpBottomNavigationView() {

        Log.d(TAG, "setUpBottomNavigationView: ");
        BottomNavigationViewHelper.setUpBottomNavigation(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(getActivity(), getActivity(), bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
    }

    private void widgetsInit(View view) {
        mPostImage = (SquareImageView) view.findViewById(R.id.post_image);
        bottomNavigationViewEx = (BottomNavigationViewEx) view.findViewById(R.id.bottom_navigation_bar);

        mbackArrow = (ImageView) view.findViewById(R.id.backArraw);
        mEllipsee = (ImageView) view.findViewById(R.id.ivEllipses);
        mHeartRed = (ImageView) view.findViewById(R.id.image_heart_red);
        mHeartWhite = (ImageView) view.findViewById(R.id.image_heart_outline);
        mCommentBubble   = (ImageView) view.findViewById(R.id.comment_bubble);
        mProfileImage = (ImageView) view.findViewById(R.id.profile_photo);

        mbackLabel = (TextView) view.findViewById(R.id.tvBackLabel);
        mCaption = (TextView) view.findViewById(R.id.image_captions);
        mUsername = (TextView) view.findViewById(R.id.username);
        mTimeStamp = (TextView) view.findViewById(R.id.image_time_posted);
        mLikes = (TextView) view.findViewById(R.id.image_likes);

        heart = new Heart(mHeartWhite,mHeartRed);
        mGestureDetector = new GestureDetector(getActivity(), new Gesturelistenier());

        try {
            mPhoto = getPhotoFromBundle();
            UniversalImageLoader.setImage(mPhoto.getImage_path(), mPostImage, null, "");
            mActivityNumber = getActivityNumBundle();
            getPhotoDetails();
            getLikeString();
        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: Null pointer Exception:" + e.getMessage());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mOnCThreadSelectedListner = (OnCommentThreadSelectedListner) getActivity();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException"+e.getMessage() );
        }
    }

    /**
     * Return a string re
     *
     * @return
     */
    private String getTimeStampDifference() {
        Log.d(TAG, "getTimeStampDifference: getting timestamp difference ");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Karachi"));//google android list of timezones'
        Date today = c.getTime();
        sdf.format(today);
        Date timeStamp;
        final String photoTimeStamp = mPhoto.getDate_created();
        try {
            timeStamp = sdf.parse(photoTimeStamp);
            difference = String.valueOf(Math.round(((today.getTime() - timeStamp.getTime()) / 1000 / 60 / 60 / 24)));
        } catch (ParseException e) {
            Log.d(TAG, "getTimeStampDifference: ");
            difference = "0";
        }
        return difference;
    }

    private void setupWidgets() {
        String timestampDiff = getTimeStampDifference();

        if (!timestampDiff.equals("0")) {
            mTimeStamp.setText(timestampDiff + " DAYS AGO");
        } else {
            mTimeStamp.setText("TODAY");
        }

        UniversalImageLoader.setImage(mUserAccountSettings.getProfile_photo(), mProfileImage, null, "");
        mUsername.setText(mUserAccountSettings.getUsername());
        mLikes.setText(mLikesString);
        mCaption.setText(mPhoto.getCaption());

        mbackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigate back");
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mCommentBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: on comment bubble");
                mOnCThreadSelectedListner.onCommentThreadSelectedListner(mPhoto);

            }
        });

        if(mLikedByCurrentUser){
            mHeartWhite.setVisibility(View.GONE);
            mHeartRed.setVisibility(View.VISIBLE);
            mHeartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    Log.d(TAG, "onTouch: red heart detected");
                    return mGestureDetector.onTouchEvent(motionEvent);
                }
            });
        }else{
            mHeartWhite.setVisibility(View.VISIBLE);
            mHeartRed.setVisibility(View.GONE);
            mHeartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    Log.d(TAG, "onTouch: red heart detected");
                    return mGestureDetector.onTouchEvent(motionEvent);
                }
            });
        }

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

}
