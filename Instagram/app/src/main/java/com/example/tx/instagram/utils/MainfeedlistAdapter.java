package com.example.tx.instagram.utils;

import android.content.Context;
import android.gesture.GestureLibraries;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.model.ByteArrayLoader;
import com.example.tx.instagram.Home.HomeActivity;
import com.example.tx.instagram.R;
import com.example.tx.instagram.model.Comment;
import com.example.tx.instagram.model.Like;
import com.example.tx.instagram.model.Photo;
import com.example.tx.instagram.model.User;
import com.example.tx.instagram.model.UserAccountSetting;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainfeedlistAdapter extends ArrayAdapter<Photo> {

    private static final String TAG = "MainfeedlistAdapter";

    private LayoutInflater mInfalter;
    private int mLayoutResource;
    private Context mContext;
    private DatabaseReference mReference;
    private String currentUsername;

    public MainfeedlistAdapter(@NonNull Context context, int resource, List<Photo> objects) {

        super(context, resource);
        mInfalter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        this.mContext = context;
    }

    static class ViewHolder{

        CircleImageView mProfileImageView;
        String likeString;
        TextView username, timeDetail, likes, comments, caption;
        SquareImageView image;
        ImageView heartRed, heartWhite, comment;

        UserAccountSetting setting = new UserAccountSetting();
        User user = new User();
        StringBuilder users;
        String mLikeString;
        boolean likeByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Photo photo;

    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null){
            convertView = mInfalter.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();

            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.image = (SquareImageView) convertView.findViewById(R.id.post_image);
            holder.heartRed = (ImageView) convertView.findViewById(R.id.image_heart_red);
            holder.heartWhite = (ImageView) convertView.findViewById(R.id.image_heart_outline);
            holder.comment = (ImageView) convertView.findViewById(R.id.image_heart_outline);
            holder.likes = (TextView) convertView.findViewById(R.id.image_likes);
            holder.comments = (TextView) convertView.findViewById(R.id.image_comment_link);
            holder.caption = (TextView) convertView.findViewById(R.id.image_captions);
            holder.timeDetail = (TextView) convertView.findViewById(R.id.image_time_posted);
            holder.mProfileImageView = (CircleImageView) convertView.findViewById(R.id.profile_image);
            holder.heart = new Heart(holder.heartWhite, holder.heartRed);
            holder.photo = getItem(position);
            holder.detector = new GestureDetector(mContext, new Gesturelistenier(holder));
            holder.users = new StringBuilder();

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        // get the current users username (need for checking likes String)
        getCurrentUser();
        //get like string
        getLikeString(holder);
        //set the comment
        List<Comment> comments = getItem(position).getComments();
        holder.comments.setText("View all "+comments.size() +" comments");
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: loading comment thread for "+getItem(position).getPhoto_id() );
                ((HomeActivity)mContext)
            }
        });
        return convertView;
    }

    public class Gesturelistenier extends GestureDetector.SimpleOnGestureListener{

        ViewHolder  mHolder;

        public Gesturelistenier(ViewHolder  holder){
            this.mHolder = holder;
        }
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child(mContext.getString(R.string.dbname_photos))
                    .child(mHolder.photo.getPhoto_id())
                    .child(mContext.getString(R.string.field_likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapShot : dataSnapshot.getChildren()){
                        String keyID = singleSnapShot.getKey();

                        //case 1 then user already liked the photo
                        if(mHolder.likeByCurrentUser &&
                                singleSnapShot.getValue(Like.class).getUser_id()
                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                            mReference.child(mContext.getString(R.string.dbname_photos))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            mReference.child(mContext.getString(R.string.dbname_user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.field_likes))
                                    .child(keyID)
                                    .removeValue();

                            mHolder.heart.toggleLike();
                            getLikeString(mHolder);
                        }

                        //case 2 then user has not liked the photo
                        else if(!mHolder.likeByCurrentUser){
                            //add new like
                            addNewLike(mHolder);
                            break;
                        }

                    }
                    if (!dataSnapshot.exists()){
                        addNewLike(mHolder);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return true;

        }
    }

    private void addNewLike(final ViewHolder holder){
        Log.d(TAG, "addNewLike: adding new like");

        String newLikeID = mReference.push().getKey();
        Like like = new Like();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mReference.child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        mReference.child(mContext.getString(R.string.dbname_user_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes))
                .child(newLikeID)
                .setValue(like);

        holder.heart.toggleLike();
        getLikeString(holder);

    }

    private void getCurrentUser(){

        Log.d(TAG, "getCurrentUser: retrieving user account settings");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_user))
                .orderByChild(mContext.getString(R.string.field_user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapShot : dataSnapshot.getChildren()) {
                    currentUsername = singleSnapShot.getValue(UserAccountSetting.class).getUsername();
                }
                }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void getLikeString(final ViewHolder holder){

        Log.d(TAG, "getLikeString: getting likes string");
        try{

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.dbname_photos))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.field_likes));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.users = new StringBuilder();

                for (DataSnapshot singleSnapShot : dataSnapshot.getChildren()){

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query = reference
                            .child(mContext.getString(R.string.dbname_user))
                            .orderByChild(mContext.getString(R.string.field_user_id))
                            .equalTo(singleSnapShot.getValue(Like.class).getUser_id());

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot singleSnapShot : dataSnapshot.getChildren()) {
                                Log.d(TAG, "onDataChange: found like : " + singleSnapShot.getValue(User.class).getUsername());

                                holder.users.append(singleSnapShot.getValue(User.class).getUsername());
                                holder.users.append(",");
                            }
                            String[] splitUser = holder.users.toString().split(",");

                            if (holder.users.toString().contains(holder.user.getUsername()+ ",")){
                                holder.likeByCurrentUser = true;
                            }else{
                                holder.likeByCurrentUser = false;
                            }
                            int length = splitUser.length;
                            if(length == 1){
                                holder.likeString = "Liked by "+splitUser[0];
                            }
                            else if(length == 2){
                                holder.likeString = "Liked by "+splitUser[0]
                                        + " and "+splitUser[1];
                            }
                            else if(length == 3){
                                holder.likeString = "Liked by "+splitUser[0]
                                        + ", "+splitUser[1]
                                        + " and "+splitUser[2];

                            }
                            else if(length == 4){
                                holder.likeString = "Liked by "+splitUser[0]
                                        + ", "+splitUser[1]
                                        + ", "+splitUser[2]
                                        + " and "+splitUser[3];
                            }
                            else if(length > 4){
                                holder.likeString = "Liked by "+splitUser[0]
                                        + ", "+splitUser[1]
                                        + ", "+splitUser[2]
                                        + " and "+(splitUser.length - 3)+" others";
                            }

                            Log.d(TAG, "onDataChange: like String "+holder.likeString);
                            //setup like string
                            setupLikeString(holder, holder.likeString);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
                if(!dataSnapshot.exists()){
                    holder.likeString= "";
                    holder.likeByCurrentUser = false;
//               //setup like string
                    setupLikeString(holder, holder.likeString);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        }catch (NullPointerException e){
            Log.e(TAG, "getLikeString: Null poiner Exception  : "+e.getMessage() );
            holder.likeString = "";
            holder.likeByCurrentUser = false;
            //setup like string
            setupLikeString(holder, holder.likeString);
        }
    }

    private void setupLikeString(final ViewHolder holder, String likeString){
        Log.d(TAG, "setupLikeString: likes string: "+holder.likeString);

        if (holder.likeByCurrentUser){
            Log.d(TAG, "setupLikeString photo is liked by current user ");
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return holder.detector.onTouchEvent(motionEvent);
                }
            });
        }else{
            Log.d(TAG, "setupLikeString photo is not liked by current user ");
            holder.heartWhite.setVisibility(View.VISIBLE);
            holder.heartRed.setVisibility(View.GONE);
            holder.heartWhite.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return holder.detector.onTouchEvent(motionEvent);
                }
            });
        }
        holder.likes.setText(likeString);
    }

    private String getTimeStampDifference(Photo mPhoto) {
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


}
