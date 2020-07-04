package com.songboxhouse.telegrambot.example;

import com.google.common.base.Strings;
import com.songboxhouse.telegrambot.util.Storage;
import com.songboxhouse.telegrambot.view.BotContext;
import com.songboxhouse.telegrambot.view.BotMessage;
import com.songboxhouse.telegrambot.view.BotView;
import org.apache.http.util.TextUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.Nullable;

import static com.songboxhouse.telegrambot.example.R.LOCALE_EN;
import static com.songboxhouse.telegrambot.example.R.LOCALE_RU;

public class LocalizationExampleView extends BotView {
    public static final String ACTION_EN = "AEN";
    public static final String ACTION_RUS = "ARUS";

    private final Main.DBInMemory db;

    public LocalizationExampleView(BotContext context, Main.DBInMemory db) {
        super(context);
        this.db = db;
    }

    void redraw() {
        BotMessage.Builder builder = BotMessage.builder();
        builder.appendText(getString(R.string.LocalizationViewDescription.ordinal()));
        builder.inlineKeyboard()
                .appendButtonInNewRow(buildButton("Рус 🇷🇺", ACTION_RUS))
                .appendButtonToTheLastRow(buildButton("En 🏴󠁧󠁢󠁥󠁮󠁧󠁿", ACTION_EN));

        sendMessage(builder.build());
    }
    
    @Nullable
    @Override
    public String draw(Storage data) {
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        redraw();
    }

    @Override
    public String name() {
        return getString(R.string.LocalizationViewTitle.ordinal()); // use enum::ordinal as ID
    }

    @Override
    public void onCallbackQueryDataReceived(Update update, String action, Storage data) {
        super.onCallbackQueryDataReceived(update, action, data);
        if (ACTION_EN.equals(action)) {
            db.setLocale(update, LOCALE_EN);
        } else if (ACTION_RUS.equals(action)) {
            db.setLocale(update, LOCALE_RU);
        }
        redraw();
    }
}
