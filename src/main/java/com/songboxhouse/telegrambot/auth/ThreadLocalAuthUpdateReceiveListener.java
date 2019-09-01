package com.songboxhouse.telegrambot.auth;

import com.songboxhouse.telegrambot.util.UpdateReceiveListener;
import org.telegram.telegrambots.meta.api.objects.Update;

public class ThreadLocalAuthUpdateReceiveListener implements UpdateReceiveListener {
    @Override
    public void willReceiveUpdate(Update update) {
        ThreadLocalAuth.setUser(update);
    }

    @Override
    public void didReceiveUpdate(Update update) {

    }
}