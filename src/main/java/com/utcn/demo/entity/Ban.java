package com.utcn.demo.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bans")
public class Ban {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "moderator_id", nullable = false)
    private User moderator;


    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private LocalDateTime bannedAt;
}
