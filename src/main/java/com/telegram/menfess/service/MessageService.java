package com.telegram.menfess.service;

import com.telegram.menfess.entity.Messages;
import com.telegram.menfess.repository.MessagesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessagesRepository messagesRepository;

    @Retryable(value = ObjectOptimisticLockingFailureException.class,
						backoff = @Backoff(delay = 100))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void saveMessage(Messages messages) {
        messagesRepository.save(messages);
    }

    public Messages findByMessageId(Integer messageId) {
        return messagesRepository.findByMessageId(messageId);
    }

}