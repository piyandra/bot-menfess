package com.telegram.menfess.repository;

import com.telegram.menfess.entity.RepliedMessage;
import com.telegram.menfess.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepliedMessageRepository extends JpaRepository<RepliedMessage, String> {
    long countDistinctByUser(User users);
}
