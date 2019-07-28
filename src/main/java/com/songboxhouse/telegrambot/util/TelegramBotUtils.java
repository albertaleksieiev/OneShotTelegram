package com.songboxhouse.telegrambot.util;

import com.songboxhouse.telegrambot.view.BotView;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.UUID;

import static com.songboxhouse.telegrambot.BotCenter.STATE_GO_BACK;
import static com.songboxhouse.telegrambot.BotCenter.STATE_GO_BACK_AS_NEW_MESSAGE;

public class TelegramBotUtils {
    private static final int UUIDLength = 8;
    public static class UUIDWithData {
        public final String uuid;
        public final String data;

        UUIDWithData(String uuid, String data) {
            this.uuid = uuid;
            this.data = data;
        }
    }

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

    public static InlineKeyboardButton backButton(BotView view, boolean sendAsNew) {
        if (sendAsNew) {
            return new InlineKeyboardButton("🔙")
                    .setCallbackData(createCallbackData(view, STATE_GO_BACK_AS_NEW_MESSAGE));

        } else {
            return new InlineKeyboardButton("🔙")
                    .setCallbackData(createCallbackData(view, STATE_GO_BACK));
        }
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString().substring(0, UUIDLength);
    }

    public static UUIDWithData parseUUIDWithData(String uuid) {
        if (uuid.length() > UUIDLength) {
            String parserUUID = uuid.substring(0, UUIDLength);
            String parserData = uuid.substring(UUIDLength);
            return new UUIDWithData(parserUUID, parserData);
        }
        return null;
    }

    public static String createCallbackData(BotView botView, String data) {
        return botView.getUuid() + data;
    }
}
