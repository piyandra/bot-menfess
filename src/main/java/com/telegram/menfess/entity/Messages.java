package com.telegram.menfess.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Messages {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private Integer messageId;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private boolean deleted;
    private String text;
}