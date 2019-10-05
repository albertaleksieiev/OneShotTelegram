package com.songboxhouse.telegrambot;

import com.songboxhouse.telegrambot.util.ExecutorServiceManager;
import com.songboxhouse.telegrambot.util.UpdateReceiveListener;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.ExecutorService;

public class TelegramLongPollingBotImpl extends TelegramLongPollingBot {
    private final String botToken;
    private final BotCenter botCenter;
    private final String botName;
    private ExecutorService telegramExecutorService;
    private UpdateReceiveListener updateReceiveListener;

    TelegramLongPollingBotImpl(String botToken, String botName,
                               UpdateReceiveListener updateReceiveListener,
                               ExecutorServiceManager executorServiceManager,
                               BotCenter botCenter) {
        this.botToken = botToken;
        this.updateReceiveListener = updateReceiveListener;
        this.botCenter = botCenter;
        this.botName = botName;
        telegramExecutorService = executorServiceManager.createExecutorService(16);
    }

    @Override
    public void onUpdateReceived(Update update) {
        telegramExecutorService.submit(() -> {
            if (updateReceiveListener != null) {
                updateReceiveListener.willReceiveUpdate(update);
            }

            botCenter.onUpdateReceived(update);

            if (updateReceiveListener != null) {
                updateReceiveListener.didReceiveUpdate(update);
            }
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
