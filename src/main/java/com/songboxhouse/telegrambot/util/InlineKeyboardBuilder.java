package com.songboxhouse.telegrambot.util;

import com.songboxhouse.telegrambot.CallbackDataManager;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.songboxhouse.telegrambot.CallbackDataManager.STORAGE_KEY_CALLBACK_ACTION;

public class InlineKeyboardBuilder {
    private List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
    private int maxButtonsInARow = 2;


    public InlineKeyboardBuilder(int maxButtonsInARow) {
        this.maxButtonsInARow = maxButtonsInARow;
    }

    public InlineKeyboardBuilder() {
    }

    public InlineKeyboardBuilder addAll(List<List<InlineKeyboardButton>> buttons) {
        this.buttons.addAll(buttons);
        return this;
    }

    InlineKeyboardBuilder appendButton(int row, InlineKeyboardButton button) {
        if (buttons.size() <= row) {
            for (int i = 0; i <= row - buttons.size(); i++) {
                createRow();
            }
        }

        buttons.get(row).add(button);
        return this;
    }

    public InlineKeyboardBuilder appendButtonInNewRow(InlineKeyboardButton button) {
        appendButton(rowCount(), button);
        return this;
    }

    public InlineKeyboardBuilder appendButtonToTheLastRow(InlineKeyboardButton button) {
        appendButton(Math.max(rowCount() - 1, 0), button);
        return this;
    }

    public InlineKeyboardBuilder createRow() {
        buttons.add(new ArrayList<>());
        return this;
    }

    public int rowCount() {
        return buttons.size();
    }

    public List<List<InlineKeyboardButton>> build() {
        List<List<InlineKeyboardButton>> res = new ArrayList<>();

        for (List<InlineKeyboardButton> row : buttons) {
            List<InlineKeyboardButton> resRow = new ArrayList<>();
            for (InlineKeyboardButton button : row) {
                if (resRow.size() >= maxButtonsInARow) {
                    res.add(resRow);
                    resRow = new ArrayList<>();
                }

                resRow.add(button);
            }

            if (!resRow.isEmpty()) {
                res.add(resRow);
            }
        }

        return res;
    }

    public InlineKeyboardBuilder setMaxButtonsInARow(int max) {
        this.maxButtonsInARow = max;
        return this;
    }
}
