package com.utcn.demo.controller;

import com.utcn.demo.entity.Comment;
import com.utcn.demo.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/list/bug/{bugId}")
    public ResponseEntity<List<Comment>> listCommentsByBug(@PathVariable Long bugId) {
        List<Comment> comments = commentService.getCommentsByBug(bugId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/add")
    public ResponseEntity<Comment> createNewComment(@RequestBody Map<String, Object> payload) {
        Long bugId = Long.valueOf(payload.get("bugId").toString());
        Long authorId = Long.valueOf(payload.get("authorId").toString());
        String text = (String) payload.get("text");
        String image = (String) payload.get("image");

        Comment createdComment = commentService.addComment(bugId, authorId, text, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<Void> removeCommentById(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}