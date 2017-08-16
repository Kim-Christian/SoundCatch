package com.soundcatch.soundcatch;

import android.graphics.drawable.Drawable;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kim-Christian on 2017-04-18.
 */

public class PlayFragment extends Fragment {

    private View bottomSheet;
    private ImageView hideShow;
    private ImageView play;
    private ImageView delete;
    private Drawable playIcon;
    private Drawable pauseIcon;
    private File file;
    private BottomSheetBehavior bottomSheetBehavior;
    int collapsed = BottomSheetBehavior.STATE_COLLAPSED;
    int expanded = BottomSheetBehavior.STATE_EXPANDED;
    int state = collapsed;
    int hidden = 0;
    private boolean isExpanded;
    private View layout;
    OnClickListener mClickCallback;
    OnStateChangedListener mSheetCallback;
    private boolean isPlaying = false;
    private MediaPlayer mediaPlayer;
    private double currentTime = 0;
    private double finalTime = 0;
    private Handler myHandler = new Handler();
    private SeekBar seekbar;
    private TextView title, time, length;
    private double prevTime = -1;
    private Recording recording;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_play, container, false);
        bottomSheet = layout.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(hidden);
        bottomSheetBehavior.setState(state);

        title = (TextView) layout.findViewById(R.id.title);
        time = (TextView) layout.findViewById(R.id.time);
        length = (TextView) layout.findViewById(R.id.length);
        seekbar = (SeekBar) layout.findViewById(R.id.seekBar);

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        bottomSheet = layout.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(hidden);
        bottomSheetBehavior.setState(state);

        try {
            mClickCallback = (OnClickListener) getActivity();
            mSheetCallback = (OnStateChangedListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement Listener");
        }

        hideShow = (ImageView) layout.findViewById(R.id.hideShow);
        play = (ImageView) layout.findViewById(R.id.play);
        delete = (ImageView) layout.findViewById(R.id.delete);

        playIcon = getResources().getDrawable(R.drawable.play, getContext().getTheme());
        pauseIcon = getResources().getDrawable(R.drawable.pause, getContext().getTheme());

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (isExpanded)
                    bottomSheet.post(new Runnable() {
                        @Override
                        public void run() {
                            state = expanded;
                            mSheetCallback.onSheetExpand();
                        }
                    });
                else
                    bottomSheet.post(new Runnable() {
                        @Override
                        public void run() {
                            state = collapsed;
                            mSheetCallback.onSheetCollapse();
                        }
                    });
                bottomSheetBehavior.setState(state);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        bottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        hideShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isExpanded) {
                    isExpanded = false;
                    state = collapsed;
                } else {
                    isExpanded = true;
                    state = expanded;
                }
                bottomSheetBehavior.setState(state);
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying) {
                    try {
                        play();
                    } catch (Exception e)
                    {}
                } else {
                    pause();
                }
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickCallback.onDeleteClicked(recording);
            }
        });
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    updateTime();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public interface OnClickListener {
        public void onHideShowClicked();
        public void onDeleteClicked(Recording recording);
    }

    public interface OnStateChangedListener {
        public void onSheetExpand();
        public void onSheetCollapse();
    }

    public String getTitle() {
        return title.getText().toString();
    }

    private void setPeekHeight(int height) {
        bottomSheetBehavior.setPeekHeight(height);
    }

    public View getBottomSheet() {
        return bottomSheet;
    }

    public BottomSheetBehavior getBottomSheetBehavior() {
        return bottomSheetBehavior;
    }

    public boolean sheetIsExpanded() {
        return isExpanded;
    }

    public ImageView getHide() {
        return hideShow;
    }

    public void setBottomSheetState(int state) {
        this.state = state;
        if (state == expanded) {
            isExpanded = true;
        } else if (state == collapsed) {
            isExpanded = false;
        }
        bottomSheetBehavior.setState(state);
    }

    public void expandSheet() {
        setBottomSheetState(expanded);
    }

    public void collapseSheet() {
        setBottomSheetState(collapsed);
    }

    private void updateSheet(String newTitle) {
        title.setText(newTitle);
    }

    public void setData(Recording recording) {
        this.recording = recording;
        String title = recording.getName();
        file = recording.getFile();
        updateSheet(title);
        mediaPlayer = MediaPlayer.create(getContext(), Uri.parse(file.getPath()));
    }

    public void resetData() {
        recording = null;
        file = null;
        updateSheet("Title");
        setPlayButton();
        mediaPlayer = null;
        finalTime = 0;
        seekbar.setMax(0);
    }

    private void setPlayButton() {
        isPlaying = false;
        play.setImageDrawable(playIcon);
    }

    private void setPauseButton() {
        isPlaying = true;
        play.setImageDrawable(pauseIcon);
    }

    private void updateTime() {
        time.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes((long) currentTime),
                TimeUnit.MILLISECONDS.toSeconds((long) currentTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                currentTime)))
        );
    }

    private void play() {
        mediaPlayer.start();
        currentTime = mediaPlayer.getCurrentPosition();
        finalTime = mediaPlayer.getDuration();
        seekbar.setMax((int) finalTime);

        length.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                finalTime)))
        );

        updateTime();
        seekbar.setProgress((int) currentTime);
        myHandler.postDelayed(UpdateSongTime, 100);
        setPauseButton();
    }

    public void pause() {
        mediaPlayer.pause();
        myHandler.removeCallbacks(UpdateSongTime);
        setPlayButton();
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            currentTime = mediaPlayer.getCurrentPosition();
            updateTime();
            seekbar.setProgress((int)currentTime);
            myHandler.postDelayed(this, 100);
            if (currentTime == prevTime && currentTime > 0) {
                pause();
            }
            prevTime = currentTime;
        }
    };
}