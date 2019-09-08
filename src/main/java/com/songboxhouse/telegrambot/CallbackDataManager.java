package com.songboxhouse.telegrambot;

import com.songboxhouse.telegrambot.util.Storage;
import com.songboxhouse.telegrambot.util.StorageUtil;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;

public class CallbackDataManager {
    private final StorageUtil storageUtil;

    public CallbackDataManager(boolean instanceSavingEnabled, int maxCacheSize) {
        storageUtil = new StorageUtil(".data/storages", instanceSavingEnabled, maxCacheSize);
    }

    public static final String STORAGE_KEY_CALLBACK_ACTION = "STORAGE_KEY_CALLBACK_ACTION";

    public String saveStorage(Update update, Storage storage) throws IOException {
        return storageUtil.saveStorage(update, storage);
    }

    public Storage tryParseData(Update update, String data) {
        try {
            return storageUtil.loadStorage(update, data);
        } catch (Exception e) {
        }
        return null;
    }

}
