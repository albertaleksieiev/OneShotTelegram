package com.songboxhouse.telegrambot.example;

import com.songboxhouse.telegrambot.view.BotView;
import com.songboxhouse.telegrambot.context.BotContext;
import com.songboxhouse.telegrambot.util.SessionStorage;
import com.songboxhouse.telegrambot.util.Storage;
import org.telegram.telegrambots.meta.api.objects.Message;

public class SettingsView extends BotView {
    public SettingsView(BotContext context) {
        super(context);
    }

    @Override
    public String draw(Storage data) {
        return "🖲 View: " + this.getClass().getSimpleName() + ".java\n" +
                "Enter your name";
    }

    @Override
    public String name() {
        return "Configure Name ⚙️";
    }

    @Override
    public void onTextReceived(Message message) {
        super.onTextReceived(message);

        // Get entered text
        String text = message.getText();

        // Save it into session storage
        SessionStorage sessionStorage = getContext().getSessionStorage();
        sessionStorage.put(HomeView.STORAGE_KEY_USERNAME, text);


        Storage data = new Storage();
        data.put(HomeView.ARG_JUST_CONFIGURED, true);

        // And navigate to home view
        navigate(HomeView.class, data, true /* Send it as a new message*/);
    }
}
