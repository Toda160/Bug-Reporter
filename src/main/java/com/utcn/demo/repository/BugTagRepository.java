package com.utcn.demo.repository;

import com.utcn.demo.entity.BugTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BugTagRepository extends JpaRepository<BugTag, Integer> {
    void deleteByBugId(Long bugId);
    // You might already have findByBugId if needed elsewhere
    List<BugTag> findByBugId(Long bugId);
}
