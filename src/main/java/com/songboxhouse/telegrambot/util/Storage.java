package com.songboxhouse.telegrambot.util;

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
}
