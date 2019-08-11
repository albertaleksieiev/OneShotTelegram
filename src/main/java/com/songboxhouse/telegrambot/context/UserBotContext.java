package com.songboxhouse.telegrambot.context;

import com.songboxhouse.telegrambot.BotCenter;
import com.songboxhouse.telegrambot.view.BotMessage;
import com.songboxhouse.telegrambot.util.SessionStorage;
import com.songboxhouse.telegrambot.util.Storage;
import com.songboxhouse.telegrambot.view.BotView;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.concurrent.ExecutorService;

public class UserBotContext extends BotContext {
    private Update update;
    private BotCenter.BotCenterToContextBridge botCenterToContextBridge;

    public UserBotContext(Update update, BotCenter.BotCenterToContextBridge botCenterToContextBridge) {
        this.update = update;
        this.botCenterToContextBridge = botCenterToContextBridge;
    }

    @Override
    public <BT extends BotView> void navigate(BotView callerView, Class<BT> view, Storage data, boolean asNewMessage) {
        botCenterToContextBridge.navigate(callerView, view, data, update, asNewMessage);
    }

    @Override
    public boolean sendMessage(BotMessage message, BotView botView) {
        return botCenterToContextBridge.sendMessage(message, botView, update);
    }

    @Override
    public AbsSender getTelegramSender() {
        return botCenterToContextBridge.getTelegramSender();
    }

    @Override
    public ExecutorService getExecutorService() {
        return botCenterToContextBridge.executorService();
    }

    @Override
    public SessionStorage getSessionStorage() {
        return botCenterToContextBridge.getSessionStorage(update);
    }

    @Override
    public Update getTelegramUpdate() {
        return update;
    }

    public BotCenter.BotCenterToContextBridge getBotCenterToContextBridge() {
        return botCenterToContextBridge;
    }
}
