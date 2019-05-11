package com.songboxhouse.telegrambot.example;

import com.songboxhouse.telegrambot.context.BotContext;
import com.songboxhouse.telegrambot.util.Storage;
import com.songboxhouse.telegrambot.BotView;

public class HomeView extends BotView {
    public static final String STORAGE_KEY_USERNAME = "STORAGE_KEY_USERNAME";
    public static final String ARG_JUST_CONFIGURED = "ARG_JUST_CONFIGURED";
    private final Main.DBInMemory db;

    public HomeView(BotContext context, Main.DBInMemory db) {
        super(context);
        this.db = db;
    }

    @Override
    public String draw(Storage data) {
        String userName = getContext().getSessionStorage().get(STORAGE_KEY_USERNAME, String.class);
        Boolean justConfigured = data.get(ARG_JUST_CONFIGURED, Boolean.class);

        String token = db.getUserToken();

        return "🖲 View: " + this.getClass().getSimpleName()+ ".java\n" +
                "Welcome Home " +
                (userName == null ? "" : userName)
                + "🏠"
                + "\nYour token `" + (token == null ? "?" : token) + "`\n"
                + (justConfigured != null && justConfigured ? "\nAnd thanks for configuration!" : "");
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
