package com.utcn.demo.entity;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bug_id", nullable = false)
    private Bug bug;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(columnDefinition = "TEXT")
    private String text;

    private String image;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date createdAt;

    public Comment() {}

    public Comment(Bug bug, User author, String text, String image) {
        this.bug = bug;
        this.author = author;
        this.text = text;
        this.image = image;
    }

    // Getters and Setters
}
