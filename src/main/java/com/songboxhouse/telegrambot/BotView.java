package com.songboxhouse.telegrambot;

import com.songboxhouse.telegrambot.context.BotContext;
import com.songboxhouse.telegrambot.util.Storage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.UUID;

public abstract class BotView {
    private final BotContext context;
    private Storage data;
    private String uuid = UUID.randomUUID().toString();

    public BotView(BotContext context) {
        this.context = context;
    }

    public abstract String draw(Storage data);

    public void onTextReceived(Message message) {

    }

    public <BT extends BotView> Class<BT>[] menuLinkStates() {
        return null;
    }

    public abstract String name();

    public void onCreate(Storage data) {

    }

    // This method will be called right after draw is finished
    public void onStart() {

    }

    // This method will be called right view is stoped
    public void onStop() {

    }

    public void onCallbackQueryDataReceived(Update update, String data) {

    }

    public void onTextReceived(Update telegramMessage) {
        this.onTextReceived(telegramMessage.getMessage());
    }

    public BotContext getContext() {
        return context;
    }

    protected void sendMessage(BotMessage message) {
        getContext().sendMessage(message, this);
    }

    protected void sendMessage(String message) {
        getContext().sendMessage(new BotMessage(message, true), this);
    }

    protected void sendMessageOrEdit(String message) {
        getContext().sendMessage(new BotMessage(message, false), this);
    }

    protected <BT extends BotView> void navigate(Class<BT> view) {
        navigate(view, null);
    }

    protected <BT extends BotView> void navigate(Class<BT> view, Storage data) {
        navigate(view, data,false);
    }

    protected <BT extends BotView> void navigate(Class<BT> view, boolean asNewMessage) {
        navigate(view, null, asNewMessage);
    }

    protected <BT extends BotView> void navigate(Class<BT> view, Storage data, boolean asNewMessage) {
        getContext().navigate(view, data, asNewMessage);
    }

    // Save/Restore state in case of click on old button
    protected void onSaveInstanceState(Storage data) {
        data.copyFrom(getData());
    }

    public void onRestoreInstanceState(Storage data) {
        setData(data);
    }

    protected Storage getData() {
        return data;
    }

    void setData(Storage data) {
        this.data = data;
    }

    public String getUuid() {
        return uuid;
    }

    void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
