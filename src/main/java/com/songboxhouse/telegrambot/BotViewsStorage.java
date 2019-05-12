package com.songboxhouse.telegrambot;

import com.songboxhouse.telegrambot.context.BotContext;
import com.songboxhouse.telegrambot.util.Storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class BotViewsStorage {
    private static final String KEY_BOT_VIEW_CLASS = "com.songboxhouse.telegrambot.KEY_BOT_VIEW_CLASS";
    private final BotViewManager botViewManager;
    private boolean instanceSavingEnabled = true;

    public BotViewsStorage(BotViewManager botViewManager) {
        this.botViewManager = botViewManager;
    }

    protected final Map<Integer, Stack<ViewStackItem>> userViewStack = new ConcurrentHashMap<>();
    private static final String STORAGE_FOLDER = ".data/savedViewInstances";

    private BotView findBotViewByUuidInStack(String uuid, Integer userID) {
        Stack<ViewStackItem> viewStackItems = userViewStack.get(userID);
        if (viewStackItems == null) {
            return null;
        }

        for (int i = 0; i < viewStackItems.size(); i++) {
            ViewStackItem viewStackItem = viewStackItems.elementAt(i);
            if (viewStackItem.getView().getUuid().equals(uuid)) {
                return viewStackItem.getView();
            }
        }
        return null;
    }


    private File getFolder(String uuid, Integer userID) {
        File folder = new File(STORAGE_FOLDER, "" + userID);
        folder.mkdirs();

        return new File(folder, uuid);
    }

    void saveBotViewInstance(BotView botView, Integer userID) {
        if (!instanceSavingEnabled) {
            return;
        }

        Storage storage = new Storage();
        botView.onSaveInstanceState(storage);
        storage.put(KEY_BOT_VIEW_CLASS, botView.getClass());

        botView.getContext().getExecutorService().submit(() -> {
            File file = getFolder(botView.getUuid(), userID);
            try {
                storage.saveIntoFile(file);
                System.out.println("BotView saved into " + file);
            } catch (IOException e) {
                System.out.println("Cannot save bot instance");
                e.printStackTrace();
            }
        });
    }

    private BotView loadBotViewInstance(BotContext botContext, String uuid, Integer userID) throws IOException, ClassNotFoundException {
        System.out.println("Load bot view from file with uuid=" + uuid + ", and userID = " + userID);
        File file = getFolder(uuid, userID);
        Storage storage = new Storage();
        storage.loadFromFile(file);
        Class viewClass = storage.get(KEY_BOT_VIEW_CLASS, Class.class);

        BotView view = botViewManager.getView(botContext, viewClass);
        view.setUuid(uuid);
        view.onRestoreInstanceState(storage);
        return view;
    }

    BotView findBotViewByUuid(BotContext botContext, String uuid, Integer userID) {
        BotView botViewByUuidInStack = findBotViewByUuidInStack(uuid, userID);
        if (botViewByUuidInStack != null) {
            return botViewByUuidInStack;
        }

        try {
            return loadBotViewInstance(botContext, uuid, userID);
        } catch (Exception e) {
            System.out.printf("Cannot load bot view");
            e.printStackTrace();
        }

        return null;
    }

    public void setInstanceSavingEnabled(boolean instanceSavingEnabled) {
        this.instanceSavingEnabled = instanceSavingEnabled;
    }
}
