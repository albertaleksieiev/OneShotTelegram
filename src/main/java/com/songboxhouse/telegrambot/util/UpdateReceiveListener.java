package com.songboxhouse.telegrambot.util;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateReceiveListener {
    public void willReceiveUpdate(Update update);
    public void didReceiveUpdate(Update update);
}

