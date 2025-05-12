package com.telegram.menfess.handler;

import com.telegram.menfess.entity.Messages;
import com.telegram.menfess.service.RepliedMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class DailyTrending {

	private static final Logger log = LoggerFactory.getLogger(DailyTrending.class);
	private final RepliedMessageService repliedMessageService;

	@Value("${channel.id}")
	private String channelId;

	@Value("${channel.username}")
	private String channelName;

	private final TelegramClient telegramClient;

	public DailyTrending(RepliedMessageService repliedMessageService, 
                        @Value("${bot.token}") String botToken) {
		this.repliedMessageService = repliedMessageService;
		this.telegramClient = new OkHttpTelegramClient(botToken);
	}

	@Scheduled(cron = "0 58 23 * * *")
	@Async
	public CompletableFuture<Void> dailyTrending() {
		return CompletableFuture.runAsync(() -> {
			log.info("Starting Daily Trending task");
			LocalDate today = LocalDate.now();
			Map<Messages, Long> mostReplied = repliedMessageService.findMostRepliedMessagesWithCountByDate(today);
			StringBuilder report = new StringBuilder(String.format("""
        \uD83C\uDFC6 Trending Hari Ini %s:
        
        """, today.format(DateTimeFormatter
					.ofPattern("dd MMMM yyyy",
							Locale.forLanguageTag("id-ID")))));

			int rank = 1;

			for (Map.Entry<Messages, Long> entry : mostReplied.entrySet()) {
				Messages message = entry.getKey();
				Long count = entry.getValue();
				log.info("Rank {}: Message ID {} with {} replies", rank, message.getMessageId(), count);
				if (rank <= 10) {
					String medal = rank == 1 ? "🥇" : (rank == 2 ? "🥈" : (rank == 3 ? "🥉" : "🏅"));

					report.append(String.format("""
        %s <b>Rank %d</b>
        <a href="https://t.me/%s/%s">Buka Pesan</a> | %d komentar
        
        """,
							medal,
							rank,
							channelName,
							message.getMessageId(),
							count
					));
					rank++;
				} else {
					break;
				}
			}
			sendTrendingMessage(report.toString());
		});
	}

	public void sendTrendingMessage(String text) {
		try {
			telegramClient.execute(SendMessage.builder()
							.chatId(Long.parseLong(channelId))
							.text(text)
							.disableWebPagePreview(true)
							.parseMode("HTML")
					.build());
		} catch (TelegramApiException e) {
			log.error("Error sending trending message: {}", e.getMessage());
		}
	}
}