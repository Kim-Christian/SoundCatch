package com.soundcatch.soundcatch;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kim-Christian on 2017-04-11.
 */

public class Project implements Parcelable {

    private Folder folderObject;
    private Drawable background;
    private List<Tags> tags;

    public Project(Folder folder) {
        this.folderObject = folder;
        //this.background = new GradientDrawable();
        this.tags = new ArrayList<>();
    }

    public Project(Folder parent, String name) {
        this(parent.addFolder(name));
    }

    private Project(Parcel in) {
        this.folderObject = (Folder) in.readParcelable(Folder.class.getClassLoader());
        //this.background = in.readParcelable(Drawable.class.getClassLoader());// = in.readString();
    }

    public static final Creator<Project> CREATOR = new Creator<Project>() {
        @Override
        public Project createFromParcel(Parcel in) {
            return new Project(in);
        }

        @Override
        public Project[] newArray(int size) {
            return new Project[size];
        }
    };

    public String getName() {
        return folderObject.getName();
    }
    public Folder getFolder() { return folderObject; }
    public Drawable getBackground() { return background; }
    public Tags[] getTags() { return tags.toArray(new Tags[tags.size()]); }

    public void renameTo(String newName) {
        folderObject.renameTo(newName);
    }

    public void setBackground(Drawable drawable) {
        background = drawable;
    }

    public void delete() {
        background = null;
        folderObject.delete();
    }

    public Folder addFolder(String name) {
        return folderObject.addFolder(name);
    }

    public File addFile(String name) {
        return folderObject.addFile(name);
    }

    public void addTag(String fileName, String tag) {

    }

    public void removeTag(String fileName, String tag) {

    }

    public void clearTags(String fileName) {

    }

    private void prepareFolder(String name, String newFolderName) {
        //folderObject.claimFileName(name, newFolderName);
        //Folder recordings = new Folder(appFolder, "Recordings");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(folderObject, flags);
        //Bitmap bitmap = (Bitmap)((BitmapDrawable) background).getBitmap();
        //dest.writeParcelable(bitmap, flags);
    }

    /*private List<Tags> loadTags() {

    }*/
}
