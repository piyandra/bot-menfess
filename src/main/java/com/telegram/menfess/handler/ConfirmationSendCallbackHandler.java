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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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

    @Value("${channel.username}")
    private String channelUsername;

    @Override
    public String callbackPrefix() {
        return CALLBACK_PREFIX;
    }

    @Override
    @Async
    public CompletableFuture<Void> process(Update update, TelegramClient telegramClient) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (update.getCallbackQuery().getData() != null) {
                    String messageId = extractMessageId(update);
                    MenfessData menfessData = retrieveMenfessData(messageId);
                    Message message = processMessage(menfessData, telegramClient);
                    String successMessage = String.format(
                           """
                           🚀 <b>Pesan Berhasil Terkirim!</b>
                           
                           ✅ Pesan menfess kamu sudah terkirim ke channel
                           
                           🔗 <b>Lihat Pesan:</b> https://t.me/%s/%s""",
                           channelUsername, message.getMessageId().toString()
                   );

                    editMessageSuccessSendMenfess(
                            update.getCallbackQuery().getMessage().getChatId(),
                            update.getCallbackQuery().getMessage().getMessageId(),
                            successMessage,
                            null,
                            telegramClient
                    );
                } else {
                    log.warn("Update is not a user message");
                }

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

    private Message processMessage(MenfessData data, TelegramClient telegramClient) {
        String formattedMessage = formatMessage(data.getCaption());

        MessageSender messageSender = getMessageSender(data.getType());
        Message sentMessage = messageSender.sendMessage(data, formattedMessage, telegramClient);

        if (sentMessage != null) {
            saveMessageAndCleanup(data, sentMessage);

        }
        return sentMessage;
    }

    private MessageSender getMessageSender(FileType type) {
        return switch (type) {
			case AUDIO -> ((data, message, client) -> sendAudio(Long.parseLong(channelId), data.getFileId(), message, client));
			case TEXT -> (data, message, client) -> sendMessage(Long.parseLong(channelId), message, client);
            case PHOTOS -> (data, message, client) -> sendMessageWithPhoto(Long.parseLong(channelId), data.getFileId(), message, client);
            case VIDEOS -> (data, message, client) -> sendVideo(Long.parseLong(channelId), data.getFileId(), message, client);
        };
    }

    private String formatMessage(String caption) {
        return MENFESS_HEADER + (caption != null ? caption : "");
    }


    protected void saveMessageAndCleanup(MenfessData data, Message sentMessage) {
        int maxRetries = 3;
        for (int retries = 0; retries < maxRetries; retries++) {
            try {
                User user = userService.saveUser(User.builder()
                        .id(data.getChatId())
                        .username(null)
                        .build());

                Messages messages = Messages.builder()
                        .messageId(sentMessage.getMessageId())
                        .deleted(false)
                        .text(sentMessage.getText() != null ? sentMessage.getText() : sentMessage.getCaption())
                        .user(user)
                        .type(data.getType())
                        .build();

                messageService.saveMessage(messages);
                menfessDataService.deleteDataById(data.getId());
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                if (retries >= maxRetries - 1) {
                    log.error("Error Locking");
                    throw e;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    log.error("Thread interrupted: ", interruptedException);
                    throw new RuntimeException(interruptedException);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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