package com.soundcatch.soundcatch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Kim-Christian on 2017-04-19.
 */

public class EditDialogFragment extends DialogFragment{

    private OnClickListener mCallback;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String object = getArguments().getString("Object");
        final EditText textInput = new EditText(getContext());
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(object + " name")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mCallback.onPositiveButtonClick(object, textInput.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mCallback.onNegativeButtonClick(object);
                    }
                })
                .setView(textInput);

        final AlertDialog dialog = builder.create();
        dialog.show();
        final Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setEnabled(false);
        textInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    positiveButton.setEnabled(false);
                } else {
                    positiveButton.setEnabled(true);
                }
            }
        });

        try {
            mCallback = (EditDialogFragment.OnClickListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement OnHeadlineSelectedListener");
        }

        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mCallback.onCancel();
    }

    public interface OnClickListener {
        public void onPositiveButtonClick(String object, String input);
        public void onNegativeButtonClick(String object);
        public void onCancel();
    }
}