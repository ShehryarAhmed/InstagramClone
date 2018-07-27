package com.example.tx.instagram.Share;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.tx.instagram.R;

/**
 * Created by Admin on 7/27/2018.
 */

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";

    private GridView gridView;
    private ImageView galleryIamge;
    private ImageView shareClose;
    private ProgressBar mProgressBar;
    private Spinner directorySpinner;
    private TextView nextScreen;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery,container,false);
        Log.d(TAG, "onCreateView: started");
        return view;
    }

    private void initWidgets(View view){
        Log.d(TAG, "initWidgets: started");
        galleryIamge = (ImageView) view.findViewById(R.id.galleryImageView);
        shareClose = (ImageView) view.findViewById(R.id.iv_CloseShare);
        gridView = (GridView) view.findViewById(R.id.gridView);
        directorySpinner = (Spinner) view.findViewById(R.id.spinnerDirectory);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        nextScreen = (TextView) view.findViewById(R.id.tvNext);

        shareClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: closing the gallery fragment");
                getActivity().finish();
            }
        });
        nextScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: navigate the final share screen ");
            }
        });




    }
}
