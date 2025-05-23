package com.telegram.menfess.handler;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
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
    default Message sendAudio(long chatId, String audioId, String message, TelegramClient telegramClient) {
        try {
            SendVoice audio = SendVoice.builder()
                    .voice(new InputFile(audioId))
                    .chatId(chatId)
                    .caption(message)
                    .parseMode("HTML")
                    .build();
            return telegramClient.execute(audio);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default void editMessageSuccessSendMenfess(long chatId, int messageId, String text, InlineKeyboardMarkup inlineKeyboardMarkup, TelegramClient telegramClient) {
        try {
            EditMessageText message = EditMessageText.builder()
                    .messageId(messageId)
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(inlineKeyboardMarkup)
                    .parseMode("HTML")
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

    default void editMessageReplyToUserMenfess(Long chatId, int messageId, String text, InlineKeyboardMarkup inlineKeyboardMarkup, TelegramClient telegramClient) {
        try {
            EditMessageText message = EditMessageText.builder()
                    .messageId(messageId)
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(inlineKeyboardMarkup)
                    .parseMode("HTML")
                    .build();
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error Edit message: {}", e.getMessage());
        }
    }
}
