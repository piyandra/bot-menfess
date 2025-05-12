package com.telegram.menfess.service;

import com.telegram.menfess.entity.User;
import com.telegram.menfess.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.findUsersById(user.getId()).orElseGet(() -> userRepository.save(user));
    }

    @Transactional
    public void addJoinRequest(Long id) {
        userRepository.findUsersById(id).ifPresentOrElse(user -> {
            user.setJoined(true);
            user.setJoinUntil(System.currentTimeMillis() + 1000 * 60 * 60);
            userRepository.save(user);
        }, () -> {
            User user = new User();
            user.setId(id);
            user.setJoined(true);
            user.setJoinUntil(System.currentTimeMillis() + 1000 * 60 * 60);
            userRepository.save(user);
        });
    }


    @Transactional
    public User findAndDeleteIfExpired(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        Long currentTimeMillis = System.currentTimeMillis();

        if (optionalUser.isPresent()) {
            log.info("User found with ID: {}", id);
            User user = optionalUser.get();
            if (user.getJoinUntil() <= currentTimeMillis) {
                log.info("User with ID: {} has expired, deleting...", id);
                userRepository.deleteById(id);
                return null;
            } else {
                return user;
            }
        } else {
            return null;
        }
    }
}