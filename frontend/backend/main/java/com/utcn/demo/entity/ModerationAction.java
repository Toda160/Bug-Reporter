package com.utcn.demo.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "moderation_actions")
public class ModerationAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "moderator_id", nullable = false)
    private User moderator;

    @Column(nullable = false)
    private String actionType;

    @ManyToOne
    @JoinColumn(name = "target_user_id")
    private User targetUser;

    @ManyToOne
    @JoinColumn(name = "target_bug_id")
    private Bug targetBug;

    @ManyToOne
    @JoinColumn(name = "target_comment_id")
    private Comment targetComment;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
