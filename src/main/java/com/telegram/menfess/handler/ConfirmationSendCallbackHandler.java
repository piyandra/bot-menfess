package com.telegram.menfess.handler;

import com.telegram.menfess.entity.FileType;
import com.telegram.menfess.entity.MenfessData;
import com.telegram.menfess.entity.Messages;
import com.telegram.menfess.entity.User;
import com.telegram.menfess.service.MenfessDataService;
import com.telegram.menfess.service.MessageService;
import com.telegram.menfess.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfirmationSendCallbackHandler implements CallbackProcessor {
    private static final String MENFESS_HEADER = "<blockquote>#Menfess</blockquote>\n\n";
    private static final String NO_DATA_MESSAGE = "Tidak Ada Data";
    private static final String CALLBACK_PREFIX = "send";

    private final MenfessDataService menfessDataService;
    private final MessageService messageService;
    private final UserService userService;
    @Value("${channel.id}")
    private String channelId;

    @Override
    public String callbackPrefix() {
        return CALLBACK_PREFIX;
    }

    @Override
    @Async
    public CompletableFuture<Void> process(Update update, TelegramClient telegramClient) {
        return CompletableFuture.runAsync(() -> {
            try {
                String messageId = extractMessageId(update);
                MenfessData menfessData = retrieveMenfessData(messageId);
                processMessage(menfessData, telegramClient);
            } catch (Exception e) {
                log.error("Error processing confirmation callback: ", e);
                handleError(update, telegramClient);
            }
        });
    }

    private String extractMessageId(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        String[] data = callbackData.split("_");
        if (data.length < 2) {
            throw new IllegalArgumentException("Invalid callback data format");
        }
        return data[1];
    }

    private MenfessData retrieveMenfessData(String messageId) {
        log.info("Retrieving data for message ID: {}", messageId);
        MenfessData data = menfessDataService.findDataById(messageId);
        if (data == null) {
            throw new IllegalStateException("No menfess data found for ID: " + messageId);
        }
        return data;
    }

    private void processMessage(MenfessData data, TelegramClient telegramClient) {
        String formattedMessage = formatMessage(data.getCaption());

        MessageSender messageSender = getMessageSender(data.getType());
        Message sentMessage = messageSender.sendMessage(data, formattedMessage, telegramClient);

        if (sentMessage != null) {
            saveMessageAndCleanup(data, sentMessage);
        }
    }

    private MessageSender getMessageSender(FileType type) {
        return switch (type) {
            case TEXT -> (data, message, client) -> sendMessage(Long.parseLong(channelId), message, client);
            case PHOTOS -> (data, message, client) -> sendMessageWithPhoto(Long.parseLong(channelId), data.getFileId(), message, client);
            case VIDEOS -> (data, message, client) -> sendVideo(Long.parseLong(channelId), data.getFileId(), message, client);
        };
    }

    private String formatMessage(String caption) {
        return MENFESS_HEADER + (caption != null ? caption : "");
    }

    private void saveMessageAndCleanup(MenfessData data, Message sentMessage) {
        try {
            User user = userService.saveUser(User.builder()
                    .id(data.getChatId())
                    .username(null)
                    .build());

            Messages messages = Messages.builder()
                    .createdAt(System.currentTimeMillis())
                    .messageId(String.valueOf(sentMessage.getMessageId()))
                    .isDeleted(false)
                    .id(user)
                    .build();

            messageService.saveMessage(messages);
            menfessDataService.deleteDataById(data.getId());
        } catch (Exception e) {
            log.error("Error saving message: ", e);
        }
    }

    private void handleError(Update update, TelegramClient telegramClient) {
        if (update.getCallbackQuery() != null && update.getCallbackQuery().getMessage() != null) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            sendMessage(chatId, NO_DATA_MESSAGE, telegramClient);
        }
    }

    @FunctionalInterface
    private interface MessageSender {
        Message sendMessage(MenfessData data, String message, TelegramClient client);
    }
}