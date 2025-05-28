package com.utcn.demo.dto;

public class BugDTO {
    private Long id;
    private String title;
    private String description;
    private String image;
    private String status;
    private String createdAt;
    private AuthorDTO author;
    private int voteCount; // Added voteCount field

    // Getters and setters

    public static class AuthorDTO {
        private Long id;
        private String username;
        private Double score; // Added score field

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public Double getScore() { return score; } // Added getter for score
        public void setScore(Double score) { this.score = score; } // Added setter for score
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public AuthorDTO getAuthor() { return author; }
    public void setAuthor(AuthorDTO author) { this.author = author; }

    // Getter and setter for voteCount
    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }
}