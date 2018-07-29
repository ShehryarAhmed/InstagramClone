package com.example.tx.instagram.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.tx.instagram.R;
import com.example.tx.instagram.utils.FilePaths;
import com.example.tx.instagram.utils.FileSearch;
import com.example.tx.instagram.utils.GridImageAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

/**
 * Created by Admin on 7/27/2018.
 */

public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";

    private static final int NUM_GRID_COLUMNS = 3;
    private static final String mAppend = "file:/";

    private GridView gridView;
    private ImageView galleryIamge;
    private ImageView shareClose;
    private ProgressBar mProgressBar;
    private Spinner directorySpinner;
    private TextView nextScreen;
    private ArrayList<String> directories;
    private String mSelectedImage;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery,container,false);
        Log.d(TAG, "onCreateView: started");
        initWidgets(view);
        init();
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
                Intent intent = new Intent(getActivity(),NextActivity.class);
                intent.putExtra(getString(R.string.selected_imgae),mSelectedImage);
                startActivity(intent);
            }
        });
    }

    private void init(){
        FilePaths filePaths = new FilePaths();

        if(FileSearch.getDirectoryPaths(filePaths.PICTURE) != null){
            directories = FileSearch.getDirectoryPaths(filePaths.PICTURE);
        }
        ArrayList<String> directoryNames = new ArrayList<>();
        for (int i = 0; i<directories.size(); i++){
            int index = directories.get(i).lastIndexOf("/");
            String string = directories.get(i).substring(++index);
            directoryNames.add(string);
        }

        directories.add(filePaths.CAMERA);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, directoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directorySpinner.setAdapter(adapter);

        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: selected "+ directories.get(position));

                //setup ur image grid for the directory chosen
                setupGridView(directories.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupGridView(String selectedDir){
        Log.d(TAG, "setupGridView:  directory chosen "+selectedDir);
        final ArrayList<String> imgUrls = FileSearch.getFilePaths(selectedDir);

        //set the grid columns width
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        //user the grid adapter to adapter the images to gridviews
        GridImageAdapter adapter = new GridImageAdapter(getActivity(),R.layout.layout_grid_imageview,mAppend,imgUrls);
        gridView.setAdapter(adapter);
        //
        setImage(imgUrls.get(0),galleryIamge,mAppend);
        mSelectedImage = imgUrls.get(0);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected an item ");
                setImage(imgUrls.get(position),galleryIamge,mAppend);
                mSelectedImage = imgUrls.get(position);

            }
        });
    }

    private void setImage(String imgUrl, ImageView image, String append){
        Log.d(TAG, "setImage: setting image");

        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(append + imgUrl, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                mProgressBar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mProgressBar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                mProgressBar.setVisibility(View.INVISIBLE);

            }
        });
    }
}
