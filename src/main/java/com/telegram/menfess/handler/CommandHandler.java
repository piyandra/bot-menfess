package com.telegram.menfess.handler;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class CommandHandler {


    private final Map<String, CommandProcessor> commandProcessorMap;

    public CommandHandler(List<CommandProcessor> commandProcessor) {
        this.commandProcessorMap = commandProcessor.stream().collect(Collectors.toMap(CommandProcessor::commands, commandProcessor1 -> commandProcessor1));
    }
    @Async
    public CompletableFuture<Void> handleCommandHandler(Update update, TelegramClient telegramClient) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            CommandProcessor commandProcessor = commandProcessorMap.getOrDefault(update.getMessage().getText().split(" ")[0], commandProcessorMap.get("/"));
            commandProcessor.process(update, telegramClient);
        } else {
            commandProcessorMap.get("/").process(update, telegramClient);
        }
        return CompletableFuture.completedFuture(null);
    }
}
