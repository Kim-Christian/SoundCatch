package com.soundcatch.soundcatch;

/**
 * Created by Kim-Christian on 2017-04-17.
 */

import android.content.ClipData;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.DragEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.soundcatch.soundcatch.R.id.pager;

public class MainActivity extends FragmentActivity implements ProjectCard.OnDropListener, PlayFragment.OnClickListener, PlayFragment.OnStateChangedListener, RecordingCard.OnDragListener, RecordingCard.OnClickListener, ConfirmDialogFragment.OnClickListener {

    private static final int NUM_PAGES = 3;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private boolean recordFragmentCreated = false;
    private RecordFragment recordFragment;
    private PlayFragment playFragment;
    private Drawable rec;
    private Drawable pause;
    private boolean isRecording = false;
    private FloatingActionButton recFAB;
    private FloatingActionButton addFAB;
    int collapsed = BottomSheetBehavior.STATE_COLLAPSED;
    int expanded = BottomSheetBehavior.STATE_EXPANDED;
    private CoordinatorLayout layout;
    private int shortSnackbar = Snackbar.LENGTH_SHORT;
    private int longSnackbar = Snackbar.LENGTH_LONG;
    private int indefinateSnackbar = Snackbar.LENGTH_INDEFINITE;
    private int selectedPage;
    private int pageScrollState;
    private static final int stateIdle = ViewPager.SCROLL_STATE_IDLE;
    private static final int stateDragging = ViewPager.SCROLL_STATE_DRAGGING;
    private static final int stateSettling = ViewPager.SCROLL_STATE_SETTLING;
    private Snackbar activeSnackbar;
    private String[] pageTitles;
    private View bottomDragLayer;
    private View leftDragLayer;
    private View rightDragLayer;
    private ClipData.Item item;
    private Folder appFolder;
    private Folder recordingsFolder;
    private PitchFragment pitchFragment;
    private ProjectsFragment projectsFragment;
    private RecordingsFragment recordingsFragment;
    ScrollView scrollView;
    private boolean deleteDrag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appFolder = new Folder(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
        appFolder.claimFileName("Projects", "Other");
        appFolder.claimFileName("Recordings", "Other");
        recordingsFolder = new Folder(appFolder, "Recordings");

        mPager = (ViewPager) findViewById(pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        pageTitles = new String[]{"Pitch", "Projects", "Recordings"};

        recordFragment = new RecordFragment();
        playFragment = new PlayFragment();
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.main_content, recordFragment);
        ft.add(R.id.main_content, playFragment).commit();

        initPaging();

        layout = (CoordinatorLayout) findViewById(R.id.main_content);
        myDragEventListener mDragListen = new myDragEventListener();
        layout.setOnDragListener(mDragListen);

        bottomDragLayer = findViewById(R.id.bottomDragLayer);
        leftDragLayer = findViewById(R.id.leftDragLayer);
        rightDragLayer = findViewById(R.id.rightDragLayer);
        layout.removeView(bottomDragLayer);
        layout.removeView(leftDragLayer);
        layout.removeView(rightDragLayer);

        rec = getDrawable(R.drawable.record);
        pause = getDrawable(R.drawable.pause);
        recFAB = (FloatingActionButton) findViewById(R.id.recFAB);
        addFAB = (FloatingActionButton) findViewById(R.id.addFAB);
        addFABhide();

        recFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!recordFragmentCreated) {
                    recordFragmentCreated = true;
                    System.out.println("create record");
                }
                if (!isRecording) {
                    // Record
                    isRecording = true;
                    recFAB.setImageDrawable(pause);
                    recordFragment.startRec();
                    activeSnackbar = messageSnackbar("Recording", indefinateSnackbar);
                } else {
                    // Pause
                    isRecording = false;
                    recFAB.setImageDrawable(rec);
                    activeSnackbar = messageSnackbar("Saving", indefinateSnackbar);
                    Recording latestRecording = recordFragment.pauseRec();
                    updateViewPager();
                    activeSnackbar = latestRecSnackbar(latestRecording);
                    mPager.setCurrentItem(0);
                }
            }
        });

        addFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                projectsFragment.createProject();
                messageSnackbar("New project created", shortSnackbar);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (playFragment.sheetIsExpanded()) {
            playFragment.collapseSheet();
        } else if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(0, true);
        }
    }

    private void updateFragments() {
        pitchFragment = new PitchFragment();
        projectsFragment = new ProjectsFragment();
        recordingsFragment = new RecordingsFragment();
    }

    private ScreenSlidePagerAdapter updatePagerAdapter() {
        ScreenSlidePagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(pitchFragment);
        pagerAdapter.addFragment(projectsFragment);
        pagerAdapter.addFragment(recordingsFragment);
        return pagerAdapter;
    }

    private ViewPager setViewPager(ScreenSlidePagerAdapter pagerAdapter) {
        ViewPager viewPager = (ViewPager) findViewById(pager);
        viewPager.setAdapter(pagerAdapter);
        return viewPager;
    }

    private void initPaging() {
        updateFragments();
        ScreenSlidePagerAdapter pagerAdapter = updatePagerAdapter();
        ViewPager viewPager = setViewPager(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int newPosition) {
                selectedPage = newPosition;
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) { }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                pageScrollState = arg0;
                if (pageScrollState == stateIdle && selectedPage == 1) {
                    addFABshow();
                } else {
                    addFABhide();
                }
            }
        });
    }

    public void recFABshow() {
        recFAB.show();
    }

    public void recFABhide() {
        recFAB.hide();
    }

    public void addFABshow() { addFAB.show(); }

    public void addFABhide() { addFAB.hide(); }

    private void showRecording(Recording recording) {
        System.out.println("Show recording!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*************************'");
        playFragment.setData(recording);
        System.out.println(recording.getName());
        System.out.println(recording.getParent().getName());
        playFragment.expandSheet();
    }

    public Snackbar messageSnackbar(String message, int length) {
        final Snackbar mySnackbar = Snackbar.make(layout, message, length);
        mySnackbar.show();
        mySnackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {

            }
        });
        return mySnackbar;
    }

    public Snackbar latestRecSnackbar(final Recording latestRecording) {
        final Snackbar mySnackbar = Snackbar.make(layout, "Latest recording", indefinateSnackbar);
        mySnackbar.setAction("Show", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecording(latestRecording);
            }
        });
        mySnackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {

            }
        });
        //customizeSnackbar(mySnackbar);
        mySnackbar.show();
        return mySnackbar;
    }

    private void customizeSnackbar(Snackbar snackbar) {
        snackbar.setActionTextColor(Color.CYAN);
        TextView textView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
    }

    @Override
    public void onPositiveButtonClick(String action, Recording object) {
        if (action.equals("Delete")) {
            String name = object.getName();
            object.delete();
            updateViewPager();
            mPager.setCurrentItem(0);
            playFragment.collapseSheet();
            messageSnackbar(name + " deleted", shortSnackbar);
        }
    }

    @Override
    public void onNegativeButtonClick() {

    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onSheetExpand() {
        recFABhide();
        addFABhide();
        try {
            activeSnackbar.dismiss();
        } catch (Exception e) {}
    }

    @Override
    public void onSheetCollapse() {
        recFABshow();
        if (isRecording) {
            activeSnackbar = messageSnackbar("Recording", indefinateSnackbar);
        }
    }

    @Override
    public void onDragStart(CardView view) {

    }

    @Override
    public void onDragEnter(CardView view) {

    }

    @Override
    public void onDragLocation(CardView view) {

    }

    @Override
    public void onDragExit(CardView view) {

    }

    @Override
    public void onDragDrop(CardView view, String dragData) {

    }

    @Override
    public void onDragEnd(CardView view) {

    }

    @Override
    public void onCardClick(Recording recording) {
        try {
            playFragment.pause();
        } catch (Exception e) {}
        playFragment.setData(recording);
        playFragment.expandSheet();
    }

    @Override
    public void onResume() {
        super.onResume();
        scrollView = projectsFragment.getScrollView();
        updateViewPager();
    }

    @Override
    public void onDropListener(Project project) {
        String file = item.getText().toString();
        String projectName = project.getName();
        Folder destFolder = new Folder(project.getFolder(), "Inbox");
        String destFolderName = destFolder.getName();
        recordingsFolder.moveFile(file, destFolder);
        updateViewPager();
        messageSnackbar("Moved to " + destFolderName + " in " + projectName, longSnackbar);
    }

    private void updateViewPager() {
        int currentItem = mPager.getCurrentItem();
        int pages = mPagerAdapter.getCount();
        for (int i = 0; i < pages; i++) {
            mPager.setCurrentItem(i);
        }
        mPager.setCurrentItem(currentItem);
    }

    @Override
    public void onHideShowClicked() {

    }

    @Override
    public void onDeleteClicked(Recording recording) {
        confirmDialog("Delete", "Delete " + recording.getName() + "?", "Delete", recording);
    }

    private void confirmDialog(String action, String message, String positive, Recording object) {
        Bundle bundleOne = new Bundle();
        bundleOne.putString("Action", action);
        bundleOne.putString("Message", message);
        bundleOne.putString("Positive", positive);
        bundleOne.putParcelable("Object", object);
        DialogFragment dialog = new ConfirmDialogFragment();
        dialog.setArguments(bundleOne);
        dialog.show(getSupportFragmentManager(), action);
    }

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragments = new ArrayList<Fragment>();

        public ScreenSlidePagerAdapter(FragmentManager manager) {
            super(manager);
        }

        public void addFragment(Fragment fragment) {
            mFragments.add(fragment);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public CharSequence getPageTitle (int position) {
            try {
                return pageTitles[position];
            } catch (Exception e) { return ""; }
        }
    }

    private int dpToPx(float dp) {
        return Math.round(dp * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    protected class myDragEventListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {

            final int action = event.getAction();
            int currentPage = mPager.getCurrentItem();
            switch(action) {

                case DragEvent.ACTION_DRAG_ENTERED:
                    if (currentPage == 2) {
                        layout.addView(bottomDragLayer);
                        layout.addView(leftDragLayer);
                    } else if (currentPage == 1) {
                        layout.addView(rightDragLayer);
                    }
                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:
                    float x = event.getX();
                    float y = event.getY();
                    if (currentPage == 2 && y > 2250) {
                        deleteDrag = true;
                    } else if (currentPage == 2 && x < 150) {
                        deleteDrag = false;
                        mPager.setCurrentItem(1);
                        layout.removeView(bottomDragLayer);
                        layout.removeView(leftDragLayer);
                        layout.addView(rightDragLayer);
                    } else if (currentPage == 1 && x > 1300) {
                        deleteDrag = false;
                        mPager.setCurrentItem(2);
                        layout.removeView(rightDragLayer);
                        layout.addView(bottomDragLayer);
                        layout.addView(leftDragLayer);
                    } else if (currentPage == 1) {
                        deleteDrag = false;
                        try {
                            final float bottomPos = scrollView.getBottom();
                            final float topPos = scrollView.getTop();
                            final float height = bottomPos - topPos;
                            final float middlePos = height / 2 + topPos;
                            final float upScrollMaxPos = middlePos - height / 4;
                            final float downScrollMinPos = middlePos + height / 4;
                            if (y < upScrollMaxPos) {
                                int scrollFactor = (int)(y - upScrollMaxPos) / 5;
                                scrollView.smoothScrollBy(0, scrollFactor);
                            } else if (y > downScrollMinPos) {
                                int scrollFactor = (int)(y - downScrollMinPos) / 5;
                                scrollView.smoothScrollBy(0, scrollFactor);
                            }
                        } catch(Exception e) {System.out.println(e);}
                    }
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    System.out.println("exit");
                    layout.removeView(bottomDragLayer);
                    layout.removeView(leftDragLayer);
                    layout.removeView(rightDragLayer);
                    System.out.println("exit done");
                    return true;

                case DragEvent.ACTION_DROP:
                    item = event.getClipData().getItemAt(0);
                    layout.removeView(bottomDragLayer);
                    layout.removeView(leftDragLayer);
                    layout.removeView(rightDragLayer);
                    String file = item.getText().toString();
                    Recording recording = null;
                    try {
                        recording = new Recording(recordingsFolder, recordingsFolder.getFile(file));
                    } catch (Exception e) {}
                    if (deleteDrag) {
                        confirmDialog("Delete", "Delete " + file + "?", "Delete", recording);
                    }
                    return true;

                default:
                    System.out.println("unknown action");
                    break;
            }

            return false;
        }
    }
}