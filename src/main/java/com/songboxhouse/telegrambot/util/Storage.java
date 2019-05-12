package com.songboxhouse.telegrambot.util;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {
    private Map<String, Object> map = new ConcurrentHashMap<>();

    public <T> T get(String name, Class<T> type) {
        return type.cast(map.get(name));
    }

    public <T> Storage put(String name, T value) {
        map.put(name, value);
        return this;
    }

    public void copyFrom(Storage storage) {
        this.map.clear();
        if (storage != null) {
            this.map.putAll(storage.map);
        }
    }

    public void saveIntoFile(File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(map);
        oos.close();
        fos.close();
    }

    public void loadFromFile(File file) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        this.map = (Map<String, Object>) ois.readObject();
    }
}
