package com.telegram.menfess.handler;

import com.telegram.menfess.entity.*;
import com.telegram.menfess.service.MenfessDataService;
import com.telegram.menfess.service.MessageService;
import com.telegram.menfess.service.RepliedMessageService;
import com.telegram.menfess.service.UserService;
import com.telegram.menfess.utils.ButtonConfirmation;
import com.telegram.menfess.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.messageorigin.MessageOriginChannel;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDateTime;
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
    private final RepliedMessageService repliedMessageService;
    private final UserService userService;

    @Value("${channel.username}")
    private String channelUsername;

    @Value("${owner.id}")
    private String ownerId;

    @Value("${channel.id}")
    private String channelId;

    @Value("${bot.token}")
    private String botToken;

    @Override
    public String commands() {
        return "/";
    }

    @Override
    @Async
    public CompletableFuture<Void> process(Update update, TelegramClient telegramClient) {
        return CompletableFuture.runAsync(() -> {
            try {
                Message message = update.getMessage();
                if (message == null) {
                    log.warn("Received update without message");
                    return;
                }

                String messageText = message.hasText() ? message.getText() : "";

                if (handleForwardedChannelMessage(message, messageText, telegramClient)) {
                    return;
                }

                if (isGroupMessageWithReply(message)) {
                    handleGroupReply(update, telegramClient);
                    return;
                }

                if (!isValidMessage(messageText) || !message.isUserMessage()) {
                    return;
                }

                processValidMessage(message, telegramClient);
            } catch (Exception e) {
                log.error("Error processing message: ", e);
            }
        });
    }

    private boolean handleForwardedChannelMessage(Message message, String messageText, TelegramClient telegramClient) {
        if (message.getForwardOrigin() instanceof MessageOriginChannel originChannel) {
            if (originChannel.getChat().getId().equals(Long.parseLong(channelId))) {
                if (messageText.contains("#")) {
                    sendReportInstructionsMessage(message, telegramClient);
                }
                return true;
            }
            return true;
        }
        return false;
    }

    private void sendReportInstructionsMessage(Message message, TelegramClient telegramClient) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(message.getChatId())
                .text("""
                    🚩 <b>Lihat konten yang melanggar?</b>
                    
                    Gunakan perintah <code>!lapor</code> untuk melaporkan menfess ini ke admin kami.
                    
                    👮‍♂️ Bantu kami menjaga komunitas tetap aman dan nyaman!""")
                .parseMode("HTML")
                .replyToMessageId(message.getMessageId())
                .build();
        
        executeWithErrorHandling(telegramClient, sendMessage, "Failed to send report instructions");
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

        if (originalMessage == null && !message.getFrom().getId().equals(Long.parseLong(botToken.split(":")[0]))) {
            log.warn("Could not find original message for reply: {}", forwardFromMessageId);
            return;
        }

        // Handle report command
        if (message.hasText() && message.getText().contains("!lapor")) {
            handleReportCommand(message, forwardFromMessageId, telegramClient);
            return;
        }
        handleRegularReply(message, originalMessage, telegramClient);
    }

    private void handleReportCommand(Message message, Integer forwardFromMessageId, TelegramClient telegramClient) {
        String reportMessage = message.getText().replace("!lapor", "") +
                String.format(" https://t.me/%s/%s", channelUsername, forwardFromMessageId);
        
        warnFromUser(Long.parseLong(ownerId), reportMessage, telegramClient);
        sendReportConfirmationMessage(message, telegramClient);
    }

    private void handleRegularReply(Message message, Messages originalMessage, TelegramClient telegramClient) {
        sendNotificationToUserReplied(
                originalMessage.getUser().getId(),
                originalMessage.getMessageId(),
                message.getMessageId(),
                buttonConfirmation.replyAnonymous(
                        message.getChatId(), 
                        message.getMessageId(), 
                        originalMessage.getMessageId()
                ),
                telegramClient
        );

        CompletableFuture.runAsync(() -> {
            try {
                repliedMessageService.saveRepliedMessage(RepliedMessage.builder()
                        .repliedAt(LocalDateTime.now())
                        .user(userService.saveUser(User.builder()
                                        .username(null)
                                        .id(originalMessage.getUser().getId())
                                .build()))
                        .repliedMessageId(messageService.findByMessageId(originalMessage.getMessageId()))
                        .build());
            } catch (Exception e) {
                log.error("Failed to save replied message: ", e);
            }
        });
    }

    private void sendReportConfirmationMessage(Message message, TelegramClient telegramClient) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(message.getChatId())
                .text("""
                    🚨 <b>Laporan Terkirim!</b> 📢
                    
                    ✅ Pesan telah berhasil dilaporkan ke pemilik bot.
                    
                    🙏 Terima kasih atas kontribusi Anda dalam menjaga kualitas komunitas kami.""")
                .parseMode("HTML")
                .replyToMessageId(message.getMessageId())
                .build();
        
        executeWithErrorHandling(telegramClient, sendMessage, "Failed to send report confirmation");
    }

    private void processValidMessage(Message message, TelegramClient telegramClient) {
        String content = message.getCaption() != null ? message.getCaption() : message.getText();

        if (userService.isUserState(message.getChatId()) && message.isUserMessage()) {
            processReplyStateMessage(message, telegramClient);
            return;
        }

        messageUtils.process(content).ifPresentOrElse(
                messages -> {
                    MenfessData menfessData = createMenfessData(message);
                    sendConfirmationMessage(message, menfessData, telegramClient);
                },
                () -> sendInvalidMessageError(message.getChatId(), telegramClient)
        );
    }
    
    private void processReplyStateMessage(Message message, TelegramClient telegramClient) {
        User userById = userService.findUserById(message.getChatId());
        if (userById == null) {
            log.warn("User not found for chat ID: {}", message.getChatId());
            return;
        }
        
        int messageId = userById.getUserMessageId();
        int channelMessageId = userById.getChannelRepliedMessageId();

        CompletableFuture.runAsync(() -> 
            userService.setReplyState(message.getChatId(), false, 0, 0, 0L)
        );

        editMessageSuccessReplyMenfess(
                userById.getId(), 
                messageId, 
                "Balasan Sudah Terkirim",
                telegramClient
        );

        SendMessage sendMessage = SendMessage.builder()
                .chatId(userById.getGroupId())
                .replyToMessageId(channelMessageId)
                .text(String.format("<i>Sender: '%s'</i>", message.getText()))
                .parseMode("HTML")
                .build();
        
        try {
            telegramClient.execute(sendMessage);

            EditMessageText messageText = EditMessageText.builder()
                    .text("<i>Pesan Sudah Terkirim</i>")
                    .chatId(message.getChatId())
                    .messageId(message.getMessageId() - 1)
                    .parseMode("HTML")
                    .build();
            telegramClient.execute(messageText);

            telegramClient.execute(DeleteMessage.builder()
                    .chatId(message.getChatId())
                    .messageId(message.getMessageId())
                    .build());
                    
        } catch (TelegramApiException e) {
            log.error("Failed to process reply state message: ", e);
        }
    }

    private void sendInvalidMessageError(long chatId, TelegramClient telegramClient) {
        String errorMessage = """
                ❌ <b>Pesan Tidak Valid</b>
                
                Pesan harus mengandung:
                • Minimal 3 kata
                • Setidaknya satu hashtag (#)""";
                
        sendMessage(chatId, errorMessage, telegramClient);
    }

    private boolean isValidMessage(String messageText) {
        return !messageText.startsWith("/");
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
        } else if (message.hasVoice()) {
            return FileType.AUDIO;
        }
        return FileType.TEXT;
    }

    private String extractFileId(Message message, FileType fileType) {
        return switch (fileType) {
            case VIDEOS -> message.getVideo().getFileId();
            case PHOTOS -> getLargestPhotoFileId(message.getPhoto());
            case AUDIO -> message.getVoice().getFileId();
            default -> null;
        };
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
                                              InlineKeyboardMarkup markup,
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

            int randomIndex = (int)(Math.random() * greetings.length);
            String greeting = greetings[randomIndex];
            String reaction = reactions[randomIndex]; 
            String callToAction = callToActions[randomIndex];

            String notificationText = String.format(
                    "<b>%s</b>\n\n%s\n\n🔗 <b>Message Link:</b> https://t.me/%s/%s?comment=%s\n\n%s",
                    greeting, reaction, channelUsername, messageId, commentId, callToAction);

            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(notificationText)
                    .parseMode("HTML")
                    .replyMarkup(markup)
                    .build();

            telegramClient.execute(message);
        } catch (Exception e) {
            log.error("Failed to send reply notification: ", e);
        }
    }

    public void warnFromUser(long chatId, String message, TelegramClient telegramClient) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .parseMode("HTML")
                .build();
                
        executeWithErrorHandling(telegramClient, sendMessage, "Failed to send warning message");
    }
    
    // Helper method to handle common telegram API execution pattern with error handling
    private void executeWithErrorHandling(TelegramClient telegramClient, SendMessage message, String errorMsg) {
        try {
            telegramClient.execute(message);
        } catch (Exception e) {
            log.error(errorMsg + ": ", e);
        }
    }
}