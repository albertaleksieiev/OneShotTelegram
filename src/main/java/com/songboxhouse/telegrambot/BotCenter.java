package com.songboxhouse.telegrambot;


import com.songboxhouse.telegrambot.context.BotContext;
import com.songboxhouse.telegrambot.context.UserBotContext;
import com.songboxhouse.telegrambot.util.SessionStorage;
import com.songboxhouse.telegrambot.util.Storage;
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
import java.util.stream.Collectors;

import static com.songboxhouse.telegrambot.util.ExecutorUtil.createExecutorService;
import static com.songboxhouse.telegrambot.util.TelegramBotUtils.backButton;
import static com.songboxhouse.telegrambot.util.TelegramBotUtils.getChatId;
import static com.songboxhouse.telegrambot.util.TelegramBotUtils.getUid;


public class BotCenter {
    public static final String STATE_GO_BACK = "com.songboxhouse.telegrambot.STATE_GO_BACK";
    public static final String STATE_GO_BACK_AS_NEW_MESSAGE = "com.songboxhouse.telegrambot.STATE_GO_BACK";

    private final BotViewManager botViewManager;
    private Class<? extends BotView> initialViewClass;
    private final TelegramLongPollingBot telegramLongPollingBot;
    private BotCenterToContextBridge botCenterToContextBridge = new BotCenterToContextBridge();

    private Map<Integer, SessionStorage> userToSessionStorage = new ConcurrentHashMap<>();
    private Map<Integer, Object> userToLock = new ConcurrentHashMap<>();
    private final Map<Integer, Stack<ViewStackItem>> userViewStack = new ConcurrentHashMap<>();
    private Map<Long, Integer> chatIdToLastMessage = new ConcurrentHashMap<>();

    public <BV extends BotView> BotCenter(String telegramBotName,
                                          String telegramBotToken,
                                          DependecyProvider dependecyProvider,
                                          Class<BV> initialViewClass) {

        ApiContextInitializer.init(); /* Require for new Telegram bot */
        this.initialViewClass = initialViewClass;
        this.telegramLongPollingBot = new TelegramLongPollingBotImpl(telegramBotToken, telegramBotName, this);
        this.botViewManager = new BotViewManager(dependecyProvider);
    }


    public void start() {
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            botsApi.registerBot(this.telegramLongPollingBot);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    public void onUpdateReceived(Update update) {
        int uid;
        synchronized (this) {
            uid = getUid(update);
        }

        synchronized (userToLock.computeIfAbsent(uid, k -> new Object())) {
            Stack<ViewStackItem> viewStack = userViewStack.get(uid);
            if (viewStack == null || viewStack.isEmpty()) {
                botCenterToContextBridge.navigate(initialViewClass, null, update, false);
            }

            viewStack = userViewStack.get(uid);

            ViewStackItem viewStackItem = viewStack.peek();
            BotView view = viewStackItem.getView();

            if (update.getCallbackQuery() != null && update.getCallbackQuery().getData() != null) {
                String data = update.getCallbackQuery().getData();

                if (view.menuLinkStates() != null) {
                    for (Class classView : view.menuLinkStates()) {
                        if (classView.getName().equals(data)) {
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

                view.onCallbackQueryDataReceived(update, data);
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
            listsOfInlineButtons.addAll(message.getInlineButtons());
        }

        // Default inline buttons
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();  // TODO split
        List<InlineKeyboardButton> defaultButtons = Arrays.stream(links)
                .map(it -> new InlineKeyboardButton(botViewManager.getView(view.getContext(), it).name())
                        .setCallbackData(it.getName()))
                .collect(Collectors.toList());

        listsOfInlineButtons.add(defaultButtons);

        if (!view.getClass().equals(initialViewClass)) {
            defaultButtons.add(backButton(message.isSendAsNew()));
        }

        inlineKeyboardMarkup.setKeyboard(listsOfInlineButtons);

        if (message.isSendAsNew()) {
            // remove last message if some new needs to be send as new
            chatIdToLastMessage.remove(chatId);
        }

        Integer lastMessageId = chatIdToLastMessage.get(chatId);
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
            BotMessage botMessage = new BotMessage(viewStackItem.draw(), sendAsNewMessage);
            sendMessage(botMessage, viewStackItem.getView(), update);
            viewStackItem.getView().onStart();
        }

        public <T extends BotView> void navigate(Class<T> view, Storage data, Update update, boolean asNewMessage) {
            navigate(view, update, data == null ? new Storage() : data, asNewMessage);
        }

        private <T extends BotView> void navigate(Class<T> targetView, Update update, Storage data, boolean asNewMessage) {
            synchronized (userViewStack) {
                int uid = getUid(update);
                BotContext botContext = new UserBotContext(update, this);
                Stack<ViewStackItem> botViews = userViewStack.computeIfAbsent(uid, k -> new Stack<>());
                BotView view = botViewManager.getView(botContext, targetView);
                if (!botViews.isEmpty()) {
                    botViews.peek().getView().onStop();
                }

                ViewStackItem viewStackItem = new ViewStackItem(view, data);
                botViews.add(viewStackItem);

                userViewStack.put(uid, botViews);

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
            synchronized (userViewStack) {
                int uid = getUid(update);
                Stack<ViewStackItem> botViews = userViewStack.computeIfAbsent(uid, k -> new Stack<>());

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

                    userViewStack.put(uid, botViews);
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
}
