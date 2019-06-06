package com.songboxhouse.telegrambot.util;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Storage implements Serializable {
    public static final String KEY_TELEGRAM_UPDATE = "KEY_TELEGRAM_UPDATE";

    private static final long serialVersionUID = 1L;

    private Map<String, Object> map = new ConcurrentHashMap<>();

    public <T> T get(String name, Class<T> type) {
        return type.cast(map.get(name));
    }

    public <T> Storage put(String name, T value) {
        if (value == null) {
            System.out.println("DATA is null, skip put in storage");
            return this;
        }
        map.put(name, value);
        return this;
    }

    public void copyFrom(Storage storage) {
        this.map.clear();
        if (storage != null) {
            this.map.putAll(storage.map);
        }
    }
}
