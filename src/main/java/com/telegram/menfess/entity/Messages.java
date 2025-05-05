package com.telegram.menfess.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "messages")
public class Messages {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uuid;

    private String messageId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User id;

    @OneToMany(mappedBy = "repliedMessageId")
    private List<RepliedMessage> repliedMessages;

    private Long createdAt;

    private boolean isDeleted;
}
