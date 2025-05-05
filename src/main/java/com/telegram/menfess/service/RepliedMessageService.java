package com.telegram.menfess.service;

import com.telegram.menfess.entity.User;
import com.telegram.menfess.repository.RepliedMessageRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class RepliedMessageService {

    private final RepliedMessageRepository repliedMessageRepository;

    public RepliedMessageService(RepliedMessageRepository repliedMessageRepository) {
        this.repliedMessageRepository = repliedMessageRepository;
    }

    public long countRepliedMessageById(User user) {
        return repliedMessageRepository.countDistinctByUser(user);
    }
     public User findUserInRepliedMessageId(String id) {
        return Objects.requireNonNull(repliedMessageRepository.findById(id).stream()
                .filter(repliedMessage -> repliedMessage.getUser() != null)
                .findFirst()
                .orElse(null)).getUser();
     }
}
