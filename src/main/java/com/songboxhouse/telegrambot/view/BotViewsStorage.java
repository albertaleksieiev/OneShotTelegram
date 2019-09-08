package com.songboxhouse.telegrambot.view;

import com.songboxhouse.telegrambot.util.Storage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.songboxhouse.telegrambot.util.TelegramBotUtils.getUid;

public class BotViewsStorage {
    private final BotViewManager botViewManager;
    private boolean instanceSavingEnabled;
    private int maxUserViewCache;

    public BotViewsStorage(BotViewManager botViewManager) {
        this.botViewManager = botViewManager;
    }

    private final Map<Integer, LinkedList<BotView>> userViewCache = new ConcurrentHashMap<>();
    private final Map<Integer, BotView> rootBotViews = new ConcurrentHashMap<>();

    private static final String STORAGE_FOLDER = ".data/savedViewInstances";

    private BotView findBotViewByUuidInStack(String uuid, Integer userID) {
        LinkedList<BotView> botViewCache = userViewCache.get(userID);
        if (botViewCache == null) {
            return null;
        }

        for (BotView aBotViewStack : botViewCache) {
            if (aBotViewStack.getUuid().equals(uuid)) {
                return aBotViewStack;
            }
        }
        return null;
    }

    public synchronized void setRootBotView(Update update, BotView botView) {
        int uid = getUid(update);
        if (!instanceSavingEnabled) {
            rootBotViews.put(uid, botView);
            return;
        }

        saveBotViewInstance(botView, uid);

        File rootFile = getFolderForRoot(uid);
        try {
            PrintWriter printWriter = new PrintWriter(new FileOutputStream(rootFile));
            printWriter.write(botView.getUuid());
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public BotView findBotViewByUuid(UserBotContext botContext, String uuid, Integer userID) {
        BotView localBotView = findBotViewByUuidInStack(uuid, userID);

        if (localBotView == null) {
            try {
                localBotView = loadBotViewInstance(botContext, uuid, userID);
            } catch (Exception e) {
                System.out.printf("Cannot load bot view");
                e.printStackTrace();
            }
        }

       if (localBotView != null) {
            localBotView.evaluateOnCreateAndOnRestore();
            saveBotViewInstance(localBotView, userID);
       }

        return localBotView;
    }

    public synchronized BotView getRootBotView(UserBotContext context, Update update) {
        int uid = getUid(update);

        if (!instanceSavingEnabled) {
            return rootBotViews.get(uid);
        }

        File rootFile = getFolderForRoot(uid);
        Scanner scanner = null;
        try {
            scanner = new Scanner(new FileInputStream(rootFile));
            String uuid = scanner.nextLine();

            return findBotViewByUuid(context, uuid, uid);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                scanner.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private File getFolderForRoot(Integer userID) {
        File folder = new File(STORAGE_FOLDER, "" + userID);
        folder.mkdirs();

        return new File(folder, "parent");
    }

    private File getFolder(String uuid, Integer userID) {
        File folder = new File(STORAGE_FOLDER, "" + userID);
        folder.mkdirs();

        return new File(folder, uuid);
    }

    public void saveBotViewInstance(BotView botView, Integer userID) {
        LinkedList<BotView> botViewCache = userViewCache.getOrDefault(userID, new LinkedList<>());

        // Remove if exist
        botViewCache.removeFirstOccurrence(botView);

        // Insert at begin
        botViewCache.push(botView);

        // Remove last
        if (botViewCache.size() >= maxUserViewCache) {
            botViewCache.removeLast();
        }

        userViewCache.put(userID, botViewCache);

        if (!instanceSavingEnabled) {
            return;
        }

        Storage storage = new Storage();
        botView.onSaveInstanceState(storage);
        BotViewSavedInstance botViewSavedInstance = new BotViewSavedInstance(storage, botView);

        botView.getContext().getExecutorService().submit(() -> {
            File file = getFolder(botView.getUuid(), userID);
            try {
                saveIntoFile(file, botViewSavedInstance);
                System.out.println("BotView saved into " + file);
            } catch (IOException e) {
                System.out.println("Cannot save bot instance");
                e.printStackTrace();
            }
        });
    }

    public void saveIntoFile(File file, BotViewSavedInstance instance) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(instance);
        oos.close();
        fos.close();
    }

    public BotViewSavedInstance loadFromFile(File file) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        return (BotViewSavedInstance) ois.readObject();
    }

    private BotView loadBotViewInstance(BotContext botContext, String uuid, Integer userID) throws IOException, ClassNotFoundException {
        System.out.println("Load bot view from file with uuid=" + uuid + ", and userID = " + userID);

        BotViewSavedInstance savedInstance = loadFromFile(getFolder(uuid, userID));


        BotView view = botViewManager.buildView(botContext, savedInstance.getBotViewClass());
        view.setUuid(uuid);
        view.setTelegramMessageId(savedInstance.getTelegramMessageId());
        view.onRestoreInstanceState(savedInstance.getData());
        view.setParentViewId(savedInstance.parentViewId);
        return view;
    }

    public void setInstanceSavingEnabled(boolean instanceSavingEnabled) {
        this.instanceSavingEnabled = instanceSavingEnabled;
    }

    public void setMaxUserViewCache(int maxUserViewCache) {
        this.maxUserViewCache = maxUserViewCache;
    }
}
