package com.utcn.demo.controller;

import com.utcn.demo.entity.Comment;
import com.utcn.demo.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<List<Comment>> getCommentsByBugId(@PathVariable Long bugId) {
        List<Comment> comments = commentService.getCommentsByBugId(bugId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/create")
    public ResponseEntity<Comment> createComment(@RequestBody Map<String, Object> payload) {
        Long authorId = Long.valueOf(payload.get("authorId").toString());
        Long bugId = Long.valueOf(payload.get("bugId").toString());
        String text = (String) payload.get("text");
        String image = (String) payload.get("image");

        Comment createdComment = commentService.createComment(authorId, bugId, text, image);
        return ResponseEntity.ok(createdComment);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        Long authorId = Long.valueOf(payload.get("authorId").toString());
        Comment updatedComment = commentService.updateComment(id, authorId, payload);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @RequestParam Long authorId) {
        commentService.deleteComment(id, authorId);
        return ResponseEntity.noContent().build();
    }
}