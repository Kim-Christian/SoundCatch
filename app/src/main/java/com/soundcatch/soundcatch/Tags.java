package com.soundcatch.soundcatch;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Kim-Christian on 2017-04-12.
 */

public class Tags {

    private File file;
    private List<String> tags;

    public Tags(File file, String[] tags) {
        this.file = file;
        this.tags = new ArrayList<String>(Arrays.asList(tags));
    }

    public Tags(File file, String tag) {
        this(file, new String[]{tag});
    }

    public Tags(File file) {
        this(file, new String[0]);
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return file.getName();
    }

    public String[] getTags() {
        return tags.toArray(new String[tags.size()]);
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public void clearTags() {
        tags = new ArrayList<String>();
    }
}
