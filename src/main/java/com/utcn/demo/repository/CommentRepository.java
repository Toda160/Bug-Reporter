package com.utcn.demo.repository;

import com.utcn.demo.entity.Bug;
import com.utcn.demo.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBug(Bug bug);
}
