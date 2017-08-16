package com.soundcatch.soundcatch;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Kim-Christian on 2017-04-17.
 */

public class ProjectsFragment extends Fragment implements ProjectCard.OnDropListener {

    private List<Project> projects;
    private List<ProjectCard> projectCards;
    private ScrollView scrollView;
    private LinearLayout projectList;
    private Folder projectsFolder;
    private int index = 0;
    private Drawable[] backgrounds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_projects, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        scrollView = (ScrollView) getActivity().findViewById(R.id.scroll_view);
        projectList = (LinearLayout) getActivity().findViewById(R.id.project_list);
        System.out.println(scrollView + " created");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        backgrounds = new Drawable[]{
                getContext().getDrawable(R.drawable.guitar),
                getContext().getDrawable(R.drawable.guitar_fire),
                getContext().getDrawable(R.drawable.notes)};

        Folder appFolder = new Folder(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
        appFolder.claimFileName("Projects", "Other");
        projectsFolder = new Folder(appFolder, "Projects");
        projects = new ArrayList<>();
        projectCards = new ArrayList<>();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProjectCards();
    }

    public ScrollView getScrollView() {
        return scrollView;
    }
    public LinearLayout getProjectList() {
        return projectList;
    }

    public Project createProject() {
        Project project = new Project(projectsFolder, "New project");
        setBackground(project);
        addProjectCard(project);
        return project;
    }

    private void setBackground(Project project) {
        project.setBackground(backgrounds[index]);
        index++;
        if (index >= backgrounds.length) {
            index = 0;
        }
    }

    private void deleteProject(Project project) {
        projects.remove(project);
        project.delete();
    }

    private ProjectCard addProjectCard(final Project project) {
        final ProjectCard projectCard = new ProjectCard(getContext(), project);
        projectList.addView(projectCard.getCardView());
        projectCards.add(projectCard);
        projectCard.getCardView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ProjectActivity.class);
                intent.putExtra("Project", project);
                startActivityForResult(intent, 1);
            }
        });

        final ViewTreeObserver vto = projectCard.getTextView().getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                projectCard.adjustTextView();
                projectCard.getTextView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        return projectCard;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                String deletedProject = data.getStringExtra("deletedProject");
                ((MainActivity) getActivity()).messageSnackbar(deletedProject + " deleted", Snackbar.LENGTH_SHORT);
            }
        }
    }

    private void removeProjectCard(ProjectCard projectCard) {
        projectList.removeView(projectCard.getCardView());
    }

    private void loadProjects() {
        index = 0;
        Folder[] folders = projectsFolder.getFolders();
        for (Folder folder : folders) {
            Project project = new Project(folder);
            setBackground(project);
            projects.add(project);
            addProjectCard(project);
        }
    }

    private void setProjectName(String name) {
        TextView text = new TextView(getContext());
        ImageView image = new ImageView(getContext());

        text.setText(name);
        image.setImageResource(R.drawable.notes);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void removeAllCards() {
        for (ProjectCard card : projectCards) {
            removeProjectCard(card);
        }
    }

    private void updateProjectCards() {
        removeAllCards();
        projects.clear();
        projectCards.clear();
        loadProjects();
    }

    @Override
    public void onDropListener(Project project) {

    }
}
