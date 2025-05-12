package com.telegram.menfess.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.CompletableFuture;

@Component
public class StartCommandHandler implements CommandProcessor {
	@Override
	public String commands() {
		return "/start";
	}

	@Override
	public CompletableFuture<Void> process(Update update, TelegramClient telegramClient) {
		return CompletableFuture.runAsync(() -> {
			long chatId = update.getMessage().getChatId();
			String welcomeMessage = """
					🎉 Selamat datang di bot Menfess! 🎉
					
					Di sini, anonimmu dijamin aman, seakan-akan rahasia yang paling rahasia di dunia. Kata Durov
					Kirim pesanmu, yang penting jangan sampai ketahuan siapa kamu — jangan sampai terbakar sendiri! 🔥""";
			sendMessage(chatId, welcomeMessage, telegramClient);
		});
	}

}
