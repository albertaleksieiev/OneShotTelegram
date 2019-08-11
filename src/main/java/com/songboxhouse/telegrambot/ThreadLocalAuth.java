package com.songboxhouse.telegrambot;

import com.songboxhouse.telegrambot.util.TelegramBotUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

public class ThreadLocalAuth {
    public static class Context {
        private String userName;

        Context(String userName) {
            this.userName = userName;
        }
    }

    private static ThreadLocal<Context> userContext
            = new ThreadLocal<>();

    private static String getUserNameByTelegramUpdate(Update update) {
        return "telegram_" + TelegramBotUtils.getUid(update);
    }

    static void setUser(Update update) {
        userContext.set(new Context(getUserNameByTelegramUpdate(update)));
    }

    protected static void setUser(String userName) {
        userContext.set(new Context(userName));
    }

    public static String getUser() {
        Context context = userContext.get();
        if (context == null) {
            return "";
        }

        return context.userName;
    }
}


