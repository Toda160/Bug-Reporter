package com.utcn.demo.service;

import com.utcn.demo.entity.Bug;
import com.utcn.demo.entity.Comment;
import com.utcn.demo.entity.User;
import com.utcn.demo.repository.BugRepository;
import com.utcn.demo.repository.CommentRepository;
import com.utcn.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public List<Comment> getCommentsByBug(Long bugId) {
        Bug bug = bugRepository.findById(bugId).orElseThrow(() -> new RuntimeException("Bug not found"));
        return commentRepository.findByBug(bug);
    }

    public Comment addComment(Long bugId, Long authorId, String text, String image) {
        Bug bug = bugRepository.findById(bugId).orElseThrow(() -> new RuntimeException("Bug not found"));
        User author = userRepository.findById(authorId).orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = new Comment(bug, author, text, image);
        return commentRepository.save(comment);
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
}
