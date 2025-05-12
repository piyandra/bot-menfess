package com.telegram.menfess.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    private Long id;

    private String username;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Messages> messages;

    @OneToMany(mappedBy = "user")
    private List<RepliedMessage> repliedMessages;

    private boolean isJoined;

    private Long joinUntil;
}