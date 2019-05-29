package com.songboxhouse.telegrambot;


import com.songboxhouse.telegrambot.context.BotContext;
import com.songboxhouse.telegrambot.context.UserBotContext;
import com.songboxhouse.telegrambot.util.SessionStorage;
import com.songboxhouse.telegrambot.util.Storage;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.songboxhouse.telegrambot.util.ExecutorUtil.createExecutorService;
import static com.songboxhouse.telegrambot.util.TelegramBotUtils.backButton;
import static com.songboxhouse.telegrambot.util.TelegramBotUtils.getChatId;
import static com.songboxhouse.telegrambot.util.TelegramBotUtils.getUid;


public class BotCenter {
    public static final String STATE_GO_BACK = "com.songboxhouse.telegrambot.STATE_GO_BACK";
    public static final String STATE_GO_BACK_AS_NEW_MESSAGE = "com.songboxhouse.telegrambot.STATE_GO_BACK";

    private final BotViewManager botViewManager;
    private final Object telegramMethodManager;
    private Class<? extends BotView> initialViewClass;
    private final TelegramLongPollingBot telegramLongPollingBot;
    private BotCenterToContextBridge botCenterToContextBridge = new BotCenterToContextBridge();

    private Map<Integer, SessionStorage> userToSessionStorage = new ConcurrentHashMap<>();
    private Map<Integer, Object> userToLock = new ConcurrentHashMap<>();
    private Map<Long, Integer> chatIdToLastMessage = new ConcurrentHashMap<>();
    private BotViewsStorage botViewsStorage;

    private BotCenter(Builder builder) {
        ApiContextInitializer.init(); /* Require for new Telegram bot */
        this.initialViewClass = builder.initialViewClass;
        this.telegramLongPollingBot = new TelegramLongPollingBotImpl(builder.telegramBotToken, builder.telegramBotName, this);
        this.botViewManager = new BotViewManager(builder.dependecyProvider);
        this.telegramMethodManager = builder.telegramMethodManager;
        this.botViewsStorage = new BotViewsStorage(botViewManager);

        botViewsStorage.setInstanceSavingEnabled(builder.instanceSavedEnabled);
    }

    public void start() {
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(this.telegramLongPollingBot);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    void onUpdateReceived(Update update) {
        int uid;
        synchronized (this) {
            uid = getUid(update);
        }

        synchronized (userToLock.computeIfAbsent(uid, k -> new Object())) {
            Stack<ViewStackItem> viewStack = botViewsStorage.userViewStack.get(uid);
            if (viewStack == null || viewStack.isEmpty()) {
                botCenterToContextBridge.navigate(initialViewClass, null, update, false);
            }

            viewStack = botViewsStorage.userViewStack.get(uid);

            ViewStackItem viewStackItem = viewStack.peek();
            BotView view = viewStackItem.getView();

            if (update.getCallbackQuery() != null && update.getCallbackQuery().getData() != null) {
                String data = update.getCallbackQuery().getData();

                if (view.menuLinkStates() != null) {
                    for (Class classView : view.menuLinkStates()) {
                        if (classView.getSimpleName().equals(data)) {
                            botCenterToContextBridge.navigate(classView, null, update, false);
                            return;
                        }
                    }
                }

                if (data.equals(STATE_GO_BACK)) {
                    botCenterToContextBridge.goBack(update, false);
                    return;
                } else if (data.equals(STATE_GO_BACK_AS_NEW_MESSAGE)) {
                    botCenterToContextBridge.goBack(update, true);
                    return;
                }

                if (data.length() > 36) {
                    String viewUuid = data.substring(0, 36);
                    String viewData = data.substring(36);
                    BotContext botContext = new UserBotContext(update, botCenterToContextBridge);
                    BotView botView = botViewsStorage.findBotViewByUuid(botContext, viewUuid, uid);
                    if (botView != null) {
                        botView.onCreate(botView.getData());
                        botView.onCallbackQueryDataReceived(update, viewData);
                    } else {
                        System.out.println("Bot view is null, cannot process callback data");
                    }
                } else {
                    System.out.println("Cannot find BotView uuid instance for callbackdata");
                }
            }

            if (update.getMessage() != null) {
                view.onTextReceived(update.getMessage());
            }
        }
    }

    private synchronized boolean sendMessage(Update update, BotView view, BotMessage message) {
        Class[] links = view.menuLinkStates();
        if (links == null) {
            links = new Class[0];
        }

        long chatId = getChatId(update);

        ArrayList<List<InlineKeyboardButton>> listsOfInlineButtons = new ArrayList<>();

        if (message.getInlineButtons() != null && !message.getInlineButtons().isEmpty()) {
            List<List<InlineKeyboardButton>> buttons = message.getInlineButtons()
                    .stream()
                    .map(list -> list.stream().peek(it -> {
                        String callbackData = it.getCallbackData();
                        String newCallbackData = view.getUuid() + it.getCallbackData();
                        if (!TextUtils.isEmpty(callbackData) && !callbackData.contains(newCallbackData)) {
                            it.setCallbackData(newCallbackData);
                        }
                    }).collect(Collectors.toList())).collect(Collectors.toList());
            listsOfInlineButtons.addAll(buttons);
        }

        // Default inline buttons
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();  // TODO split

        List<InlineKeyboardButton> currentSetOfButtons = new ArrayList<>();
        for (Class linkClass : links) {
            currentSetOfButtons.add(new InlineKeyboardButton(botViewManager.getView(view.getContext(), linkClass).name())
                    .setCallbackData(linkClass.getSimpleName()));
            if (currentSetOfButtons.size() >= 2) {
                listsOfInlineButtons.add(currentSetOfButtons);
                currentSetOfButtons = new ArrayList<>();
            }
        }

        if (currentSetOfButtons.size() > 0) {
            listsOfInlineButtons.add(currentSetOfButtons);
        }

        if (!view.getClass().equals(initialViewClass)) {
            listsOfInlineButtons.add(Collections.singletonList(backButton(message.isSendAsNew())));
        }

        inlineKeyboardMarkup.setKeyboard(listsOfInlineButtons);

        if (message.isSendAsNew()) {
            // remove last message if some new needs to be send as new
            chatIdToLastMessage.remove(chatId);
        }

        Integer lastMessageId = chatIdToLastMessage.get(chatId);

        if (TextUtils.isBlank(message.getText())) {
            System.out.println("Message cannot be empty, skip sending");
            return false;
        }

        if (lastMessageId != null) {
            EditMessageText editMessageText = new EditMessageText()
                    .setMessageId(lastMessageId)
                    .setChatId(chatId)
                    .setReplyMarkup(inlineKeyboardMarkup)
                    .setText(message.getText());


            // Send message
            try {
                telegramLongPollingBot.execute(editMessageText);
                return true;
            } catch (TelegramApiException e) {
                e.printStackTrace();
                chatIdToLastMessage.remove(chatId); // cannot edit so send it as general message
                return sendMessage(update, view, message);
            }
        } else {
            SendMessage sendMessage = new SendMessage(chatId, message.getText())
                    .setReplyMarkup(inlineKeyboardMarkup);

            try {
                Message sentMessage = telegramLongPollingBot.execute(sendMessage);
                chatIdToLastMessage.put(chatId, sentMessage.getMessageId());
                return true;
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public class BotCenterToContextBridge {
        private ExecutorService executorService;

        BotCenterToContextBridge() {
            this.executorService = createExecutorService(16);
        }

        private void draw(ViewStackItem viewStackItem, Update update, boolean sendAsNewMessage) {
            viewStackItem.getView().getData().put(Storage.KEY_TELEGRAM_UPDATE, update);
            viewStackItem.getView().onCreate(viewStackItem.getView().getData());

            BotMessage botMessage = new BotMessage(viewStackItem.draw(), sendAsNewMessage);
            sendMessage(botMessage, viewStackItem.getView(), update);
            viewStackItem.getView().onStart();

            botViewsStorage.saveBotViewInstance(viewStackItem.getView(), getUid(update));
        }

        public <T extends BotView> void navigate(Class<T> view, Storage data, Update update, boolean asNewMessage) {
            navigate(view, update, data == null ? new Storage() : data, asNewMessage);
        }

        private <T extends BotView> void navigate(Class<T> targetView, Update update, Storage data, boolean asNewMessage) {
            synchronized (botViewsStorage.userViewStack) {
                int uid = getUid(update);
                BotContext botContext = new UserBotContext(update, this);
                Stack<ViewStackItem> botViews = botViewsStorage.userViewStack.computeIfAbsent(uid, k -> new Stack<>());
                BotView view = botViewManager.getView(botContext, targetView);
                if (!botViews.isEmpty()) {
                    botViews.peek().getView().onStop();
                }

                ViewStackItem viewStackItem = new ViewStackItem(view, data);
                botViews.add(viewStackItem);

                botViewsStorage.userViewStack.put(uid, botViews);

                try {
                    draw(viewStackItem, update, asNewMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

        void goBack(Update update, boolean sendAsNewMessage) {
            synchronized (botViewsStorage.userViewStack) {
                int uid = getUid(update);
                Stack<ViewStackItem> botViews = botViewsStorage.userViewStack.computeIfAbsent(uid, k -> new Stack<>());

                if (botViews.isEmpty()) {
                    System.out.println("Cannot navigate back, stack is empty");
                    return;
                }

                botViews.pop().getView().onStop();

                if (botViews.isEmpty()) {
                    navigate(initialViewClass, null, update, false);
                } else {
                    ViewStackItem viewStackItem = botViews.peek();

                    try {
                        draw(viewStackItem, update, sendAsNewMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    botViewsStorage.userViewStack.put(uid, botViews);
                }
            }
        }

        public AbsSender getTelegramSender() {
            return telegramLongPollingBot;
        }

        public ExecutorService executorService() {
            return executorService;
        }
    }

    public static class Builder {
        private Object dependecyProvider;
        private final String telegramBotName;
        private final String telegramBotToken;
        private final Class initialViewClass;
        private Object telegramMethodManager;
        private boolean instanceSavedEnabled = true;

        public <BV extends BotView> Builder(String telegramBotName, String telegramBotToken, Class<BV> initialViewClass) {
            this.telegramBotName = telegramBotName;
            this.telegramBotToken = telegramBotToken;
            this.initialViewClass = initialViewClass;
        }

        public Builder setDependecyProvider(Object dependecyProvider) {
            this.dependecyProvider = dependecyProvider;
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

        public BotCenter build() {
            return new BotCenter(this);
        }
    }
}
