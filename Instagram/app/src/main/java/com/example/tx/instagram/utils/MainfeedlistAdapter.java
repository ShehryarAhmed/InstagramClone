package com.example.tx.instagram.utils;

import android.content.Context;
import android.gesture.GestureLibraries;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.model.ByteArrayLoader;
import com.example.tx.instagram.R;
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

import java.util.List;

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
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

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
                            getLikeString();
                        }

                        //case 2 then user has not liked the photo
                        else if(!mHolder.likeByCurrentUser){
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
}
