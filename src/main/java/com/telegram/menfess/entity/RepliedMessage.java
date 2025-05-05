package com.telegram.menfess.entity;

import jakarta.persistence.*;
import lombok.*;



@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "replied_messages")
public class RepliedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String repliedId;

    @ManyToOne
    @JoinColumn(name = "message_id")
    private Messages repliedMessageId;

    private Long repliedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;




}
