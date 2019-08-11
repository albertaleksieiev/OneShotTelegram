package com.songboxhouse.telegrambot;

import com.songboxhouse.telegrambot.util.ExecutorUtil;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.ExecutorService;

class TelegramLongPollingBotImpl extends TelegramLongPollingBot {
    private final String botToken;
    private final BotCenter botCenter;
    private final String botName;
    private ExecutorService telegramExecutorService;

    TelegramLongPollingBotImpl(String botToken, String botName, BotCenter botCenter) {
        this.botToken = botToken;
        this.botCenter = botCenter;
        this.botName = botName;
        telegramExecutorService = ExecutorUtil.createExecutorService(16);
    }

    @Override
    public void onUpdateReceived(Update update) {
        telegramExecutorService.submit(() -> {
            ThreadLocalAuth.setUser(update);
            botCenter.onUpdateReceived(update);
        });
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
