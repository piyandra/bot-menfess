package com.telegram.menfess.service;

import com.telegram.menfess.entity.Messages;
import com.telegram.menfess.entity.User;
import com.telegram.menfess.repository.MessagesRepository;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final MessagesRepository messagesRepository;

    public MessageService(MessagesRepository messagesRepository) {
        this.messagesRepository = messagesRepository;
    }

    public Messages saveMessage(Messages messages) {
        return messagesRepository.save(messages);
    }
    public Messages findMessageById(String id) {
        return messagesRepository.findById(id).orElse(null);
    }
    public void deleteMessageById(String id) {
        messagesRepository.findById(id).ifPresent(messages -> {
            messages.setDeleted(true);
            messagesRepository.save(messages);
        });
    }
    public long countMessageByUserId(User user) {
        return messagesRepository.countDistinctById(user);
    }
}
