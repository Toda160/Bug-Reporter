package com.utcn.demo.entity;


import org.hibernate.annotations.CreationTimestamp;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.Date;

@Entity
@Table(name = "bugs")
public class Bug {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    private String title;
    private String description;
    private String image;
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date createdAt;

    // Getters, Setters, Constructors
    public Bug() {}

    public Bug(User author, String title, String description, String image, String status) {
        this.author = author;
        this.title = title;
        this.description = description;
        this.image = image;
        this.status = status;
    }

    // Getters and Setters
}

