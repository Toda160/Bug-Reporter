package com.utcn.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

   // @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    @OneToMany(mappedBy = "tag")
    @JsonIgnore
    private Set<BugTag> bugTags = new HashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<BugTag> getBugTags() {
        return bugTags;
    }

    public void setBugTags(Set<BugTag> bugTags) {
        this.bugTags = bugTags;
    }
}
