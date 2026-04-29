package com.example.vkr.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class EmailMessageDto {
    private String from;
    private String subject;
    private String content;
    private LocalDateTime receivedDate;
    private List<String> attachments = new ArrayList<>();
    private String messageId;
}