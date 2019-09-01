package com.songboxhouse.telegrambot.example;

import com.songboxhouse.telegrambot.util.TelegramBotUtils;
import com.songboxhouse.telegrambot.view.BotMessage;
import com.songboxhouse.telegrambot.context.BotContext;
import com.songboxhouse.telegrambot.util.Storage;
import com.songboxhouse.telegrambot.view.BotView;
import org.telegram.telegrambots.meta.api.objects.Message;
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
        if (userName == null) {
            userName = getData().get(STORAGE_KEY_USERNAME, String.class);
        }
        Boolean justConfigured = getData().get(ARG_JUST_CONFIGURED, Boolean.class);

        String token = db.getUserToken();

        String message = "🖲 View: " + this.getClass().getSimpleName() + ".java\n" +
                "🎃" + getUuid() + "\n" +
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
    public void onTextReceived(Message message) {
        super.onTextReceived(message);
        navigate(HomeView.class, true);
    }

    @Override
    public void onCallbackQueryDataReceived(Update update, String data) {
        super.onCallbackQueryDataReceived(update, data);
        if (data.equals(BUTTON_KEY_RUN_SUPER_CONFIG)) {
            Storage storage = new Storage()
                    .put(SuperConfigureViewWithInlineButtons.ARG_NESTED_LEVEL, 1);

            navigate(SuperConfigureViewWithInlineButtons.class, storage, true);
        }
    }

    @Override
    public String name() {
        return "Home 🌏"; // This name will appear on menu
    }

    public <BT extends BotView> Class<BT>[] menuLinkStates() {
        return new Class[]{
                SettingsView.class,
                ConfigureTokenView.class,
                ConfigureTokenView.class,
                TestAuthBotView.class
        };
    }

    @Override
    public void onSaveInstanceState(Storage data) {
        super.onSaveInstanceState(data);
        data.put(STORAGE_KEY_USERNAME, getContext().getSessionStorage().get(STORAGE_KEY_USERNAME, String.class));
    }
}
