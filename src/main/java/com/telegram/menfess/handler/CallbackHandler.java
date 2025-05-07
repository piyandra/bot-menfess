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
public class CallbackHandler {


    private final Map<String, CallbackProcessor> callbackProcessorMap;

    public CallbackHandler(List<CallbackProcessor> callbackProcessor) {
        this.callbackProcessorMap = callbackProcessor.stream().collect(Collectors.toMap(CallbackProcessor::callbackPrefix, callbackProcessor1 -> callbackProcessor1));
    }

    @Async
    public CompletableFuture<Void> handleCallbackHandler(Update update, TelegramClient telegramClient) {
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            System.out.println(update.getCallbackQuery().getData());
            String callbackPrefix = callbackData.split("_")[0];
            CallbackProcessor callbackProcessor = callbackProcessorMap.getOrDefault(callbackPrefix, callbackProcessorMap.get("none"));
            return callbackProcessor.process(update, telegramClient);
        }
        return CompletableFuture.completedFuture(null);
    }
}
