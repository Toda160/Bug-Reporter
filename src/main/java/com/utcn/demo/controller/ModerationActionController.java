package com.utcn.demo.controller;

import com.utcn.demo.entity.ModerationAction;
import com.utcn.demo.service.ModerationActionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/moderation-actions")
public class ModerationActionController {

    private final ModerationActionService moderationActionService;

    public ModerationActionController(ModerationActionService moderationActionService) {
        this.moderationActionService = moderationActionService;
    }

    @GetMapping
    public List<ModerationAction> getAllActions() {
        return moderationActionService.getAllActions();
    }

    @PostMapping
    public ModerationAction createAction(@RequestBody ModerationAction action) {
        return moderationActionService.createAction(action);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAction(@PathVariable Integer id) {
        moderationActionService.deleteAction(id);
        return ResponseEntity.noContent().build();
    }
}
