package com.utcn.demo.repository;

import com.utcn.demo.entity.Vote;
import com.utcn.demo.entity.User; // Import User
import com.utcn.demo.entity.Bug; // Import Bug
import com.utcn.demo.entity.Comment; // Import Comment
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserAndComment(User user, Comment comment);
    Optional<Vote> findByUserAndBug(User user, Bug bug); // Assuming voting directly on bugs is possible

    List<Vote> findByBug(Bug bug);
    List<Vote> findByComment(Comment comment);

    // Add these methods for deletion:
    @Transactional // Deletion operations should be transactional
    void deleteByCommentIdIn(List<Long> commentIds);

    @Transactional // Deletion operations should be transactional
    void deleteByBugId(Long bugId);

     @Transactional
     void deleteByCommentId(Long commentId);
    // Need methods to retrieve votes by comment IDs and bug ID for score reversal before deletion
    List<Vote> findByCommentIdIn(List<Long> commentIds);
    List<Vote> findByBugId(Long bugId);
}