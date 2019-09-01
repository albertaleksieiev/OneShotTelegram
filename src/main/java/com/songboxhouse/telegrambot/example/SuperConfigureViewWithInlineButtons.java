package com.songboxhouse.telegrambot.example;

import com.songboxhouse.telegrambot.view.BotMessage;
import com.songboxhouse.telegrambot.view.BotView;
import com.songboxhouse.telegrambot.context.BotContext;
import com.songboxhouse.telegrambot.util.Storage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.songboxhouse.telegrambot.util.InlineKeyboardBuilder.buildButton;

public class SuperConfigureViewWithInlineButtons extends BotView {
    public static final String BUTTON_OK = "ok";
    public static final String BUTTON_CANCEL = "cancel";
    public static final String BUTTON_RERUN_CONFIGURATION = "rerun";
    public static final String ARG_NESTED_LEVEL = "level";

    private static final String COUNTER = "counter";

    Thread thread = null;

    public SuperConfigureViewWithInlineButtons(BotContext context) {
        super(context);
    }

    @Override
    public String draw(Storage data) {
        return null;
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("ON STOP, Stoping threads...");
        thread.interrupt();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (thread != null) {
            thread.interrupt();
        }
        Integer counter = getData().get(COUNTER, Integer.class);
        if (counter == null) {
            getData().put(COUNTER, 1);
        }

        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Integer counter = getData().get(COUNTER, Integer.class);
                while (counter != null && counter < 60 && !Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(3000);
                        redraw();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    void redraw() {
        Integer counter = getData().get(COUNTER, Integer.class);
        if (counter == null) {
            counter = 10000;
        }
        counter += 1;
        getData().put(COUNTER, counter);

        BotMessage.Builder botMessageBuilder = BotMessage.builder()
                .appendText("Counter = " + counter)
                .appendText("Run super configuration for view " + describeView())
                .setSendAsNew(false);
        botMessageBuilder.inlineKeyboard()
                .appendButtonToTheLastRow(buildButton("Ok 👍", BUTTON_OK))
                .appendButtonToTheLastRow(buildButton("No 🙅", BUTTON_CANCEL))
                .appendButtonToTheLastRow(buildButton("Rerun super configuration 💥", BUTTON_RERUN_CONFIGURATION));

        sendMessage(botMessageBuilder.build());
    }


    private String describeView() {
        return "Level 🛤 = " + getData().get(ARG_NESTED_LEVEL, Integer.class) + "\n"
                + "UUID = " + getUuid();
    }

    @Override
    public void onCallbackQueryDataReceived(Update update, String data) {
        super.onCallbackQueryDataReceived(update, data);

        if (data.equals(BUTTON_OK)) {
            sendMessage("OK clicked for view " + describeView(), false);
        } else if (data.equals(BUTTON_CANCEL)) {
            sendMessage("Cancel clicked for view " + describeView(), false);
        } else if (data.equals(BUTTON_RERUN_CONFIGURATION)) {
            Integer level = getData().get(ARG_NESTED_LEVEL, Integer.class);
            if (level == null) {
                level = -2;
            }
            level++;
            Storage storage = new Storage()
                    .put(SuperConfigureViewWithInlineButtons.ARG_NESTED_LEVEL, level);

            navigate(SuperConfigureViewWithInlineButtons.class, storage);
        }
    }

    @Override
    public String name() {
        return "Super configuration 💥";
    }
}
