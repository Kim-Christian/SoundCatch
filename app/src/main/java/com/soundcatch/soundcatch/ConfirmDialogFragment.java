package com.soundcatch.soundcatch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.view.MotionEvent;
import android.view.WindowManager;

/**
 * Created by Kim-Christian on 2017-04-19.
 */

public class ConfirmDialogFragment extends DialogFragment{

    private OnClickListener mCallback;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String action = getArguments().getString("Action");
        final String message = getArguments().getString("Message");
        final String positive = getArguments().getString("Positive");
        final Recording object = getArguments().getParcelable("Object");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mCallback.onPositiveButtonClick(action, object);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mCallback.onNegativeButtonClick();
                    }
                });

        try {
            mCallback = (ConfirmDialogFragment.OnClickListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnSelectedListener");
        }

        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mCallback.onCancel();
    }

    public interface OnClickListener {
        public void onPositiveButtonClick(String action, Recording object);
        public void onNegativeButtonClick();
        public void onCancel();
    }
}
