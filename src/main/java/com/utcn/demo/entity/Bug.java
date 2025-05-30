package com.utcn.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.CreationTimestamp;
import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "bugs")
public class Bug {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnoreProperties("bugs")
    //@JoinColumn(name = "author_id", nullable = false)
    private User author;

    private String title;
    private String description;
    private String image;
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date createdAt;

   // @OneToMany(mappedBy = "bug", cascade = CascadeType.ALL, orphanRemoval = true)
    @OneToMany(mappedBy="bug")
    @JsonManagedReference
    private Set<BugTag> bugTags = new HashSet<>();

    // New field to track the accepted comment
    @OneToOne
    @JoinColumn(name = "accepted_comment_id")
    private Comment acceptedComment;

    // Constructors
    public Bug() {}

    public Bug(User author, String title, String description, String image, String status) {
        this.author = author;
        this.title = title;
        this.description = description;
        this.image = image;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Set<BugTag> getBugTags() { return bugTags; }
    public void setBugTags(Set<BugTag> bugTags) { this.bugTags = bugTags; }

    // Getter and Setter for acceptedComment
    public Comment getAcceptedComment() { return acceptedComment; }
    public void setAcceptedComment(Comment acceptedComment) { this.acceptedComment = acceptedComment; }
}