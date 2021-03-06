package com.songboxhouse.telegrambot;


import com.songboxhouse.telegrambot.view.BotContext;
import com.songboxhouse.telegrambot.view.UserBotContext;
import com.songboxhouse.telegrambot.util.*;
import com.songboxhouse.telegrambot.view.BotMessage;
import com.songboxhouse.telegrambot.view.BotView;
import com.songboxhouse.telegrambot.view.BotViewManager;
import com.songboxhouse.telegrambot.view.BotViewsStorage;
import org.apache.http.util.TextUtils;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static com.songboxhouse.telegrambot.CallbackDataManager.STORAGE_KEY_CALLBACK_ACTION;
import static com.songboxhouse.telegrambot.util.TelegramBotUtils.backButton;
import static com.songboxhouse.telegrambot.util.TelegramBotUtils.getChatId;
import static com.songboxhouse.telegrambot.util.TelegramBotUtils.getUid;


public class BotCenter {
    public static final String STATE_GO_BACK = "SGB_&^#";
    public static final String STATE_GO_BACK_AS_NEW_MESSAGE = "SGBN_&_^#";

    private final BotViewManager botViewManager;
    private final Object telegramMethodManager;
    private final int buttonsInRow;
    private Class<? extends BotView> initialViewClass;
    private final TelegramLongPollingBot telegramLongPollingBot;
    private final BotCenterToContextBridge botCenterToContextBridge;

    private Map<Integer, SessionStorage> userToSessionStorage = new ConcurrentHashMap<>();
    private Map<Integer, Object> userToLock = new ConcurrentHashMap<>();
    private final BotViewsStorage botViewsStorage;
    private final CallbackDataManager callbackDataManager;

    private BotCenter(Builder builder) {
        ApiContextInitializer.init(); /* Require for new Telegram bot */
        this.initialViewClass = builder.initialViewClass;
        this.telegramLongPollingBot = new TelegramLongPollingBotImpl(builder.telegramBotToken, builder.telegramBotName,
                builder.updateReceiveListener, builder.executorServiceManager, this);
        this.botViewManager = new BotViewManager(builder.dependecyProvider);
        this.telegramMethodManager = builder.telegramMethodManager;
        this.botViewsStorage = new BotViewsStorage(botViewManager);
        this.callbackDataManager = new CallbackDataManager(builder.callbackDataSavingEnabled, builder.maxCallbackDataCache);
        this.buttonsInRow = builder.buttonsInRow;

        botCenterToContextBridge = new BotCenterToContextBridge(builder.executorServiceThreadSize, builder.executorServiceManager, builder.localizationProvider);

        botViewsStorage.setInstanceSavingEnabled(builder.instanceSavedEnabled);
        botViewsStorage.setMaxUserViewCache(builder.maxBotViewCache);
    }

    public void start() {
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(this.telegramLongPollingBot);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    private void onUpdateReceivedUnsafe(Update update) {
        int uid;
        synchronized (this) {
            uid = getUid(update);
        }
        UserBotContext context = botCenterToContextBridge.buildContext(update);

        synchronized (userToLock.computeIfAbsent(uid, k -> new Object())) {
            if (update.getCallbackQuery() == null || update.getCallbackQuery().getData() == null) {
                BotView rootBotView = botViewsStorage.getRootBotView(context, update);
                if (rootBotView != null) {
                    rootBotView.onTextReceived(update.getMessage()); // TODO test
                } else {
                    fallbackGoHome(update);
                }

                return;
            }

            if (update.getCallbackQuery() != null && update.getCallbackQuery().getData() != null) {
                String data = update.getCallbackQuery().getData();

                BotView view;
                TelegramBotUtils.UUIDWithData uuidWithData = TelegramBotUtils.parseUUIDWithData(data);
                if (uuidWithData != null) {

                    view = botViewsStorage.findBotViewByUuid(context, uuidWithData.uuid, uid);

                    if (uuidWithData.data.equals(STATE_GO_BACK)) {
                        botCenterToContextBridge.goBack(view, update, false);
                        return;
                    } else if (uuidWithData.data.equals(STATE_GO_BACK_AS_NEW_MESSAGE)) {
                        botCenterToContextBridge.goBack(view, update, true);
                        return;
                    }

                    if (view == null) {
                        throw new IllegalStateException("Bot view is null, cannot process callback data");
                    }


                } else {
                    throw new IllegalStateException("Cannot find BotView uuid instance for callbackdata");
                }

                if (view.menuLinkStates() != null) {
                    for (Class classView : view.menuLinkStates()) {
                        if (classView.getSimpleName().equals(uuidWithData.data)) {
                            botCenterToContextBridge.navigate(view, classView, null, update, view.menuLinksAsNewMessage());
                            return;
                        }
                    }
                }

                String action = uuidWithData.data;
                Storage storage = callbackDataManager.tryParseData(update, uuidWithData.data);
                if (storage != null) {
                    String storageAction = storage.get(STORAGE_KEY_CALLBACK_ACTION, String.class);
                    if (storageAction != null) {
                        action = storageAction;
                    }
                }

                view.onCallbackQueryDataReceived(update, action, storage);
            }
        }
    }

    void onUpdateReceived(Update update) {
        try {
            onUpdateReceivedUnsafe(update);
        } catch (Exception e) {
            e.printStackTrace();
            fallbackGoHome(update);
        }
    }

    private void fallbackGoHome(Update update) {
        System.out.println("fallbackGoHome");
        BotView botView = botViewManager.buildView(botCenterToContextBridge.buildContext(update), initialViewClass);
        botCenterToContextBridge.draw(botView, update, true);
    }

    private synchronized boolean sendMessage(Update update, BotView view, BotMessage message) {
        Class[] links = view.menuLinkStates();
        if (links == null) {
            links = new Class[0];
        }

        long chatId = getChatId(update);
        InlineKeyboardBuilder inlineKeyboardBuilder = new InlineKeyboardBuilder(buttonsInRow);

        if (message.getInlineButtons() != null && !message.getInlineButtons().isEmpty()) {
            List<List<InlineKeyboardButton>> buttons = message.getInlineButtons()
                    .stream()
                    .map(list -> list.stream().peek(it -> {
                        String callbackData = it.getCallbackData();
                        String newCallbackData = TelegramBotUtils.createCallbackData(view, it.getCallbackData());
                        if (!TextUtils.isEmpty(callbackData) && !callbackData.contains(newCallbackData)) {
                            it.setCallbackData(newCallbackData);
                        }
                    }).collect(Collectors.toList())).collect(Collectors.toList());
            inlineKeyboardBuilder.addAll(buttons);
        }

        // Default inline buttons
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        inlineKeyboardBuilder.createRow();
        for (Class linkClass : links) {
            InlineKeyboardButton button = new InlineKeyboardButton(botViewManager.buildView(view.getContext(), linkClass).name())
                    .setCallbackData(view.getUuid() + linkClass.getSimpleName());
            inlineKeyboardBuilder.appendButtonToTheLastRow(button);
        }

        if (!view.getClass().equals(initialViewClass)) {
            inlineKeyboardBuilder.appendButtonInNewRow(backButton(view, false));
        }

        inlineKeyboardMarkup.setKeyboard(inlineKeyboardBuilder.build());

        if (TextUtils.isBlank(message.getText())) {
            System.out.println("Message cannot be empty, skip sending");
            return false;
        }

        if (!message.isSendAsNew() && view.getTelegramMessageId() == 0) {
            System.out.println("Cannot send message as edited, message id is 0: " + view.getUuid());
        }

        if (!message.isSendAsNew() && view.getTelegramMessageId() != 0) {
            EditMessageText editMessageText = new EditMessageText()
                    .setMessageId(view.getTelegramMessageId())
                    .setChatId(chatId)
                    .setReplyMarkup(inlineKeyboardMarkup)
                    .setText(message.getText());


            // Send message
            try {
                telegramLongPollingBot.execute(editMessageText);
                return true;
            } catch (TelegramApiException e) {
                e.printStackTrace();
                view.setTelegramMessageId(0);
                return sendMessage(update, view, message);
            }
        } else {
            SendMessage sendMessage = new SendMessage(chatId, message.getText())
                    .setReplyMarkup(inlineKeyboardMarkup);

            try {
                Message sentMessage = telegramLongPollingBot.execute(sendMessage);
                view.setTelegramMessageId(sentMessage.getMessageId());
                // botViewsStorage.saveBotViewInstance(view, uid);
                return true;
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public class BotCenterToContextBridge {
        @Nullable
        private final LocalizationProvider localizationProvider;
        private ExecutorService executorService;

        BotCenterToContextBridge(int executorServiceThreadSize, ExecutorServiceManager executorServiceManager, @Nullable LocalizationProvider localizationProvider) {
            this.executorService = executorServiceManager.createExecutorService(executorServiceThreadSize);
            this.localizationProvider = localizationProvider;
        }

        private void draw(BotView botView, Update update, boolean sendAsNewMessage) {
            botView.draw(this, update, sendAsNewMessage);

            botViewsStorage.saveBotViewInstance(botView, getUid(update));
            botViewsStorage.setRootBotView(update, botView);
        }

        public <T extends BotView> BotView navigate(BotView callerView, Class<T> view, Storage data, Update update, boolean asNewMessage) {
            return navigate(callerView, view, update, data == null ? new Storage() : data, asNewMessage);
        }

        private <T extends BotView> BotView navigate(BotView callerView, Class<T> targetViewClass, Update update, Storage data, boolean asNewMessage) {
            synchronized (botViewsStorage) {
                BotContext botContext = buildContext(update);

                BotView targetView = botViewManager.buildView(botContext, targetViewClass);
                targetView.setParentView(callerView);

                if (callerView != null) {
                    callerView.onStop();

                    // Attach message id from caller view to target
                    if (!asNewMessage) {
                        targetView.setTelegramMessageId(callerView.getTelegramMessageId());
                    }
                }

                targetView.setData(data);

                try {
                    draw(targetView, update, asNewMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return targetView;
            }
        }

        public synchronized boolean sendMessage(BotMessage message,
                                                BotView view,
                                                Update update) {
            return BotCenter.this.sendMessage(update, view, message);
        }

        public SessionStorage getSessionStorage(Update update) {
            int uid = getUid(update);
            return userToSessionStorage.computeIfAbsent(uid, k -> new SessionStorage());
        }

        void goBack(BotView target, Update update, boolean sendAsNewMessage) {
            synchronized (botViewsStorage) {
                int uid = getUid(update);

                BotView parent = null;
                if (target.getParentViewId().equals(BotView.PARENT_ID_ROOT)
                        || (parent = botViewsStorage.findBotViewByUuid(buildContext(update), target.getParentViewId(), uid)) == null) {
                    System.out.println("Cannot navigate back this is leaf");
                    fallbackGoHome(update);
                    return;
                }
                target.onStop();
                // TODO REMOVE FROM STORAGE
                parent = parent.clone(botViewManager, botCenterToContextBridge, update);
                parent.setTelegramMessageId(target.getTelegramMessageId());

                draw(parent, update, sendAsNewMessage);
                if (sendAsNewMessage) {
                    botViewsStorage.setRootBotView(update, parent);
                }
            }
        }

        public AbsSender getTelegramSender() {
            return telegramLongPollingBot;
        }

        public ExecutorService executorService() {
            return executorService;
        }

        public CallbackDataManager getCallbackDataManager() {
            return callbackDataManager;
        }

        @Nullable
        public String getLocale(Update update) {
            if (localizationProvider == null) {
                return null;
            }
            return localizationProvider.getLocale(update);
        }

        public UserBotContext buildContext(Update update) {
            return new UserBotContext(update, this);
        }

        @Nullable
        public LocalizationProvider getLocalization() {
            return localizationProvider;
        }
    }

    public interface LocalizationProvider {
        public String getString(String locale, Integer stringId);
        public String getLocale(Update update);
    }

    public static class Builder {
        private Object dependecyProvider;
        private final String telegramBotName;
        private final String telegramBotToken;
        private final Class initialViewClass;
        private Object telegramMethodManager;
        private boolean instanceSavedEnabled = false;
        private int maxBotViewCache = 60;

        private boolean callbackDataSavingEnabled = false;
        private int maxCallbackDataCache = 256;

        private int buttonsInRow = 2;
        private int executorServiceThreadSize = 16;
        private UpdateReceiveListener updateReceiveListener;
        private ExecutorServiceManager executorServiceManager = new ExecutorServiceManager();
        private LocalizationProvider localizationProvider;

        public <BV extends BotView> Builder(String telegramBotName, String telegramBotToken, Class<BV> initialViewClass) {
            this.telegramBotName = telegramBotName;
            this.telegramBotToken = telegramBotToken;
            this.initialViewClass = initialViewClass;
        }

        public Builder setDependecyProvider(Object dependecyProvider) {
            this.dependecyProvider = dependecyProvider;
            return this;
        }

        public Builder setLocalization(LocalizationProvider localizationProvider) {
            this.localizationProvider = localizationProvider;
            return this;
        }

        //        public Builder setTelegramMethodManager(Object telegramMethodManager) {
//            this.telegramMethodManager = telegramMethodManager;
//            return this;
//        }

        public Builder setInstanceSavingEnabled(boolean enabled) {
            instanceSavedEnabled = enabled;
            return this;
        }

        public Builder setButtonsInRow(int buttonsInRow) {
            this.buttonsInRow = buttonsInRow;
            return this;
        }

        public Builder setExecutorServiceThreadSize(int executorServiceThreadSize) {
            this.executorServiceThreadSize = executorServiceThreadSize;
            return this;
        }

        public Builder setUpdateReceiveListener(UpdateReceiveListener updateReceiveListener) {
            this.updateReceiveListener = updateReceiveListener;
            return this;
        }

        public Builder setMaxBotViewCache(int maxBotViewCache) {
            this.maxBotViewCache = maxBotViewCache;
            return this;
        }

        public Builder setCallbackDataSavingEnabled(boolean callbackDataSavingEnabled) {
            this.callbackDataSavingEnabled = callbackDataSavingEnabled;
            return this;
        }

        public Builder setMaxCallbackDataCache(int maxCallbackDataCache) {
            this.maxCallbackDataCache = maxCallbackDataCache;
            return this;
        }

        public Builder setExecutorServiceManager(ExecutorServiceManager executorServiceManager) {
            this.executorServiceManager = executorServiceManager;
            return this;
        }

        public BotCenter build() {
            return new BotCenter(this);
        }
    }
}
