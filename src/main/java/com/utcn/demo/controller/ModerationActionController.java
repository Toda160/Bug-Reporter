package com.utcn.demo.controller;

import com.utcn.demo.entity.ModerationAction;
import com.utcn.demo.service.ModerationActionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ModerationActionController {

    private final ModerationActionService moderationActionService;

    public ModerationActionController(ModerationActionService moderationActionService) {
        this.moderationActionService = moderationActionService;
    }

    //
    // ─── Generic CRUD OVER ModerationAction records ───────────────────────────────
    //

    /** List all recorded moderation actions. */
    @GetMapping("/moderation-actions")
    public ResponseEntity<List<ModerationAction>> listAllActions() {
        return ResponseEntity.ok(moderationActionService.getAllActions());
    }

    /** Create (record) a brand-new moderation action entry. */
    @PostMapping("/moderation-actions")
    public ResponseEntity<ModerationAction> createAction(@RequestBody ModerationAction action) {
        ModerationAction saved = moderationActionService.createAction(action);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /** Delete a single ModerationAction by its ID. */
    @DeleteMapping("/moderation-actions/{id}")
    public ResponseEntity<Void> deleteAction(@PathVariable Integer id) {
        moderationActionService.deleteAction(id);
        return ResponseEntity.noContent().build();
    }

    //
    // ─── Bonus Feature 2: moderator-only operations ───────────────────────────────
    //   (remove/edit bug, remove/edit comment, ban/unban user)
    //

    /** 1) Remove a bug from the site. */
    @DeleteMapping("/moderation/bugs/{bugId}/mods/{moderatorId}")
    public ResponseEntity<Void> removeBug(@PathVariable Long bugId,
                                          @PathVariable Long moderatorId) {
        moderationActionService.removeBug(moderatorId, bugId);
        return ResponseEntity.noContent().build();
    }

    /** 2) Edit a bug’s title/description. */
    @PatchMapping("/moderation/bugs/{bugId}/mods/{moderatorId}")
    public ResponseEntity<ModerationAction> editBug(@PathVariable Long bugId,
                                                    @PathVariable Long moderatorId,
                                                    @RequestBody Map<String, Object> payload) {
        String newTitle = (String) payload.get("title");
        String newDesc  = (String) payload.get("description");
        ModerationAction act =
                moderationActionService.editBug(moderatorId, bugId, newTitle, newDesc);
        return ResponseEntity.ok(act);
    }

    /** 3) Remove a comment. */
    @DeleteMapping("/moderation/comments/{commentId}/mods/{moderatorId}")
    public ResponseEntity<Void> removeComment(@PathVariable Long commentId,
                                              @PathVariable Long moderatorId) {
        moderationActionService.removeComment(moderatorId, commentId);
        return ResponseEntity.noContent().build();
    }

    /** 4) Edit a comment’s text. */
    @PatchMapping("/moderation/comments/{commentId}/mods/{moderatorId}")
    public ResponseEntity<ModerationAction> editComment(@PathVariable Long commentId,
                                                        @PathVariable Long moderatorId,
                                                        @RequestBody Map<String, Object> payload) {
        String newText = (String) payload.get("text");
        ModerationAction act =
                moderationActionService.editComment(moderatorId, commentId, newText);
        return ResponseEntity.ok(act);
    }

    /** 5) Ban a user indefinitely. */
    @PostMapping("/moderation/users/{userId}/ban/mods/{moderatorId}")
    public ResponseEntity<Void> banUser(@PathVariable Long userId,
                                        @PathVariable Long moderatorId,
                                        @RequestBody String reason) {
        moderationActionService.banUser(moderatorId, userId, reason);
        return ResponseEntity.ok().build();
    }

    /** 6) Lift a ban on a user. */
    @PostMapping("/moderation/users/{userId}/unban/mods/{moderatorId}")
    public ResponseEntity<Void> unbanUser(@PathVariable Long userId,
                                          @PathVariable Long moderatorId) {
        moderationActionService.unbanUser(moderatorId, userId);
        return ResponseEntity.ok().build();
    }
}
