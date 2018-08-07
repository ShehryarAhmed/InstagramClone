package com.example.tx.instagram;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.tx.instagram.model.Photo;
import com.example.tx.instagram.utils.BottomNavigationViewHelper;
import com.example.tx.instagram.utils.SquareImageView;
import com.example.tx.instagram.utils.UniversalImageLoader;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class ViewPostFragment extends Fragment {

    private static final String TAG = "ViewPostFragment";
    public ViewPostFragment(){
        super();
        setArguments(new Bundle());
    }
   //widgets
    private SquareImageView mPostImage;
    private BottomNavigationViewEx bottomNavigationViewEx;

    //vars
    private Photo mPhoto;
    private int mActivityNumber = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        mPostImage = (SquareImageView) view.findViewById(R.id.post_image);
        bottomNavigationViewEx = (BottomNavigationViewEx) view.findViewById(R.id.bottom_navigation_bar);
        try{
            mPhoto = getPhotoFromBundle();
            UniversalImageLoader.setImage(mPhoto.getImage_path(), mPostImage, null, "");
            mActivityNumber = getActivityNumBundle();
            setUpBottomNavigationView();
        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: Null pointer Exception:"+e.getMessage() );
        }
        return  view;
    }
    /**
     * retrieve the activity number from the incoming bundle from profile Activity interface
     * @return
     */
    private int getActivityNumBundle(){
        Log.d(TAG, "getActivityNumBundle: arguments "+getArguments());

        Bundle bundle = this.getArguments();
        if(bundle != null){
            return bundle.getInt(getString(R.string.activity_number));
        }
        else {
            return 0;
        }
    }
    /**
     * retrieve the photo from the incoming bundle from profileActivity interface
     * @return
     */

    private Photo getPhotoFromBundle(){
        Log.d(TAG, "getPhotoFromBundle: arguments: "+getArguments());
        Bundle bundle =   this.getArguments();
        if (bundle != null){
            return  bundle.getParcelable(getString(R.string.photo));
        }
        else{
            return null;
        }
    }

    /**
     * Bottom navigation
     */
    private void setUpBottomNavigationView(){

        Log.d(TAG, "setUpBottomNavigationView: ");
        BottomNavigationViewHelper.setUpBottomNavigation(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(getActivity(),getActivity(),bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(mActivityNumber);
        menuItem.setChecked(true);
    }
}
