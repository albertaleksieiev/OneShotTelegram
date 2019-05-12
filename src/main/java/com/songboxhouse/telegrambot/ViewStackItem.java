package com.songboxhouse.telegrambot;

import com.songboxhouse.telegrambot.util.Storage;

public class ViewStackItem {
    private BotView view;

    public ViewStackItem(BotView view, Storage data) {
        this.view = view;
        this.view.setData(data);
    }

    public BotView getView() {
        return view;
    }

    public void setView(BotView view) {
        this.view = view;
    }

    String draw() {
        return getView().draw(view.getData());
    }
}
