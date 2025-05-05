package com.telegram.menfess.repository;

import com.telegram.menfess.entity.Messages;
import com.telegram.menfess.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MessagesRepository extends JpaRepository<Messages, String> {

    Long countDistinctById(User id);

}
