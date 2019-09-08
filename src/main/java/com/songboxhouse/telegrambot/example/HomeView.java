package com.songboxhouse.telegrambot.example;

import com.songboxhouse.telegrambot.view.BotMessage;
import com.songboxhouse.telegrambot.view.BotContext;
import com.songboxhouse.telegrambot.util.Storage;
import com.songboxhouse.telegrambot.view.BotView;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public class HomeView extends BotView {
    public static final String STORAGE_KEY_USERNAME = "STORAGE_KEY_USERNAME";
    public static final String ARG_JUST_CONFIGURED = "ARG_JUST_CONFIGURED";
    public static final String BUTTON_KEY_RUN_SUPER_CONFIG = "run_config";
    public static final String ARG_CONFIGURATION_START_LEVEL = "ARG_CONFIGURATION_START_LEVEL";

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

        BotMessage.Builder messageBuilder = BotMessage.builder()
                .appendText("🖲 View: " + this.getClass().getSimpleName() + ".java")
                .appendText("🎃" + getUuid())
                .appendText("Welcome Home " + (userName == null ? "" : userName))
                .appendText("🏠")
                .appendText("Your token `" + (token == null ? "?" : token) + "`")
                .appendText(justConfigured != null && justConfigured ? "\nAnd thanks for configuration!" : "")
                .setSendAsNew(false);

        messageBuilder.inlineKeyboard()
                .appendButtonToTheLastRow(
                        buildButton("Run super configuration 💥",
                                BUTTON_KEY_RUN_SUPER_CONFIG,
                                new Storage().put(ARG_CONFIGURATION_START_LEVEL, 5))
                );

        sendMessage(messageBuilder.build());
    }

    @Override
    public void onTextReceived(Message message) {
        super.onTextReceived(message);
        navigate(HomeView.class, true);
    }

    @Override
    public void onCallbackQueryDataReceived(Update update, String action, Storage data) {
        super.onCallbackQueryDataReceived(update, action, data);
        if (action.equals(BUTTON_KEY_RUN_SUPER_CONFIG)) {
            Storage storage = new Storage()
                    .put(SuperConfigureViewWithInlineButtons.ARG_NESTED_LEVEL, data.get(ARG_CONFIGURATION_START_LEVEL, Integer.class));

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
                TestAuthBotView.class
        };
    }

    @Override
    public void onSaveInstanceState(Storage data) {
        super.onSaveInstanceState(data);
        data.put(STORAGE_KEY_USERNAME, getContext().getSessionStorage().get(STORAGE_KEY_USERNAME, String.class));
    }
}
