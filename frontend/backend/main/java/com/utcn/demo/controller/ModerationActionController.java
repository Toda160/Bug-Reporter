package com.utcn.demo.controller;

import com.utcn.demo.entity.ModerationAction;
import com.utcn.demo.service.ModerationActionService;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/list")
    public ResponseEntity<List<ModerationAction>> listAllModerationActions() {
        List<ModerationAction> actions = moderationActionService.getAllActions();
        return ResponseEntity.ok(actions);
    }

    @PostMapping("/create")
    public ResponseEntity<ModerationAction> createNewModerationAction(@RequestBody ModerationAction action) {
        ModerationAction createdAction = moderationActionService.createAction(action);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAction);
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<Void> removeModerationActionById(@PathVariable Integer id) {
        moderationActionService.deleteAction(id);
        return ResponseEntity.noContent().build();
    }
}