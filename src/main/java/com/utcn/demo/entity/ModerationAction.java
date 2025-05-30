package com.utcn.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "moderation_actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModerationAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "moderator_id", nullable = false)
    private Long moderatorId;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "target_bug_id")
    private Long targetBugId;

    @Column(name = "target_comment_id")
    private Long targetCommentId;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "details")
    private String details;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
}
