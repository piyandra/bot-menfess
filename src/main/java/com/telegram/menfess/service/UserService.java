package com.telegram.menfess.service;

import com.telegram.menfess.entity.User;
import com.telegram.menfess.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(User user) {
        return userRepository.findUsersById(user.getId()).orElseGet(() -> userRepository.save(user));
    }
    public long countUser() {
        return userRepository.count();
    }
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}
