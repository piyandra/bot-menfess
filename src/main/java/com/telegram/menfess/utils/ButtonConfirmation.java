package com.telegram.menfess.utils;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class ButtonConfirmation {

    private static final String[] SEND_BUTTON_STYLES = {
            "✅ Kirim",
            "📤 Kirim",
            "🚀 Kirim",
            "✨ Kirim",
            "💌 Kirim"
    };

    private static final String[] CANCEL_BUTTON_STYLES = {
            "❌ Batal",
            "🚫 Batal",
            "↩️ Batal",
            "⛔ Batal",
            "🔙 Batal"
    };

    public InlineKeyboardMarkup confirmSendMenfess(String uuid) {
        Random random = new Random();
        int styleIndex = random.nextInt(SEND_BUTTON_STYLES.length);
        
        List<InlineKeyboardRow> inlineKeyboardRows = new ArrayList<>();
        InlineKeyboardRow inlineKeyboardRow = new InlineKeyboardRow();

        inlineKeyboardRow.add(InlineKeyboardButton
                .builder()
                .callbackData("send_" + uuid)
                .text(SEND_BUTTON_STYLES[styleIndex])
                .build());

        inlineKeyboardRow.add(InlineKeyboardButton
                .builder()
                .text(CANCEL_BUTTON_STYLES[styleIndex])
                .callbackData("cancel_" + uuid)
                .build());
        
        inlineKeyboardRows.add(inlineKeyboardRow);
        return InlineKeyboardMarkup.builder().keyboard(inlineKeyboardRows).build();
    }
    public InlineKeyboardMarkup notJoinedChannel(String channelUsername, String callbackData) {
        List<InlineKeyboardRow> inlineKeyboardRows = new ArrayList<>();
        InlineKeyboardRow inlineKeyboardRow = new InlineKeyboardRow();

        inlineKeyboardRow.add(InlineKeyboardButton
                .builder()
                        .url(String.format("https://t.me/%s", channelUsername))
                        .text("Join Disini")
                .build());
        inlineKeyboardRow.add(InlineKeyboardButton
                .builder()
                .callbackData("send_" + callbackData)
                .text("Coba Lagi")
                .build());


        inlineKeyboardRows.add(inlineKeyboardRow);
        return InlineKeyboardMarkup.builder().keyboard(inlineKeyboardRows).build();
    }

    public InlineKeyboardMarkup replyAnonymous(long groupId, int messageId, int messageReplyId) {
        List<InlineKeyboardRow> inlineKeyboardRows = new ArrayList<>();
        InlineKeyboardRow inlineKeyboardRow = new InlineKeyboardRow();

        inlineKeyboardRow.add(InlineKeyboardButton
                .builder()
                .callbackData("reply_" + groupId + "_" + messageId + "_" + messageReplyId)
                .text("Balas Pesan")
                .build());
        inlineKeyboardRows.add(inlineKeyboardRow);
        return InlineKeyboardMarkup.builder().keyboard(inlineKeyboardRows).build();
    }
}