package com.songboxhouse.telegrambot.view;

import com.songboxhouse.telegrambot.util.Storage;

import java.io.*;
import java.util.Map;

public class BotViewSavedInstance implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Storage data;
    private final Class botViewClass;
    private final int telegramMessageId;
    public String parentViewId;

    BotViewSavedInstance(Storage data, BotView botView) {
        this.data = data;
        this.botViewClass = botView.getClass();
        this.telegramMessageId = botView.getTelegramMessageId();
        this.parentViewId = botView.getParentViewId();
    }

    public Storage getData() {
        return data;
    }

    public Class getBotViewClass() {
        return botViewClass;
    }

    public int getTelegramMessageId() {
        return telegramMessageId;
    }
}
