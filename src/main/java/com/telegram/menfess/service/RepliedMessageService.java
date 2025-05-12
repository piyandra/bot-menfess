package com.telegram.menfess.service;

import com.telegram.menfess.entity.Messages;
import com.telegram.menfess.entity.RepliedMessage;
import com.telegram.menfess.repository.RepliedMessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RepliedMessageService {

    private final RepliedMessageRepository repliedMessageRepository;

    public RepliedMessageService(RepliedMessageRepository repliedMessageRepository) {
        this.repliedMessageRepository = repliedMessageRepository;
    }

    public void saveRepliedMessage(RepliedMessage repliedMessage) {
        repliedMessageRepository.save(repliedMessage);
    }

    
    /**
     * Finds the most replied messages with their reply counts for a specific date
     * @param date The date to find replies for
     * @return Map of Messages to their reply counts, sorted by count (most replied first)
     */
    public Map<Messages, Long> findMostRepliedMessagesWithCountByDate(LocalDate date) {
        List<Object[]> results = repliedMessageRepository.findMostRepliedMessagesByDate(date);
        return results.stream()
                .collect(Collectors.toMap(
                    result -> (Messages) result[0],
                    result -> (Long) result[1],
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));
    }
}