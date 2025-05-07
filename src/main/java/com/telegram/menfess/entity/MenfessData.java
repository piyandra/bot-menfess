package com.telegram.menfess.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "menfess_data")
public class MenfessData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private Long chatId;
    private int messageId;
    private String caption;

    @Enumerated(EnumType.STRING)
    private FileType type;
    private String fileId;


}
