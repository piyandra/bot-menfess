package com.telegram.menfess.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.CompletableFuture;

@Component
public interface CommandProcessor {

	Logger log = LoggerFactory.getLogger(CommandProcessor.class);

	String commands();

	CompletableFuture<Void> process(Update update, TelegramClient telegramClient);

	default void sendMessage(long chatId, String message, TelegramClient telegramClient) {
		try {
			telegramClient.execute(SendMessage.builder()
					.chatId(chatId)
					.text(message)
					.parseMode("HTML")
					.build());
		} catch (Exception e) {
			log.warn("Cannot Send message to {}", chatId, e);
		}
	}

	default void sendMessageWithMarkup(long chatId, String message, InlineKeyboardMarkup markup, TelegramClient telegramClient) {
		try {
			telegramClient.execute(SendMessage.builder()
					.chatId(chatId)
					.text(message)
					.parseMode("HTML")
					.replyMarkup(markup)
					.build());
		} catch (Exception e) {
			log.warn("Cannot Send message to {}", chatId, e);
		}
	}

	default void editMessageSuccessReplyMenfess(long chatId, int messageId, String text, TelegramClient telegramClient) {
		try {
			telegramClient.execute(EditMessageText.builder()
					.chatId(chatId)
					.messageId(messageId)
					.text(text)
					.build());
		} catch (TelegramApiException e) {
			log.error("Error editing message for reply Menfess: {}", e.getMessage());
		}
	}
}
