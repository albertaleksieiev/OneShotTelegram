package com.songboxhouse.telegrambot.view;

import com.songboxhouse.telegrambot.BotCenter;
import com.songboxhouse.telegrambot.CallbackDataManager;
import com.songboxhouse.telegrambot.util.Storage;
import com.songboxhouse.telegrambot.util.TelegramBotUtils;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import static com.songboxhouse.telegrambot.CallbackDataManager.STORAGE_KEY_CALLBACK_ACTION;

public abstract class BotView {
    public static final String PARENT_ID_ROOT = "root";
    private final BotContext context;
    private Storage data = new Storage();
    private String uuid = TelegramBotUtils.generateUUID();
    private int telegramMessageId = 0;
    private String parentViewId;

    private boolean isDrawed = false;
    private boolean isCreated = false;
    protected boolean isDestroyed = false;

    public BotView(BotContext context) {
        this.context = context;
    }

    public abstract String draw(Storage data);

    public void onTextReceived(Message message) {

    }

    public <BT extends BotView> Class<BT>[] menuLinkStates() {
        return null;
    }
    public boolean menuLinksAsNewMessage() {
        return false;
    }

    public abstract String name();

    public void onCreate(Storage data) {
        isCreated = true;
    }

    // This method will be called right after draw is finished
    public void onStart() {
        isDestroyed = false;
    }

    // This method will be called right view is stoped
    public void onStop() {
        isDestroyed = true;
    }

    public void onCallbackQueryDataReceived(Update update, String action, Storage data) {

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
        sendMessage(message, true);
    }

    protected void sendMessage(String message, boolean asNew) {
        getContext().sendMessage(new BotMessage(message, asNew), this);
    }

    protected void sendMessageOrEdit(String message) {
        getContext().sendMessage(new BotMessage(message, false), this);
    }

    protected <BT extends BotView> void navigate(Class<BT> view) {
        navigate(view, null);
    }

    protected <BT extends BotView> void navigate(Class<BT> view, Storage data) {
        navigate(view, data, false);
    }

    protected <BT extends BotView> void navigate(Class<BT> view, boolean asNewMessage) {
        navigate(view, null, asNewMessage);
    }

    protected <BT extends BotView> void navigate(Class<BT> view, Storage data, boolean asNewMessage) {
        getContext().navigate(this, view, data, asNewMessage);
    }

    // Save/Restore state in case of click on old button
    public void onSaveInstanceState(Storage data) {
        data.copyFrom(getData());
    }

    public void onRestoreInstanceState(Storage data) {
        setData(data);
    }

    public Storage getData() {
        return data;
    }

    public void setData(Storage data) {
        this.data = data;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getTelegramMessageId() {
        return telegramMessageId;
    }

    public void setTelegramMessageId(int telegramMessageId) {
        this.telegramMessageId = telegramMessageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotView botView = (BotView) o;
        return Objects.equals(uuid, botView.uuid);
    }

    @Override
    public int hashCode() {

        return Objects.hash(uuid);
    }

    public String getParentViewId() {
        return parentViewId;
    }

    public void setParentViewId(String parentViewId) {
        this.parentViewId = parentViewId;
    }

    public void setParentView(BotView parentView) {
        if (parentView == null) {
            setParentViewId(PARENT_ID_ROOT);
        } else {
            setParentViewId(parentView.getUuid());
        }
    }


    public void draw(BotCenter.BotCenterToContextBridge botCenterToContextBridge, Update update, boolean sendAsNewMessage) {
        if (update != null) {
            getData().put(Storage.KEY_TELEGRAM_UPDATE, update);
        }

        onCreate(getData());

        BotMessage botMessage = new BotMessage(draw(getData()), sendAsNewMessage);
        if (botCenterToContextBridge != null && update != null) {
            botCenterToContextBridge.sendMessage(botMessage, this, update);
        } else {
            System.out.println("Bot center is null, skip send message");
        }
        onStart();
    }

    void evaluateOnCreateAndOnRestore() {
        if (isCreated) {
            return;
        }

        isCreated = true;

        onCreate(getData());
        onRestoreInstanceState(getData());
    }

    public BotView clone(BotViewManager botViewManager,
                         BotCenter.BotCenterToContextBridge bridge,
                         Update update) {
        BotView botView = botViewManager.buildView(bridge.buildContext(update), this.getClass());
        botView.setParentViewId(this.getParentViewId());
        botView.data = this.data.clone();
        return botView;
    }


    // Todo move somewhere else
    public InlineKeyboardButton buildButton(String text, String action) {
        return buildButton(text, action, null);
    }

    public InlineKeyboardButton buildButton(String text, String action, Storage data) {
        if (data == null) {
            data = new Storage();
        }

        try {
            CallbackDataManager callbackDataManager = getContext().getBotCenterToContextBridge().getCallbackDataManager();

            data.put(STORAGE_KEY_CALLBACK_ACTION, action);
            return new InlineKeyboardButton()
                    .setText(text)
                    .setCallbackData(callbackDataManager.saveStorage(getContext().getTelegramUpdate(), data));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public InlineKeyboardButton buildButton(String text, URL url) {
        return new InlineKeyboardButton()
                .setText(text)
                .setUrl(url.toString());
    }
}
