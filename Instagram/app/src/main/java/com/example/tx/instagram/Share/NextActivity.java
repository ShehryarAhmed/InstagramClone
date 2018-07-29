package com.example.tx.instagram.Share;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.tx.instagram.R;

public class NextActivity extends AppCompatActivity {
    private static final String TAG = "NextActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        Log.d(TAG, "onCreate: setart"+    getIntent().getStringExtra(getString(R.string.selected_imgae))
        );
    }
}
