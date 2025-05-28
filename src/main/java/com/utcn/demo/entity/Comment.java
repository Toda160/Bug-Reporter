package com.utcn.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne
    @JoinColumn(name = "bug_id", nullable = false)
    @JsonIgnore
    private Bug bug;

    @Column(nullable = false)
    private String text;

    @Column
    private String image;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Transient
    private Integer voteCount;

    // New field to mark if the comment is accepted
    @Column(nullable = false)
    private boolean isAccepted = false;

    public Comment() {
        this.createdAt = LocalDateTime.now();
    }

    public Comment(User author, Bug bug, String text, String image) {
        this.author = author;
        this.bug = bug;
        this.text = text;
        this.image = image;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public Bug getBug() { return bug; }
    public void setBug(Bug bug) { this.bug = bug; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Integer getVoteCount() { return voteCount != null ? voteCount : 0; }
    public void setVoteCount(Integer voteCount) { this.voteCount = voteCount; }

    // Getter and Setter for isAccepted
    public boolean isAccepted() { return isAccepted; }
    public void setAccepted(boolean accepted) { isAccepted = accepted; }
}