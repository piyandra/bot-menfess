package com.telegram.menfess.handler;

import com.telegram.menfess.entity.FileType;
import com.telegram.menfess.entity.MenfessData;
import com.telegram.menfess.service.MenfessDataService;
import com.telegram.menfess.utils.ButtonConfirmation;
import com.telegram.menfess.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendMenfessCommand implements CommandProcessor {

    private static final String CONFIRMATION_MESSAGE = "Apakah Anda Ingin mengirim Pesan Ini?";
    
    private final ButtonConfirmation buttonConfirmation;
    private final MenfessDataService menfessDataService;
    private final MessageUtils messageUtils;

    @Override
    public String commands() {
        return "/";
    }

    @Override
    public CompletableFuture<Void> process(Update update, TelegramClient telegramClient) {
        return CompletableFuture.runAsync(() -> {
            Message message = update.getMessage();
            logMessageDetails(message);

            String messageText = message.hasText() ? message.getText() : "";
            if (!isValidMessage(messageText)) {
                return;
            }
            messageUtils.process(message.getCaption() != null ? message.getCaption() : message.getText()).ifPresentOrElse(messages -> {
                MenfessData menfessData = createMenfessData(message);
                sendConfirmationMessage(message, messages, menfessData, telegramClient);
            }, () ->  sendMessage(message.getChatId(), "Pesan harus mengandung minimal 3 kata dan memiliki hashtag", telegramClient));
        });
    }

    private void logMessageDetails(Message message) {
        log.info("Message type debug:");
        log.info("Message {}", message.getText());
        log.info("Has Photo: {}", message.hasPhoto());
        log.info("Has Video: {}", message.hasVideo());
        log.info("Has Text: {}", message.hasText());
        if (message.hasPhoto()) {
            log.info("Photo list size: {}", message.getPhoto().size());
        }
    }

    private boolean isValidMessage(String messageText) {
        if (messageText.isEmpty() || !messageText.startsWith("/")) {
            log.info("Message doesn't start with / or is empty, ignoring");
        }
        return true;
    }

    private MenfessData createMenfessData(Message message) {
        FileType fileType = determineFileType(message);
        String fileId = extractFileId(message, fileType);

        return MenfessData.builder()
                .type(fileType)
                .chatId(message.getChatId())
                .caption(message.getCaption() != null ? message.getCaption() : message.getText())
                .fileId(fileId)
                .messageId(message.getMessageId())
                .build();
    }

    private FileType determineFileType(Message message) {
        if (message.hasVideo()) {
            return FileType.VIDEOS;
        } else if (message.hasPhoto()) {
            return FileType.PHOTOS;
        }
        return FileType.TEXT;
    }

    private String extractFileId(Message message, FileType fileType) {
        switch (fileType) {
            case VIDEOS:
                String videoId = message.getVideo().getFileId();
                log.info("Video ID: {}", videoId);
                return videoId;
            case PHOTOS:
                String photoId = getLargestPhotoFileId(message.getPhoto());
                log.info("Photo ID: {}", photoId);
                return photoId;
            default:
                log.info("Text message");
                return null;
        }
    }

    private String getLargestPhotoFileId(List<PhotoSize> photos) {
        return photos.stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .orElse(photos.get(0))
                .getFileId();
    }

    private void sendConfirmationMessage(Message message, String messageText, 
                                       MenfessData menfessData, TelegramClient telegramClient) {
        String uuid = menfessDataService.saveDataMenfess(menfessData);
        sendMessageWithMarkup(
                message.getChatId(),
                CONFIRMATION_MESSAGE,
                buttonConfirmation.confirmSendMenfess(uuid),
                telegramClient
        );
    }
}