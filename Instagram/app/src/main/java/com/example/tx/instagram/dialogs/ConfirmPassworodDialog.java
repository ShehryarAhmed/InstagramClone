package com.example.tx.instagram.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tx.instagram.R;

import java.security.PrivateKey;

/**
 * Created by Admin on 7/25/2018.
 */

public class ConfirmPassworodDialog extends DialogFragment {

    private static final String TAG = "ConfirmPassworodDialog";

    public  interface  OnConfirmPasswordListener{
        public void onConfirmPassword(String password);
    }
    OnConfirmPasswordListener mOnConfirmPassworodListener;

    private EditText mPassword;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialg_confirm_password,container,false);
        Log.d(TAG, "onCreateView: started");

        mPassword = (EditText) view.findViewById(R.id.confirm_password);
        TextView confirmDialog = (TextView) view.findViewById(R.id.dialogConfirm);

        confirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String password = mPassword.getText().toString().trim();

                Log.d(TAG, "onClick: capture password and confirming.");
                Log.d(TAG, "onClick: capture password is "+password);

                if(!password.equals("")){
                    mOnConfirmPassworodListener.onConfirmPassword(password);
                    getDialog().dismiss();
                }
                else{
                    Toast.makeText(getContext(), "you must enter password", Toast.LENGTH_SHORT).show();
                }
            }
        });
        TextView cancelDialog = (TextView) view.findViewById(R.id.dialogCancel);
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the dialog");
                getDialog().dismiss();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mOnConfirmPassworodListener = (OnConfirmPasswordListener) getTargetFragment();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException "+e.getMessage());
        }
    }
}
