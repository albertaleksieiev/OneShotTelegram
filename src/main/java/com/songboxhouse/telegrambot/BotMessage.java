package com.songboxhouse.telegrambot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;


public class BotMessage {
    private String text;
    private boolean sendAsNew = true;
    private List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();

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
}
