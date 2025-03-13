package com.utcn.demo.controller;

import com.utcn.demo.entity.Bug;
import com.utcn.demo.entity.Comment;
import com.utcn.demo.service.BugService;
import com.utcn.demo.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/bug/{bugId}")
    public List<Comment> getCommentsByBug(@PathVariable Long bugId) {
        return commentService.getCommentsByBug(bugId);
    }

    @PostMapping
    public ResponseEntity<Comment> addComment(@RequestBody Map<String, Object> payload) {
        Long bugId = Long.valueOf(payload.get("bugId").toString());
        Long authorId = Long.valueOf(payload.get("authorId").toString());
        String text = (String) payload.get("text");
        String image = (String) payload.get("image");

        Comment createdComment = commentService.addComment(bugId, authorId, text, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
