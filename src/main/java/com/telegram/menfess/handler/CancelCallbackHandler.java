package com.telegram.menfess.handler;

import com.telegram.menfess.service.MenfessDataService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.CompletableFuture;

@Component
public class CancelCallbackHandler implements CallbackProcessor {

    private final MenfessDataService menfessDataService;

    public CancelCallbackHandler(MenfessDataService menfessDataService) {
        this.menfessDataService = menfessDataService;
    }

    @Override
    public String callbackPrefix() {
        return "cancel";
    }

    @Override
    @Async
    public CompletableFuture<Void> process(Update update, TelegramClient telegramClient) {
        return CompletableFuture.runAsync(() -> {
            String[] data = update.getCallbackQuery().getData().split("_");
            String message = data[1];
            menfessDataService.deleteDataById(message);
            try {
                DeleteMessage deleteMessage = DeleteMessage.builder()
                        .messageId(update.getCallbackQuery().getMessage().getMessageId())
                        .chatId(update.getCallbackQuery().getMessage().getChatId())
                        .build();
                telegramClient.execute(deleteMessage);
            } catch (Exception e) {
                log.error("Error deleting message: {}", e.getMessage());
            }
        });
    }
}
