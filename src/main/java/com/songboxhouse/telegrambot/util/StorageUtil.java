package com.songboxhouse.telegrambot.util;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.songboxhouse.telegrambot.util.TelegramBotUtils.getUid;

public class StorageUtil {
    public StorageUtil(String folder, boolean saveToFS, int maxCacheSize) {
        this.initFolder = folder;
        this.saveToFS = saveToFS;
        this.maxCacheSize = maxCacheSize;
    }

    private String initFolder;
    private final Map<Integer, LinkedList<Pair<String, Storage>>> storageCache = new ConcurrentHashMap<>();

    private final boolean saveToFS;
    private final int maxCacheSize;

    public String saveStorage(Update update, Storage storage) throws IOException {
        String storageIdentifier = getStorageIdentifierByUUID(UUID.randomUUID().toString().substring(0, 8));
        if (saveToFS) {
            File file = new File(initFolder, storageIdentifier);

            synchronized (this) {
                new File(initFolder).mkdirs();
            }

            saveStorageToFile(file, storage);
        } else {
            int uid = getUid(update);
            LinkedList<Pair<String, Storage>> storageCache = this.storageCache.getOrDefault(uid, new LinkedList<>());
            storageCache.push(Pair.of(storageIdentifier, storage));

            if (storageCache.size() > maxCacheSize) {
                storageCache.removeLast();
            }
            this.storageCache.put(uid, storageCache);
        }


        return storageIdentifier;
    }

    public Storage loadStorage(Update update, String identifier) throws IOException, ClassNotFoundException {
        if (saveToFS) {
            File file = new File(initFolder, identifier);
            return loadStorageFromFile(file);
        } else {
            int uid = getUid(update);
            LinkedList<Pair<String, Storage>> botViewCache = storageCache.getOrDefault(uid, new LinkedList<>());
            for (Pair<String, Storage> entry: botViewCache) {
                if (entry.fst.equals(identifier)) {
                    return entry.snd;
                }
            }
        }
        return null;
    }

    public static void saveStorageToFile(File file, Storage storage) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(storage);
        oos.close();
        fos.close();
    }

    public static Storage loadStorageFromFile(File file) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        return (Storage) ois.readObject();
    }

    private static String getStorageIdentifierByUUID(String uuid) {
        return String.format("st-%s", uuid);
    }
}
