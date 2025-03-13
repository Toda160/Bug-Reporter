package com.utcn.demo.entity;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bug_tags")
public class BugTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "bug_id", nullable = false)
    private Bug bug;

    @ManyToOne
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}
