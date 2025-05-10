package com.telegram.menfess.handler;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.CompletableFuture;

@Component
public interface CallbackProcessor {

    Logger log = org.slf4j.LoggerFactory.getLogger(CallbackProcessor.class);

    String callbackPrefix();

    CompletableFuture<Void> process(Update update, TelegramClient telegramClient);
    default Message sendMessage(long chatId, String text, TelegramClient telegramClient) {
        try {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .parseMode("HTML")
                    .build();
            return telegramClient.execute(message);
        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage());
            return null;
        }
    }

    default void editMessageSuccessSendMenfess(long chatId, int messageId, String text, InlineKeyboardMarkup inlineKeyboardMarkup, TelegramClient telegramClient) {
        try {
            EditMessageText message = EditMessageText.builder()
                    .messageId(messageId)
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(inlineKeyboardMarkup)
                    .build();
            telegramClient.execute(message);
        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage());
        }
    }
    default Message sendMessageWithPhoto(long chatId, String photoId, String caption, TelegramClient telegramClient) {
        try {
            SendPhoto sendPhoto = SendPhoto.builder()
                    .photo(new InputFile(photoId))
                    .caption(caption)
                    .parseMode("HTML")
                    .chatId(chatId)
                    .build();
            return telegramClient.execute(sendPhoto);
        } catch (Exception e) {
            log.error("Error sending Photo: {}", e.getMessage());
            return null;
        }
    }
    default Message sendVideo(long chatId, String videoId ,String caption, TelegramClient telegramClient) {
        try {
            SendVideo sendVideo = SendVideo.builder()
                    .video(new InputFile(videoId))
                    .caption(caption)
                    .parseMode("HTML")
                    .chatId(chatId)
                    .build();
            return telegramClient.execute(sendVideo);
        } catch (Exception e) {
            log.error("Error sending Video: {}", e.getMessage());
            return null;
        }
    }
}
