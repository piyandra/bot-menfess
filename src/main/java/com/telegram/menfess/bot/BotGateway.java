package com.telegram.menfess.bot;

import com.telegram.menfess.handler.CallbackHandler;
import com.telegram.menfess.handler.CommandHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.CompletableFuture;

@Component
public class BotGateway {

    private final TelegramClient telegramClient;

    private final String botToken;
    private final CommandHandler commandHandler;
    private final CallbackHandler callbackHandler;

    public BotGateway(@Value("${bot.token}")String botToken, CommandHandler commandHandler, CallbackHandler callbackHandler) {
        this.botToken = botToken;
        this.commandHandler = commandHandler;
        this.callbackHandler = callbackHandler;
        this.telegramClient = new OkHttpTelegramClient(botToken);
    }
    @Async
    public CompletableFuture<Void> process(Update update) {
        if (!update.hasCallbackQuery()) {
            commandHandler.handleCommandHandler(update, telegramClient);
        } else if (update.hasCallbackQuery()) {
            callbackHandler.handleCallbackHandler(update, telegramClient);
        }
        return CompletableFuture.completedFuture(null);
    }
}
