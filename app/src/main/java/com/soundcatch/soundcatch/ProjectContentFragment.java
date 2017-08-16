package com.soundcatch.soundcatch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kim-Christian on 2017-04-22.
 */

public class ProjectContentFragment extends Fragment {

    private Folder folder;
    private List<Recording> recordings;
    private LinearLayout contentList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_project_content, container, false);
        contentList = (LinearLayout) rootView.findViewById(R.id.project_content_list);
        folder = getArguments().getParcelable("Folder");
        System.out.println(folder.getName());
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recordings = new ArrayList<>();
        System.out.println("*********************");
        loadRecordings();
    }

    private RecordingCard addRecordingCard(Recording recording) {
        final RecordingCard recordingCard = new RecordingCard(getContext(), recording);
        contentList.addView(recordingCard.getCardView());

        return recordingCard;
    }

    private void loadRecordings() {
        File[] files = folder.getFiles();
        for (File file : files) {
            Recording recording = new Recording(folder, file);
            System.out.println(recording.getName());
            recordings.add(recording);
            addRecordingCard(recording);
        }
    }
}
