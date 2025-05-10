package com.telegram.menfess.service;

import com.telegram.menfess.entity.User;
import com.telegram.menfess.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.findUsersById(user.getId()).orElseGet(() -> userRepository.save(user));
    }
}
