package com.songboxhouse.telegrambot;

import com.songboxhouse.telegrambot.util.Storage;

public class ViewStackItem {
    private BotView view;
    private Storage data;

    public ViewStackItem(BotView view, Storage data) {
        this.view = view;
        this.data = data;
    }

    public BotView getView() {
        return view;
    }

    public void setView(BotView view) {
        this.view = view;
    }

    public Storage getData() {
        return data;
    }

    public void setData(Storage data) {
        this.data = data;
    }

    String draw() {
        return getView().draw(getData());
    }
}
