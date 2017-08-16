package com.soundcatch.soundcatch;

import android.graphics.Path;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Kim-Christian on 2017-03-29.
 */

public class Folder implements Parcelable {

    private File fileObject;
    private Folder parent;

    public Folder(File parent, String name) {
        fileObject = new File(parent.getPath() + File.separator + name);
        if (!isFolder()) {
            System.out.println("is not folder!!!!!!!!!!!!!!!!");
            boolean folder = createFolder(fileObject);
            System.out.println(folder);
        }
    }

    public Folder(Folder parent, String name) {
        this(parent.fileObject, name);
        this.parent = parent;
    }

    protected Folder(Parcel in) {
        this.parent = (Folder) in.readParcelable(Folder.class.getClassLoader());
        fileObject = new File(in.readString());
    }

    public static final Creator<Folder> CREATOR = new Creator<Folder>() {
        @Override
        public Folder createFromParcel(Parcel in) {
            return new Folder(in);
        }

        @Override
        public Folder[] newArray(int size) {
            return new Folder[size];
        }
    };

    //Getters
    public String getName() {
        return fileObject.getName();
    }
    public String getPath() {
        return fileObject.getPath();
    }
    public Folder getParent() {
        return parent;
    }
    public File[] getContent() { return fileObject.listFiles(); }

    private void rename(String name) {
        fileObject.renameTo(new File(parent.getPath() + File.separator + name));
        fileObject = new File(parent.getPath() + File.separator + name);
    }

    private void updateContent(File[] content) {
        for (File file : content) {
            file.renameTo(new File(parent.getPath() + File.separator + getName() + File.separator + file.getName()));
        }
    }

    private boolean createFile(File file) {
        boolean created = false;
        try {
            created = file.createNewFile();
            System.out.println("File created successfully!!!!!!!!!!!!!!!!!!!!!!!");
        }
        catch (Exception e)
        {}
        System.out.println(created);
        return created;
    }

    private boolean createFolder(File file) {
        if (true){//createFile(file)) {
            System.out.println("created file!!!!!!!!!!!!!!!!!!!!!");
            return file.mkdir();
        }
        return file.mkdir();
    }

    public void renameTo(String name) {
        rename(name);
    }

    public void moveTo(Folder folder) {
        File[] content = getContent();
        parent = folder;
        rename(getName());
        updateContent(content);
    }

    public void delete() {
        parent = null;
        deleteContent();
        fileObject.delete();
    }

    public void deleteFiles() {
        for (File file : getFiles()) {
            file.delete();
        }
    }

    public void deleteFolders() {
        for (Folder folder : getFolders()) {
            folder.delete();
        }
    }

    public void deleteContent() {
        deleteFiles();
        deleteFolders();
    }

    public void deleteFile(String name) {
        getFile(name).delete();
    }

    private int maxValue(int[] list) {
        int max = 0;//list[0];
        for (int i : list) {
            if (i > max) {
                max = i;
            }
        }
        return max;
    }

    private String[] startingWith(String prefix, String[] nameList) {
        List<String> names = new ArrayList<String>();
        for (String name : nameList) {
            if (name.toLowerCase().startsWith(prefix.toLowerCase())) {
                names.add(name);
            }
        }
        String[] withPrefix = new String[names.size()];
        return names.toArray(withPrefix);
    }

    private int getSuffix(String name) {
        int suffix = 0;
        try {
            suffix = Integer.parseInt(name.substring(name.indexOf("(") + 1, name.indexOf(")")));
        }
        catch (Exception e)
        {}
        return suffix;
    }

    private int[] existingSuffixes(String prefix, String[] nameList) {
        String[] withPrefix = startingWith(prefix, nameList);
        List<Integer> suffixes = new ArrayList<Integer>();
        for (String name : withPrefix) {
            int suffix = getSuffix(name);
            if (suffix != 0) {
                suffixes.add(suffix);
            }
        }
        int[] suffixList = new int[suffixes.size()];
        for (int i = 0; i < suffixes.size(); i++) {
            suffixList[i] = suffixes.get(i);
        }
        return suffixList;
    }

    private int maxSuffix(String name, String[] nameList) {
        return maxValue(existingSuffixes(name, nameList));
    }

    private String addSuffix(String name, int suffix) {
        return name + " (" + suffix + ")";
    }

    public Folder addFolder(String name) {
        String modifiedName = name;
        if (folderExists(name)) {
            modifiedName = addSuffix(name, 1 + maxSuffix(name, getFolderNames()));
        }
        Folder newFolder = new Folder(this, modifiedName);
        createFolder(newFolder.fileObject);
        return newFolder;
    }

    public File addFile(String name) {
        String modifiedName = name;
        if (fileExists(name)) {
            modifiedName = addSuffix(name, 1 + maxSuffix(name, getFileNames()));
        }
        File newFile = new File(getPath() + File.separator + modifiedName);
        createFile(newFile);
        return newFile;
    }

    public void moveFile(String name, Folder folder) {
        File newFile = folder.addFile(name);
        try {
            copyFileTo(getFile(name), newFile);
            deleteFile(name);
        } catch (Exception e) {  System.out.println("copy fail"); }
    }

    public void copyFileTo(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public Folder[] getFolders() {
        List<Folder> folders = new ArrayList<Folder>();
        for (File file : getContent()) {
            if (file.isDirectory()) {
                folders.add(new Folder(this, file.getName()));
            }
        }
        Folder[] folderList = new Folder[folders.size()];
        return folders.toArray(folderList);
    }

    public File[] getFiles() {
        List<File> files = new ArrayList<File>();
        for (File file : getContent()) {
            if (file.isFile()) {
                files.add(file);
            }
        }
        File[] fileList = new File[files.size()];
        return files.toArray(fileList);
    }

    public Folder getFolder(String name) {
        return new Folder(this, name);
    }

    public File getFile(String name) {
        return new File(getPath() + File.separator + name);
    }

    private String[] getNames(File[] fileList) {
        List<String> names = new ArrayList<String>();
        for (File file : fileList) {
            names.add(file.getName());
        }
        String[] nameList = new String[names.size()];
        return names.toArray(nameList);
    }

    public String[] getFolderNames() {
        List<File> folders = new ArrayList<File>();
        for (Folder folder : getFolders()) {
            folders.add(folder.fileObject);
        }
        File[] folderList = new File[folders.size()];
        folders.toArray(folderList);
        return getNames(folderList);
    }

    public String[] getFileNames() {
        return getNames(getFiles());
    }

    private boolean nameExists(String name, String[] nameList) {
        List<String> names = new ArrayList<String>(Arrays.asList(nameList));
        return names.contains(name);
    }

    public void claimFileName(String name, String newFolderName) {
        if (fileExists(name)) {
            Folder newFolder = new Folder(this, newFolderName);
            moveFile(name, newFolder);
        }
    }

    public boolean folderExists(String name) {
        return nameExists(name, getFolderNames());
    }

    public boolean fileExists(String name) {
        return nameExists(name, getFileNames());
    }

    //File-object methods
    public boolean exists() {
        return fileObject.exists();
    }
    public boolean isFolder() {
        return this.fileObject.isDirectory();
    }
    public boolean isFile() {
        return this.fileObject.isFile();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(parent, flags);
        dest.writeString(fileObject.getPath());
    }
}