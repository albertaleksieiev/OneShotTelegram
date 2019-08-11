package com.songboxhouse.telegrambot.example;

import com.songboxhouse.telegrambot.BotView;
import com.songboxhouse.telegrambot.ThreadLocalAuth;
import com.songboxhouse.telegrambot.context.BotContext;
import com.songboxhouse.telegrambot.util.Storage;

public class TestAuthBotView extends BotView {
    public TestAuthBotView(BotContext context) {
        super(context);
    }

    @Override
    public String draw(Storage data) {
        return "Hello " + ThreadLocalAuth.getUser();
    }

    @Override
    public String name() {
        return "🗝 Test thread local auth";
    }
}
