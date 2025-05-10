package com.telegram.menfess.handler;

import com.telegram.menfess.entity.FileType;
import com.telegram.menfess.entity.MenfessData;
import com.telegram.menfess.entity.Messages;
import com.telegram.menfess.service.MenfessDataService;
import com.telegram.menfess.service.MessageService;
import com.telegram.menfess.utils.ButtonConfirmation;
import com.telegram.menfess.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
    private final MessageService messageService;

    @Value("${channel.username}")
    private String channelUsername;

    @Override
    public String commands() {
        return "/";
    }

    @Override
    public CompletableFuture<Void> process(Update update, TelegramClient telegramClient) {
        return CompletableFuture.runAsync(() -> {
            Message message = update.getMessage();
        
            String messageText = message.hasText() ? message.getText() : "";
            if (isGroupMessageWithReply(message)) {
                handleGroupReply(update, telegramClient);
                return;
            }
            if (!isValidMessage(messageText) || !message.isUserMessage()) {
                return;
            }
            processValidMessage(message, telegramClient);
        });
    }

    private boolean isGroupMessageWithReply(Message message) {
        return (message.getChat().isGroupChat() || message.getChat().isSuperGroupChat()) && 
           message.getReplyToMessage() != null && 
           message.getReplyToMessage().getForwardFromMessageId() != null;
    }

    private void handleGroupReply(Update update, TelegramClient telegramClient) {
        Message message = update.getMessage();
        Integer forwardFromMessageId = message.getReplyToMessage().getForwardFromMessageId();
        Messages originalMessage = messageService.findByMessageId(forwardFromMessageId);
    
        if (originalMessage != null) {
            log.info("User replied to message ID: {}", message);
            sendNotificationToUserReplied(
                originalMessage.getUser().getId(),
                forwardFromMessageId,
                message.getMessageId(),
                telegramClient
            );
        }
    }

    private void processValidMessage(Message message, TelegramClient telegramClient) {
        String content = message.getCaption() != null ? message.getCaption() : message.getText();
    
        messageUtils.process(content).ifPresentOrElse(
            messages -> {
                MenfessData menfessData = createMenfessData(message);
                sendConfirmationMessage(message, menfessData, telegramClient);
            }, 
            () -> sendInvalidMessageError(message.getChatId(), telegramClient)
        );
    }

    private void sendInvalidMessageError(long chatId, TelegramClient telegramClient) {
        String errorMessage = """
								❌ *Pesan Tidak Valid*
								
								Pesan harus mengandung:
								• Minimal 3 kata
								• Setidaknya satu hashtag (#)""";
    
        sendMessage(chatId, errorMessage, telegramClient);
    }

    private boolean isValidMessage(String messageText) {
        if (!messageText.startsWith("/")) {
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
                .orElse(photos.getFirst())
                .getFileId();
    }

    private void sendConfirmationMessage(Message message,
                                         MenfessData menfessData, 
                                         TelegramClient telegramClient) {
        String uuid = menfessDataService.saveDataMenfess(menfessData);
        sendMessageWithMarkup(
                message.getChatId(),
                CONFIRMATION_MESSAGE,
                buttonConfirmation.confirmSendMenfess(uuid),
                telegramClient
        );
    }

    public void sendNotificationToUserReplied(long chatId, 
                                              Integer messageId, 
                                              Integer commentId,
                                              TelegramClient telegramClient) {
        try {
            String[] greetings = {
                    "Psst! 👀",
                    "Wah! 🌟",
                    "Halo! 💫",
                    "Coba tebak? 🎭",
                    "Kabar menarik! 🔥"
            };

            String[] reactions = {
                    "Seseorang menemukan pengakuanmu cukup menarik untuk dibalas!",
                    "Kiriman anonimmu menarik perhatian seseorang!",
                    "Pesan rahasiamu baru saja mendapat tanggapan!",
                    "Pikiranmu telah memicu percakapan!",
                    "Seseorang baru saja terhubung dengan pesanmu!"
            };

            String[] callToActions = {
                    "Penasaran apa yang mereka katakan? Cek sekarang!",
                    "Jangan menunggu lama - lihat tanggapan mereka sekarang!",
                    "Kami juga penasaran - coba lihat!",
                    "Apa yang mereka pikirkan? Cari tahu sekarang!",
                    "Percakapan berlanjut... ketuk untuk melihat!"
            };

            String greeting = greetings[(int)(Math.random() * greetings.length)];
            String reaction = reactions[(int)(Math.random() * reactions.length)];
            String callToAction = callToActions[(int)(Math.random() * callToActions.length)];

            String notificationText = String.format(
                    "*%s*\n\n%s\n\n🔗 *Message Link:* https://t.me/%s/%s?comment=%s\n\n%s",
                    greeting, reaction, channelUsername, messageId, commentId, callToAction);

            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(notificationText)
                    .parseMode("Markdown")
                    .build();

            telegramClient.execute(message);
        } catch (Exception e) {
            log.error("Failed to send reply notification", e);
            throw new RuntimeException(e);
        }
    }
}