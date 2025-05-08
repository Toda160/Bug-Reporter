package com.utcn.demo.dto;

import java.util.Date;
import java.util.List;

public class BugDTO {
    public Long id;
    public String title;
    public String description;
    public String image;
    public String status;
    public Date createdAt;
    public UserDTO author;
    public List<TagDTO> tags;
} 