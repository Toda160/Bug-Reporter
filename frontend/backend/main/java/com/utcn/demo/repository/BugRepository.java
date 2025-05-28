package com.utcn.demo.repository;

import com.utcn.demo.entity.Bug;
import com.utcn.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BugRepository extends JpaRepository<Bug, Long> {
    List<Bug> findByAuthor(User author);
}
