package com.telegram.menfess.handler;

import com.telegram.menfess.service.UserService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.CompletableFuture;

@Component
public class ReplyMessageInGroupCallbackHandler implements CallbackProcessor {
	private final UserService userService;

	public ReplyMessageInGroupCallbackHandler(UserService userService) {
		this.userService = userService;
	}

	@Override
	public String callbackPrefix() {
		return "reply";
	}

	@Override
	@Async
	public CompletableFuture<Void> process(Update update, TelegramClient telegramClient) {
		return CompletableFuture.runAsync(() -> {
			String[] data = update.getCallbackQuery().getData().split("_");
			boolean userState = userService.isUserState(update.getCallbackQuery().getMessage().getChatId());
			if (!userState) {
				long chatId = update.getCallbackQuery().getMessage().getChatId();
				int messageId = update.getCallbackQuery().getMessage().getMessageId();
				log.info("Processing reply callback: {}", update.getCallbackQuery().getData());
				log.info("From Chat Id: {}", chatId);
				log.info("Message Id: {}", messageId);
				log.info("Reply Group {}", data[1]);
				log.info("Reply Message Id {}", data[2]);
				log.info("Reply User Id Message {}", data[3]);
				userService.setReplyState(chatId, true, Integer.parseInt(data[2]), Integer.parseInt(data[2]), Long.parseLong(data[1]));
				editMessageReplyToUserMenfess(chatId, messageId, "Silahkan Kirim Pesan Balasan Untuk Menfess Ini", null, telegramClient);
			}
		});
	}
}
