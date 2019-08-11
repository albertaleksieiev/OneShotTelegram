package com.songboxhouse.telegrambot.example;

import com.songboxhouse.telegrambot.anotations.TelegramMethod;

public class TelegramMethodManager {
    @TelegramMethod(method = "/start", description = "Start Jurney")
    public void start() {

    }
}
