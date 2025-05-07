package com.telegram.menfess.utils;

import com.telegram.menfess.service.MenfessDataConfirmation;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ButtonConfirmation {

    public InlineKeyboardMarkup confirmSendMenfess(String uuid) {
        List<InlineKeyboardRow> inlineKeyboardRows = new ArrayList<>();

        InlineKeyboardRow inlineKeyboardRow = new InlineKeyboardRow();
        inlineKeyboardRow.add(InlineKeyboardButton
                .builder()
                        .callbackData("send_" + uuid)
                        .text("Send")
                .build());
        inlineKeyboardRow.add(InlineKeyboardButton
                .builder()
                        .text("Cancel")
                        .callbackData("cancel_" + uuid)
                .build());
        inlineKeyboardRows.add(inlineKeyboardRow);
        return InlineKeyboardMarkup.builder().keyboard(inlineKeyboardRows).build();
    }
}
