package com.songboxhouse.telegrambot.view;

import com.songboxhouse.telegrambot.BotCenter;
import com.songboxhouse.telegrambot.view.BotMessage;
import com.songboxhouse.telegrambot.util.SessionStorage;
import com.songboxhouse.telegrambot.util.Storage;
import com.songboxhouse.telegrambot.view.BotView;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.concurrent.ExecutorService;

public abstract class BotContext {
    public abstract <BT extends BotView> void navigate(BotView callerView, Class<BT> view, Storage data, boolean asNewMessage);
    public abstract boolean sendMessage(BotMessage message, BotView botView);
    public abstract AbsSender getTelegramSender();

    public abstract ExecutorService getExecutorService();

    public abstract SessionStorage getSessionStorage();

    public abstract Update getTelegramUpdate();

    protected abstract BotCenter.BotCenterToContextBridge getBotCenterToContextBridge();
}
