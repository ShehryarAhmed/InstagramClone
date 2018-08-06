package com.example.tx.instagram.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.tx.instagram.Profile.AccountSettingActivity;
import com.example.tx.instagram.R;
import com.example.tx.instagram.utils.Permissions;

/**
 * Created by Admin on 7/27/2018.
 */

public class PhotoFragment extends Fragment {
    private static final String TAG = "PhotoFragment";
    //constant
    private static final int PHOTO_FRAGMENT_NUM = 1;
    private static final int GALLERY_FRAGMENT_NUM = 2;
    private static final int  CAMERA_REQUEST_CODE = 5;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo,container,false);
        Log.d(TAG, "onCreateView: started");

        Button btnLaunchCamera = (Button) view.findViewById(R.id.btnLaunchCamera);
        btnLaunchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: launching camera.");

                if(((ShareActivity)getActivity()).getCurrentTabNumber() == PHOTO_FRAGMENT_NUM){
                    if(((ShareActivity)getActivity()).checkPermissions(Permissions.CAMERA_PERMISSION[0])){
                        Log.d(TAG, "onClick: starting camera");
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
                    }else{
                        Intent intent = new Intent(getActivity(), ShareActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            }
        });
        return view;
    }

    private boolean isRootTask(){
//        Log.d(TAG, "isRootTask: "+((ShareActivity)getActivity()).getTask());
        if(((ShareActivity)getActivity()).getTask() == 0 ){
            return true;
        }
        else {
            return false;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//
        if (requestCode == CAMERA_REQUEST_CODE){
            Log.d(TAG, "onActivityResult: done taking photo.");
            Log.d(TAG, "onActivityResult: Attempting to navigate to final share screen");

            Bitmap bitmap;
            bitmap = (Bitmap) data.getExtras().get("data");
            if(isRootTask()){

                try{
                    Log.d(TAG, "onActivityResult: received now bitmapfrom camera "+bitmap);

                    Intent intent = new Intent(getActivity(),NextActivity.class);
                    intent.putExtra(getString(R.string.selected_image),bitmap);
                    startActivity(intent);
                }catch (NullPointerException e){
                    Log.d(TAG, "onActivityResult: Nullpointer Exception");
                }

            }
            else{
                try{
                    Log.d(TAG, "onActivityResult: received now bitmapfrom camera "+bitmap);

                    Intent intent = new Intent(getActivity(),AccountSettingActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap),bitmap);
                    intent.putExtra(getString(R.string.return_to_fragment),getString(R.string.edit_profile));
                    startActivity(intent);
                    getActivity().finish();

                }catch (NullPointerException e){
                    Log.d(TAG, "onActivityResult: Nullpointer Exception");
                }
            }

        }

        if(isRootTask()){
        }else{
        }
    }

}
