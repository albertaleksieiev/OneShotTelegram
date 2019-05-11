package com.songboxhouse.telegrambot.context;

import com.songboxhouse.telegrambot.BotMessage;
import com.songboxhouse.telegrambot.util.SessionStorage;
import com.songboxhouse.telegrambot.util.Storage;
import com.songboxhouse.telegrambot.BotView;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.concurrent.ExecutorService;

public abstract class BotContext {
    public abstract <BT extends BotView> void navigate(Class<BT> view, Storage data, boolean asNewMessage);
    public abstract boolean sendMessage(BotMessage message, BotView botView);
    public abstract AbsSender getTelegramSender();

    public abstract ExecutorService getExecutorService();

    public abstract SessionStorage getSessionStorage();
}
