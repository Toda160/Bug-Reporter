package com.utcn.demo.entity;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name = "votes")
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "bug_id")
    private Bug bug;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment comment;

    private String voteType; // "upvote" or "downvote"

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date createdAt;

    // Constructori, Getteri și Setteri
    public Vote() {}

    public Vote(User user, Bug bug, Comment comment, String voteType) {
        this.user = user;
        this.bug = bug;
        this.comment = comment;
        this.voteType = voteType;
    }

    // Getters and Setters
}
