package com.example.hospital.data.storage;

import android.content.Context;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileStorageManager {
    private final Context context;

    public FileStorageManager(Context context) {
        this.context = context;
    }

    public <T> void saveList(String filename, List<T> data) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                context.openFileOutput(filename, Context.MODE_PRIVATE))) {
            oos.writeObject(new ArrayList<>(data));
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> loadList(String filename) throws IOException, ClassNotFoundException {
        File file = new File(context.getFilesDir(), filename);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file))) {
            return (List<T>) ois.readObject();
        }
    }

    public boolean fileExists(String filename) {
        return new File(context.getFilesDir(), filename).exists();
    }

    public void deleteFile(String filename) {
        File file = new File(context.getFilesDir(), filename);
        if (file.exists()) {
            file.delete();
        }
    }
}