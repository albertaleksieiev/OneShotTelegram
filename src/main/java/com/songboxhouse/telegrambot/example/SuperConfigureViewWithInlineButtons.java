package com.songboxhouse.telegrambot.example;

import com.songboxhouse.telegrambot.BotMessage;
import com.songboxhouse.telegrambot.BotView;
import com.songboxhouse.telegrambot.context.BotContext;
import com.songboxhouse.telegrambot.util.Storage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SuperConfigureViewWithInlineButtons extends BotView {
    public static final String BUTTON_OK = "ok";
    public static final String BUTTON_CANCEL = "cancel";
    public static final String ARG_NAME = "ARG_NAME";

    public SuperConfigureViewWithInlineButtons(BotContext context) {
        super(context);
    }

    @Override
    public String draw(Storage data) {
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        BotMessage message = new BotMessage("Run super configuration for view " + describeView());

        ArrayList<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        buttons.add(buildButton("Ok 👍", BUTTON_OK));
        buttons.add(buildButton("No 🙅", BUTTON_CANCEL));
        message.setInlineButtons(buttons);
        message.setSendAsNew(false);
        sendMessage(message);
    }

    private String describeView() {
        return getData().get(ARG_NAME, String.class) + "\n"
                + "UUID = " + getUuid();
    }

    private List<InlineKeyboardButton> buildButton(String text, String data) {
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton()
                .setText(text)
                .setCallbackData(data);
        return Arrays.asList(new InlineKeyboardButton[]{inlineKeyboardButton});
    }

    @Override
    public void onCallbackQueryDataReceived(Update update, String data) {
        super.onCallbackQueryDataReceived(update, data);

        if (data.equals(BUTTON_OK)) {
            sendMessage("OK clicked for view " + describeView());
        } else if (data.equals(BUTTON_CANCEL)) {
            sendMessage("Cancel clicked for view " + describeView());
        }
    }

    @Override
    public String name() {
        return "Super configuration 💥";
    }
}
