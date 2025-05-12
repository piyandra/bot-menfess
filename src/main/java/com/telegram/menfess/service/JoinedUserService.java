package com.telegram.menfess.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;


@Service
public class JoinedUserService {

	private static final Logger log = LoggerFactory.getLogger(JoinedUserService.class);
	private final UserService userService;

	@Value("${channel.id}")
	private String channelId;


	public JoinedUserService(UserService userService) {
		this.userService = userService;
	}

	public boolean getJoinedUsers(Long id, TelegramClient telegramClient) {
		if (userService.findAndDeleteIfExpired(id) != null) {
			log.info("User {} is already joined", id);
			return true;
		}
		GetChatMember chatMember = GetChatMember.builder()
				.chatId(channelId)
				.userId(id)
				.build();
		try {
			String status = telegramClient.execute(chatMember).getStatus();
			log.info("User {} is {} in channel", id, status);
			if (status.equals("creator") || status.equals("administrator") || status.equals("member")) {
				userService.addJoinRequest(id);
				log.info("User {} is joined", id);
				return true;
			} else {
				log.info("User {} is not joined", id);
			}
		} catch (TelegramApiException e) {
			log.info("Error Verify User");
		}
		return false;
	}
}
