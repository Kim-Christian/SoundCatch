package com.soundcatch.soundcatch;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kim-Christian on 2017-04-11.
 */

public class Recording implements Parcelable {

    private File fileObject;
    //private Graph graph;
    private List<Tags> tags;
    private Folder parent;

    public Recording(Folder parent, File file) {
        this.fileObject = file;
        this.tags = new ArrayList<>();
        this.parent = parent;
    }

    private Recording(Parcel in) {
        this.fileObject = new File(in.readString());
        this.parent = in.readParcelable(Folder.class.getClassLoader());
        //this.background = in.readParcelable(Drawable.class.getClassLoader());// = in.readString();
    }

    public static final Creator<Recording> CREATOR = new Creator<Recording>() {
        @Override
        public Recording createFromParcel(Parcel in) {
            return new Recording(in);
        }

        @Override
        public Recording[] newArray(int size) {
            return new Recording[size];
        }
    };

    public String getName() {
        return fileObject.getName();
    }
    public File getFile() { return fileObject; }
    //public Graph getGraph() { return graph; }
    public Tags[] getTags() { return tags.toArray(new Tags[tags.size()]); }
    public Folder getParent() { return parent; }

    public void renameTo(String newName) {
        fileObject.renameTo(new File(parent.getPath() + File.separator + newName));
    }

    public void delete() {
        fileObject.delete();
    }

    public void addTag(String fileName, String tag) {

    }

    public void removeTag(String fileName, String tag) {

    }

    public void clearTags(String fileName) {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileObject.getPath());
        dest.writeParcelable(parent, flags);
        //Bitmap bitmap = (Bitmap)((BitmapDrawable) background).getBitmap();
        //dest.writeParcelable(bitmap, flags);
    }

    /*private List<Tags> loadTags() {

    }*/
}
