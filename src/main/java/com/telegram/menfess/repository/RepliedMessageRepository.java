package com.telegram.menfess.repository;

import com.telegram.menfess.entity.RepliedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RepliedMessageRepository extends JpaRepository<RepliedMessage, Long> {
    
    @Query("SELECT rm.repliedMessageId, COUNT(rm) as replyCount FROM RepliedMessage rm " +
           "WHERE DATE(rm.repliedAt) = :date " +
           "GROUP BY rm.repliedMessageId " +
           "ORDER BY replyCount DESC")
    List<Object[]> findMostRepliedMessagesByDate(@Param("date") LocalDate date);
}