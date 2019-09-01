package com.songboxhouse.telegrambot.view;

import com.songboxhouse.telegrambot.util.InlineKeyboardBuilder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;


public class BotMessage {
    private String text;
    private boolean sendAsNew = true;
    private List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();

    private BotMessage(Builder builder) {
        this(builder.message.toString(), builder.sendAsNew);

        if (builder.inlineKeyboard().rowCount() > 0) {
            setInlineButtons(builder.inlineKeyboard().build());
        }
    }

    public BotMessage(String text, boolean sendAsNew) {
        this.text = text;
        this.sendAsNew = sendAsNew;
    }

    public BotMessage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isSendAsNew() {
        return sendAsNew;
    }

    public void setSendAsNew(boolean sendAsNew) {
        this.sendAsNew = sendAsNew;
    }

    public List<List<InlineKeyboardButton>> getInlineButtons() {
        return inlineButtons;
    }

    public void setInlineButtons(List<List<InlineKeyboardButton>> inlineButtons) {
        this.inlineButtons = inlineButtons;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Builder() {

        }

        private StringBuilder message = new StringBuilder();
        private InlineKeyboardBuilder inlineKeyboardBuilder = new InlineKeyboardBuilder();
        private boolean sendAsNew = true;

        public Builder setSendAsNew(boolean sendAsNew) {
            this.sendAsNew = sendAsNew;
            return this;
        }

        public Builder appendText(String text) {
            message.append(text).append("\n");
            return this;
        }

        public InlineKeyboardBuilder inlineKeyboard() {
            return this.inlineKeyboardBuilder;
        }


        public BotMessage build() {
            return new BotMessage(this);
        }
    }
}
