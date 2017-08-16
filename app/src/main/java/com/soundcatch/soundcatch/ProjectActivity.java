package com.soundcatch.soundcatch;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.soundcatch.soundcatch.R.id.pager;

/**
 * Created by Kim-Christian on 2017-04-16.
 */

public class ProjectActivity extends AppCompatActivity implements ProjectCard.OnDropListener, PlayFragment.OnClickListener, PlayFragment.OnStateChangedListener, RecordingCard.OnClickListener, RecordingCard.OnDragListener, ConfirmDialogFragment.OnClickListener, EditDialogFragment.OnClickListener {

    private Project project;
    private Folder projectFolder;
    private CoordinatorLayout layout;
    private RelativeLayout actions_layout;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private int selectedPage;
    private int pageScrollState;
    private static final int stateIdle = ViewPager.SCROLL_STATE_IDLE;
    private static final int stateDragging = ViewPager.SCROLL_STATE_DRAGGING;
    private static final int stateSettling = ViewPager.SCROLL_STATE_SETTLING;
    private PlayFragment playFragment;
    private ProjectActivity.ScreenSlidePagerAdapter pagerAdapter;
    private SearchView editView;
    private ActionBar ab;
    private int shortSnackbar = Snackbar.LENGTH_SHORT;
    private int longSnackbar = Snackbar.LENGTH_LONG;
    private int indefinateSnackbar = Snackbar.LENGTH_INDEFINITE;
    private static final int SIZE_NORMAL = 0;
    private static final int SIZE_MINI = 1;
    private List<Folder> lists;
    private Folder listFolder;
    private FloatingActionsMenu menuMultipleActions;
    private int currentItem;
    private CoordinatorLayout snackbar;
    private List<ProjectContentFragment> listFragments;
    private Snackbar activeSnackbar;
    private List<String> pageTitles;
    private View dimLayer;
    //private View dragLayer;
    private View leftDragLayer;
    private View rightDragLayer;
    private View topDragLayer;
    private View bottomDragLayer;
    private int startPage;
    private int dropPage;
    private int currentPage;
    private int previousPage;
    private ClipData.Item item;
    private boolean moveUpDrag = false;
    private boolean deleteDrag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        project = getIntent().getParcelableExtra("Project");
        layout = (CoordinatorLayout) findViewById(R.id.project_content);
        actions_layout = (RelativeLayout) findViewById(R.id.actions_layout);
        snackbar = (CoordinatorLayout) findViewById(R.id.snackbar);
        myDragEventListener mDragListen = new myDragEventListener();
        layout.setOnDragListener(mDragListen);

        pageTitles = new ArrayList<>();
        listFragments = new ArrayList<>();
        lists = new ArrayList<>();
        currentItem = 0;

        mPager = (ViewPager) findViewById(pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        projectFolder = project.getFolder();

        playFragment = new PlayFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.project_content, playFragment).commit();

        initPaging();
        loadLists();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        ab.setTitle(project.getName());

        menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions);

        final com.getbase.floatingactionbutton.FloatingActionButton actionA = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_a);
        final com.getbase.floatingactionbutton.FloatingActionButton actionB = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_b);
        final com.getbase.floatingactionbutton.FloatingActionButton actionC = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_c);

        actionA.setSize(com.getbase.floatingactionbutton.FloatingActionButton.SIZE_MINI);
        actionB.setSize(com.getbase.floatingactionbutton.FloatingActionButton.SIZE_MINI);
        actionC.setSize(com.getbase.floatingactionbutton.FloatingActionButton.SIZE_MINI);

        dimLayer = findViewById(R.id.dim_layer);
        //dragLayer = findViewById(R.id.drag_layer);
        leftDragLayer = findViewById(R.id.leftDragLayer);
        rightDragLayer = findViewById(R.id.rightDragLayer);
        topDragLayer = findViewById(R.id.topDragLayer);
        bottomDragLayer = findViewById(R.id.bottomDragLayer);

        //layout.removeView(dragLayer);
        layout.removeView(leftDragLayer);
        layout.removeView(rightDragLayer);
        layout.removeView(topDragLayer);
        layout.removeView(bottomDragLayer);
        actions_layout.removeView(dimLayer);

        dimLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuMultipleActions.collapse();
            }
        });

        menuMultipleActions.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                actions_layout.addView(dimLayer);
                menuMultipleActions.bringToFront();
            }

            @Override
            public void onMenuCollapsed() {
                actions_layout.removeView(dimLayer);
            }
        });

        actionA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissSnackbar();
                currentItem = mPager.getCurrentItem();
                String object = lists.get(currentItem).getName();
                confirmDialog(object, "Delete " + object + "?", "Delete", null);
            }
        });
        actionB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentItem = mPager.getCurrentItem();
                editDialog("Edit list");
            }
        });
        actionC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentItem = mPager.getCurrentItem();
                editDialog("New list");
            }
        });
    }

    private void dismissSnackbar() {
        try {
            activeSnackbar.dismiss();
        } catch (Exception e) {}
    }

    @Override
    public void onSheetExpand() {

    }

    @Override
    public void onSheetCollapse() {

    }

    public Project getProject() {
        return project;
    }

    private Folder newList(String name) {
        return project.addFolder(name);
    }

    private void loadLists() {
        Folder[] folders = projectFolder.getFolders();
        if (folders.length == 0) {
            folders = new Folder[]{newList("First List")};
        }
        for (Folder list : folders) {
            addListPage(list);
        }
    }

    private ProjectContentFragment loadList(Folder list) {
        Bundle bundleOne = new Bundle();
        bundleOne.putParcelable("Folder", list);
        ProjectContentFragment listFragment = new ProjectContentFragment();
        listFragment.setArguments(bundleOne);
        return listFragment;
    }

    private void addToPager(ProjectContentFragment pageFragment) {
        pagerAdapter.addFragment(pageFragment);
    }

    private void updateLists() {
        listFragments = new ArrayList<>();
        lists = new ArrayList<>();
        pageTitles = new ArrayList<>();
        initPaging();
        loadLists();
    }

    private void initPaging() {
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());

        ViewPager viewPager = (ViewPager) findViewById(pager);
        viewPager.setAdapter(pagerAdapter);
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
                if (pageScrollState == stateIdle) {
                    listFolder = projectFolder.getFolder(mPagerAdapter.getPageTitle(mPager.getCurrentItem()).toString());
                }
                if (pageScrollState == stateIdle && selectedPage == 1) {

                } else {

                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (menuMultipleActions.isExpanded()) {
            menuMultipleActions.collapse();
        } else if (playFragment.sheetIsExpanded()) {
            playFragment.collapseSheet();
        } else if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(0, true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_items, menu);

        final MenuItem editItem = menu.findItem(R.id.action_edit_name);
        final MenuItem deleteItem = menu.findItem(R.id.action_delete_project);

        editView = (SearchView) MenuItemCompat.getActionView(editItem);
        editView.setQueryHint("Edit name");
        editView.setSubmitButtonEnabled(true);
        editView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String newProjectName = editView.getQuery().toString();
                project.renameTo(newProjectName);
                ab.setTitle(project.getName());
                editItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        deleteItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                confirmDialog("project", "Delete project?", "Delete", null);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void newDialog(DialogFragment dialog, String object) {
        Bundle bundleOne = new Bundle();
        bundleOne.putString("Object", object);
        dialog.setArguments(bundleOne);
        dialog.show(getSupportFragmentManager(), object);
    }

    private void editDialog(String object) {
        newDialog(new EditDialogFragment(), object);
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

    private void addListPage(Folder list) {
        ProjectContentFragment fragment = loadList(list);
        addToPager(fragment);
        pageTitles.add(list.getName());
        listFragments.add(fragment);
        lists.add(list);
    }

    private Snackbar messageSnackbar(String message, int length) {
        final Snackbar mySnackbar = Snackbar.make(snackbar, message, length);
        mySnackbar.show();
        activeSnackbar = mySnackbar;
        return mySnackbar;
    }

    private void setCurrentItem(int item) {
        int pages = mPager.getChildCount();
        if (item < 0) {
            mPager.setCurrentItem(0, true);
        } else if (item >= pages) {
            mPager.setCurrentItem(pages-1, true);
        } else {
            mPager.setCurrentItem(item, true);
        }
        currentItem = mPager.getCurrentItem();
    }

    private void deleteList(Folder list) {
        try {
            list.deleteContent();
            list.delete();
            setCurrentItem(currentItem - 1);
        } catch (Exception e) {}
    }

    private void moveFileTo(String file, String toName, boolean withinProject) {
        String fromFolder = pagerAdapter.getPageTitle(startPage).toString();
        String toFolder = toName;
        Folder currentFolder = projectFolder.getFolder(fromFolder);
        Folder destFolder;
        if (withinProject) {
            destFolder = projectFolder.getFolder(toFolder);
        } else {
            destFolder = projectFolder.getParent().getParent().getFolder(toFolder);
        }
        currentFolder.moveFile(file, destFolder);
        updateLists();
        mPager.setCurrentItem(dropPage);
    }

    @Override
    public void onPositiveButtonClick(String action, Recording object) {
        Folder list = lists.get(currentItem);

        if (action.equals("project")) {
            Intent intent = new Intent();
            intent.putExtra("deletedProject", project.getName());
            setResult(RESULT_OK, intent);
            project.delete();
            finish();
        }
        else if (action.equals(list.getName())) {
            deleteList(list);
            pageTitles.remove(list.getName());
            updateLists();
            menuMultipleActions.collapse();
            messageSnackbar(list.getName() + " deleted", shortSnackbar);
        } else if (action.equals("Move up")) {
            String file = item.getText().toString();
            moveFileTo(file, "Recordings", false);
            messageSnackbar(file + " moved to " + "Recordings", longSnackbar);
        } else if (action.equals("Delete")) {
            String name = object.getName();
            object.delete();
            updateLists();
            playFragment.collapseSheet();
            messageSnackbar(name + " deleted", shortSnackbar);
        }
    }

    @Override
    public void onNegativeButtonClick() {

    }

    @Override
    public void onPositiveButtonClick(String object, String input) {
        if (object.equals("Edit list")) {
            lists.get(currentItem).renameTo(input);
            updateLists();
            messageSnackbar("List renamed to " + input, shortSnackbar);
        } else if (object.equals("New list")) {
            newList(input);
            updateLists();
            messageSnackbar(input + " created", shortSnackbar);
        }
        menuMultipleActions.collapse();
    }

    @Override
    public void onNegativeButtonClick(String object) {

    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onCardClick(Recording recording) {
        playFragment.setData(recording);
        playFragment.expandSheet();
    }

    @Override
    public void onDragStart(CardView view) {
        layout.addView(leftDragLayer);
        layout.addView(rightDragLayer);
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
        layout.removeView(leftDragLayer);
        layout.removeView(rightDragLayer);
    }

    @Override
    public void onDropListener(Project project) {
        System.out.println("OnDropInCard");
        String file = item.getText().toString();
        moveFileTo(file, project.getFolder().getFolderNames()[0], true);
    }

    @Override
    public void onHideShowClicked() {

    }

    @Override
    public void onDeleteClicked(Recording recording) {
        confirmDialog("Delete", "Delete " + recording.getName() + "?", "Delete", recording);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

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
                return pageTitles.get(position);
            } catch (Exception e) { return ""; }
        }
    }

    protected class myDragEventListener implements View.OnDragListener {
        public boolean onDrag(View v, DragEvent event) {

            final int action = event.getAction();
            switch(action) {

                case DragEvent.ACTION_DRAG_STARTED:
                    startPage = mPager.getCurrentItem();
                    previousPage = startPage;
                    menuMultipleActions.setVisibility(INVISIBLE);
                    if (startPage == 0) {
                        layout.addView(rightDragLayer);
                    } else if (startPage == pagerAdapter.getCount() - 1) {
                        layout.addView(leftDragLayer);
                    } else {
                        layout.addView(leftDragLayer);
                        layout.addView(rightDragLayer);
                    }
                    layout.addView(topDragLayer);
                    layout.addView(bottomDragLayer);
                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:
                    float x = event.getX();
                    float y = event.getY();
                    currentPage = mPager.getCurrentItem();

                    if (x < 200 && y < 200) {
                        moveUpDrag = true;
                        deleteDrag = false;
                    } else if (y > 2250) {
                        moveUpDrag = false;
                        deleteDrag = true;
                    } else {
                        moveUpDrag = false;
                        deleteDrag = false;
                        if (currentPage != previousPage) {
                            if (currentPage == 0) {
                                layout.removeView(leftDragLayer);
                            } else if (currentPage == pagerAdapter.getCount() - 1) {
                                layout.removeView(rightDragLayer);
                            } else {
                                layout.removeView(leftDragLayer);
                                layout.removeView(rightDragLayer);
                                layout.addView(leftDragLayer);
                                layout.addView(rightDragLayer);
                            }
                            previousPage = currentPage;
                        }

                        if (pageScrollState == stateIdle) {
                            if (x < 150 && currentPage > 0) {
                                mPager.setCurrentItem(currentPage - 1);
                            } else if (x > 1300 && currentPage < pagerAdapter.getCount() - 1) {
                                mPager.setCurrentItem(currentPage + 1);
                            }
                        }
                    }

                    return true;

                case DragEvent.ACTION_DROP:

                    dropPage = mPager.getCurrentItem();
                    item = event.getClipData().getItemAt(0);
                    String file = item.getText().toString();
                    Recording recording = null;
                    try {
                        recording = new Recording(listFolder, listFolder.getFile(file));
                    } catch (Exception e) {}
                    if (moveUpDrag) {
                        confirmDialog("Move up", "Move out of project?", "Move", recording);
                    } else if (deleteDrag) {
                        confirmDialog("Delete", "Delete " + file + "?", "Delete", recording);
                    } else if (dropPage != startPage) {
                        moveFileTo(file, pagerAdapter.getPageTitle(dropPage).toString(), true);
                    }
                    else {
                        //markera som vald, välja fler genom att klicka på korten
                    }
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    layout.removeView(leftDragLayer);
                    layout.removeView(rightDragLayer);
                    layout.removeView(topDragLayer);
                    layout.removeView(bottomDragLayer);
                    //layout.removeView(dragLayer);
                    menuMultipleActions.setVisibility(VISIBLE);
                    return true;

                default:
                    Log.e("DragDrop Example","Unknown action type received by OnDragListener.");
                    break;
            }

            return false;
        }
    }
}
