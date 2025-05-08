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

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final BugRepository bugRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, BugRepository bugRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.bugRepository = bugRepository;
        this.userRepository = userRepository;
    }

    public List<Comment> getCommentsByBugId(Long bugId) {
        return commentRepository.findByBugIdOrderByCreatedAtDesc(bugId);
    }

    public Comment createComment(Long authorId, Long bugId, String text, String image) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found"));

        Comment comment = new Comment(author, bug, text, image);
        return commentRepository.save(comment);
    }

    public Comment updateComment(Long commentId, Long authorId, Map<String, Object> payload) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new RuntimeException("Not authorized to edit this comment");
        }

        if (payload.containsKey("text")) {
            comment.setText((String) payload.get("text"));
        }
        if (payload.containsKey("image")) {
            comment.setImage((String) payload.get("image"));
        }

        return commentRepository.save(comment);
    }

    public void deleteComment(Long commentId, Long authorId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new RuntimeException("Not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }
}
