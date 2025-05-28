package com.utcn.demo.service;

import com.utcn.demo.entity.Comment;
import com.utcn.demo.entity.Bug;
import com.utcn.demo.entity.User;
import com.utcn.demo.repository.CommentRepository;
import com.utcn.demo.repository.BugRepository;
import com.utcn.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final BugRepository bugRepository;
    private final UserRepository userRepository;
    private final VoteService voteService;

    @Autowired
    public CommentService(CommentRepository commentRepository, BugRepository bugRepository,
                          UserRepository userRepository, VoteService voteService) {
        this.commentRepository = commentRepository;
        this.bugRepository = bugRepository;
        this.userRepository = userRepository;
        this.voteService = voteService;
    }

    public List<Comment> getCommentsByBugId(Long bugId) {
        List<Comment> comments = commentRepository.findByBugIdOrderByCreatedAtDesc(bugId);
        return comments.stream()
                .peek(comment -> comment.setVoteCount(voteService.getVoteCountForComment(comment.getId())))
                .sorted((c1, c2) -> Integer.compare(c2.getVoteCount(), c1.getVoteCount())) // Descending order
                .collect(Collectors.toList());
    }

    public long getCommentCount() {
        return commentRepository.count();
    }

    public Comment createComment(Long authorId, Long bugId, String text, String image) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found"));

        // Prevent adding comments to solved bugs
        if ("Solved".equals(bug.getStatus())) {
            throw new RuntimeException("Cannot add comments to a solved bug");
        }

        // If this is the first comment and bug is "Received", set status to "In progress"
        if ("Received".equals(bug.getStatus())) {
            // Check if there are any comments for this bug
            long commentCount = commentRepository.countByBugId(bugId);
            if (commentCount == 0) {
                bug.setStatus("In progress");
                bugRepository.save(bug);
            }
        }

        Comment comment = new Comment(author, bug, text, image);
        Comment savedComment = commentRepository.save(comment);
        savedComment.setVoteCount(voteService.getVoteCountForComment(savedComment.getId()));
        return savedComment;
    }

    public Comment updateComment(Long id, Long authorId, Map<String, Object> payload) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new RuntimeException("Only the author can update their comment");
        }

        if (payload.containsKey("text")) {
            comment.setText((String) payload.get("text"));
        }
        if (payload.containsKey("image")) {
            comment.setImage((String) payload.get("image"));
        }

        Comment updatedComment = commentRepository.save(comment);
        updatedComment.setVoteCount(voteService.getVoteCountForComment(updatedComment.getId()));
        return updatedComment;
    }

    public void deleteComment(Long id, Long authorId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new RuntimeException("Only the author can delete their comment");
        }

        commentRepository.delete(comment);
    }
}