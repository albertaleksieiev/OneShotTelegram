package com.songboxhouse.telegrambot.example;

import com.songboxhouse.telegrambot.BotMessage;
import com.songboxhouse.telegrambot.context.BotContext;
import com.songboxhouse.telegrambot.util.Storage;
import com.songboxhouse.telegrambot.BotView;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.UUID;

public class HomeView extends BotView {
    public static final String STORAGE_KEY_USERNAME = "STORAGE_KEY_USERNAME";
    public static final String ARG_JUST_CONFIGURED = "ARG_JUST_CONFIGURED";
    public static final String BUTTON_KEY_RUN_SUPER_CONFIG = "run_config";

    private final Main.DBInMemory db;

    public HomeView(BotContext context, Main.DBInMemory db) {
        super(context);
        this.db = db;
    }

    @Override
    public String draw(Storage data) {
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();

        String userName = getContext().getSessionStorage().get(STORAGE_KEY_USERNAME, String.class);
        Boolean justConfigured = getData().get(ARG_JUST_CONFIGURED, Boolean.class);

        String token = db.getUserToken();

        String message = "🖲 View: " + this.getClass().getSimpleName() + ".java\n" +
                "Welcome Home " +
                (userName == null ? "" : userName)
                + "🏠"
                + "\nYour token `" + (token == null ? "?" : token) + "`\n"
                + (justConfigured != null && justConfigured ? "\nAnd thanks for configuration!" : "");


        BotMessage botMessage = new BotMessage(message, false);

        InlineKeyboardButton button = new InlineKeyboardButton("Run super configuration 💥")
                .setCallbackData(BUTTON_KEY_RUN_SUPER_CONFIG);
        botMessage.setInlineButtons(Arrays.asList(Arrays.asList(button)));
        sendMessage(botMessage);
    }

    @Override
    public void onCallbackQueryDataReceived(Update update, String data) {
        super.onCallbackQueryDataReceived(update, data);
        if (data.equals(BUTTON_KEY_RUN_SUPER_CONFIG)) {
            Storage storage = new Storage();
            storage.put(SuperConfigureViewWithInlineButtons.ARG_NAME, UUID.randomUUID().toString().substring(0, 6));
            navigate(SuperConfigureViewWithInlineButtons.class, storage);
        }
    }

    @Override
    public String name() {
        return "Home 🌏"; // This name will appear on menu
    }

    @Override
    public <BT extends BotView> Class<BT>[] menuLinkStates() {
        return new Class[]{SettingsView.class, ConfigureTokenView.class};
    }
}
