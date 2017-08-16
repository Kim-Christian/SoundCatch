package com.soundcatch.soundcatch;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kim-Christian on 2017-04-17.
 */

public class RecordingsFragment extends Fragment {

    private List<Recording> recordings;
    private LinearLayout recordingList;
    private Folder recordingsFolder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_recordings, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Folder appFolder = new Folder(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
        appFolder.claimFileName("Recordings", "Other");
        recordingsFolder = new Folder(appFolder, "Recordings");
        recordingList = (LinearLayout) getView().findViewById(R.id.recording_list);

        recordings = new ArrayList<>();
        loadRecordings();
    }

    private RecordingCard addRecordingCard(Recording recording) {
        final RecordingCard recordingCard = new RecordingCard(getContext(), recording);
        recordingList.addView(recordingCard.getCardView());

        return recordingCard;
    }

    public void removeRecordingCard(RecordingCard recordingCard) {
        recordingList.removeView(recordingCard.getCardView());
    }

    private void loadRecordings() {
        File[] files = recordingsFolder.getFiles();
        for (File file : files) {
            Recording recording = new Recording(recordingsFolder, file);
            recordings.add(recording);
            addRecordingCard(recording);
        }
    }
}
