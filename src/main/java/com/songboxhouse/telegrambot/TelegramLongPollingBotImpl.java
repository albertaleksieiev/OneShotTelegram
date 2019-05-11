package com.songboxhouse.telegrambot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

class TelegramLongPollingBotImpl extends TelegramLongPollingBot {
    private final String botToken;
    private final BotCenter botCenter;
    private final String botName;

    TelegramLongPollingBotImpl(String botToken, String botName, BotCenter botCenter) {
        this.botToken = botToken;
        this.botCenter = botCenter;
        this.botName = botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        botCenter.onUpdateReceived(update);
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
