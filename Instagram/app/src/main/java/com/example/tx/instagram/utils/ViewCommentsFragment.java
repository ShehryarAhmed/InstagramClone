package com.example.tx.instagram.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.tx.instagram.R;
import com.example.tx.instagram.model.Comment;
import com.example.tx.instagram.model.Photo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ViewCommentsFragment extends Fragment {

    private static final String TAG = "ViewCommentsFragment";

    public ViewCommentsFragment() {
        super();
        setArguments(new Bundle());
    }
    //widgets
    private ImageView mBackArrow, mCheckMark;
    private EditText mComment;
    private ListView mListView;

    //vars
    private Photo mPhoto;
    private ArrayList<Comment> mComments;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListner;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRef;

    private Context mContext;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.fragment_view_comment, container, false);

        Log.d(TAG, "onCreateView: check mark");
         mBackArrow = (ImageView) view.findViewById(R.id.ic_back_arrow);
         mCheckMark = (ImageView) view.findViewById(R.id.ivPostComment);
         mComment = (EditText) view.findViewById(R.id.et_comment);
         mListView = (ListView) view.findViewById(R.id.comment_list);
         mComments = new ArrayList<>();
         mContext =  getActivity();



         try{
             mPhoto = getPhotoFromBundle();
             setUpFirebaseAuth();
         }catch (NullPointerException e){
             Log.e(TAG, "onCreateView: NullPointerException "+e.getMessage() );
         }



         return view;
    }

    private void  setUpWidgets(){

        CommentListAdapter adapter = new CommentListAdapter(mContext,
                R.layout.layout_comment,mComments);
        mListView.setAdapter(adapter);

        mCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mComment.getText().toString().equals("")){
                    Log.d(TAG, "onClick: attempting to submit new comment");
                    addNewComment(mComment.getText().toString());
                    mComment.setText("");
                    closeKeyBoard();
                }
                else{
                    Toast.makeText(getActivity(), "you can't post a blank ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back");
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
    private void closeKeyBoard() {
        View view = getActivity().getCurrentFocus();
        if(view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private Photo getPhotoFromBundle() {
        Log.d(TAG, "getPhotoFromBundle: arguments: " + getArguments());
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.photo));
        } else {
            return null;
        }

    }

     /*
     ***********************************************Firebase******************************************************
     */
    /**
     * setup Firebase auth object
     */

    private void setUpFirebaseAuth(){

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference();
        mAuthListner =  new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if user is logged in
                if(user != null){
                    Log.d(TAG, "onAuthStateChanged: "+user.getUid());
                }
                else{
                    Log.d(TAG, "signout: ");
                }
            }
        };

        if(mPhoto.getComments().size() == 0){
            mComments.clear();
            Comment firstComment = new Comment();
            firstComment.setComment(mPhoto.getCaption());
            firstComment.setUser_id(mPhoto.getUser_id());
            firstComment.setDate_created(mPhoto.getDate_created());
            mComments.add(firstComment);
            mPhoto.setComments(mComments);
            setUpWidgets();
        }


        mRef.child(mContext.getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.field_comments))
                .addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                Query query = mRef
                                        .child(mContext.getString(R.string.dbname_photos))
                                        .orderByChild(mContext.getString(R.string.field_photo_id))
                                        .equalTo(mPhoto.getPhoto_id());
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
//                                          photos.add(singleSnapshot.getValue(Photo.class));
                                            Photo photo = new Photo();
                                            Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                                            photo.setCaption(objectMap.get(mContext.getString(R.string.field_caption)).toString());
                                            photo.setDate_created(objectMap.get(mContext.getString(R.string.field_date_created)).toString());
                                            photo.setTags(objectMap.get(mContext.getString(R.string.field_tags)).toString());
                                            photo.setPhoto_id(objectMap.get(mContext.getString(R.string.field_photo_id)).toString());
                                            photo.setUser_id(objectMap.get(mContext.getString(R.string.field_user_id)).toString());
                                            photo.setImage_path(objectMap.get(mContext.getString(R.string.field_image_path)).toString());

                                            mComments.clear();
                                            Comment firstComment = new Comment();
                                            firstComment.setComment(mPhoto.getCaption());
                                            firstComment.setUser_id(mPhoto.getUser_id());
                                            firstComment.setDate_created(mPhoto.getDate_created());
                                            mComments.add(firstComment);

                                            for (DataSnapshot dsnapshot : singleSnapshot.child(mContext.getString(R.string.field_comments)).getChildren()){
                                                Comment comment  = new Comment();
                                                comment.setUser_id(dsnapshot.getValue(Comment.class).getUser_id());
                                                comment.setComment(dsnapshot.getValue(Comment.class).getComment());
                                                comment.setDate_created(dsnapshot.getValue(Comment.class).getDate_created());
                                                mComments.add(comment);
                                            }
                                            photo.setComments(mComments);
                                            mPhoto = photo;
                                            setUpWidgets();


//                    List<Like> likesList = new ArrayList<Like>();
//                    for (DataSnapshot dsnapshot : singleSnapshot.child(getString(R.string.field_likes)).getChildren()){
//                        Like like  = new Like();
//                        like.setUser_id(dsnapshot.getValue(Like.class).getUser_id());
//                        likesList.add(like);
//                    }

                                        }


                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.d(TAG, "onCancelled: Query cancelled");
                                    }
                                });

                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

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

        if(mAuthListner != null){
            mAuth.removeAuthStateListener(mAuthListner);
        }
    }


    private void addNewComment(String newComment){
        Log.d(TAG, "addNewComment: adding new comment: " + newComment);

        String commentID = mRef.push().getKey();

        Comment comment = new Comment();
        comment.setComment(newComment);
        comment.setDate_created(getTimestamp());
        comment.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        //insert into photos node
        mRef.child(getString(R.string.dbname_photos))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);

        //insert into user_photos node
        mRef.child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.field_comments))
                .child(commentID)
                .setValue(comment);

    }
    private String getTimestamp(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//        sdf.setTimeZone(TimeZone.getTimeZone("As/Pacific"));
        return sdf.format(new Date());
    }
}