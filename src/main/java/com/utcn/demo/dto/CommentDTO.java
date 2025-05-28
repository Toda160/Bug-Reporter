package com.utcn.demo.dto;

import java.time.LocalDateTime;

public class CommentDTO {
    public Long id;
    public String text;
    public String image;
    public LocalDateTime createdAt;
    public UserDTO author;
    public int voteCount;
} 