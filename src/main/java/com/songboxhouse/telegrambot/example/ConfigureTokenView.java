package com.songboxhouse.telegrambot.example;

import com.songboxhouse.telegrambot.view.BotView;
import com.songboxhouse.telegrambot.view.BotContext;
import com.songboxhouse.telegrambot.util.Storage;
import org.telegram.telegrambots.meta.api.objects.Message;

public class ConfigureTokenView extends BotView {
    private final Main.DBInMemory db;

    public ConfigureTokenView(BotContext context, Main.DBInMemory db) {
        super(context);
        this.db = db;
    }

    @Override
    public String draw(Storage data) {
        String token = db.getUserToken();
        return "🖲 View: " + this.getClass().getSimpleName()+ ".java\n" +
                "Your token `" + (token == null ? "?" : token) +"`\n"
                + "Enter new token: ";
    }

    @Override
    public void onTextReceived(Message message) {
        super.onTextReceived(message);
        String text = message.getText();

        db.setUserToken(text);

        navigate(HomeView.class, true);
    }

    @Override
    public String name() {
        return "Configure Token 🔑";
    }
}
