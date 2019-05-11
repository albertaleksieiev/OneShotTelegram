package com.songboxhouse.telegrambot.util;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import static com.songboxhouse.telegrambot.BotCenter.STATE_GO_BACK;
import static com.songboxhouse.telegrambot.BotCenter.STATE_GO_BACK_AS_NEW_MESSAGE;

public class TelegramBotUtils {
    public static int getUid(Update update) {
        Message message = update.getMessage();
        CallbackQuery callbackQuery = update.getCallbackQuery();

        if (message != null) {
            return message.getFrom().getId();
        } else {
            return callbackQuery.getFrom().getId();
        }
    }

    public static long getChatId(Update update) {
        Message message = update.getMessage();
        CallbackQuery callbackQuery = update.getCallbackQuery();

        if (message != null) {
            return message.getChatId();
        } else {
            return callbackQuery.getMessage().getChatId();
        }
    }

    public static InlineKeyboardButton backButton(boolean sendAsNew) {
        if (sendAsNew) {
            return new InlineKeyboardButton("🔙").setCallbackData(STATE_GO_BACK);

        } else {
            return new InlineKeyboardButton("🔙").setCallbackData(STATE_GO_BACK_AS_NEW_MESSAGE);
        }
    }
}
