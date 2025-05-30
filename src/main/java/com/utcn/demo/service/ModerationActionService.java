package com.utcn.demo.service;

import com.utcn.demo.entity.ModerationAction;
import com.utcn.demo.entity.User;
import com.utcn.demo.repository.ModerationActionRepository;
import com.utcn.demo.repository.UserRepository;
import com.utcn.demo.service.BugService;
import com.utcn.demo.service.CommentService;
import com.utcn.demo.service.EmailService;
import com.utcn.demo.service.SmsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ModerationActionService {

    private static final Logger logger = LoggerFactory.getLogger(ModerationActionService.class);

    private final ModerationActionRepository moderationActionRepository;
    private final UserRepository userRepository;
    private final BugService bugService;
    private final CommentService commentService;
    private final EmailService emailService;
    private final SmsService smsService;

    public ModerationActionService(
            ModerationActionRepository moderationActionRepository,
            UserRepository userRepository,
            BugService bugService,
            CommentService commentService,
            EmailService emailService,
            SmsService smsService
    ) {
        this.moderationActionRepository = moderationActionRepository;
        this.userRepository = userRepository;
        this.bugService = bugService;
        this.commentService = commentService;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    public List<ModerationAction> getAllActions() {
        return moderationActionRepository.findAll();
    }

    public ModerationAction createAction(ModerationAction action) {
        return moderationActionRepository.save(action);
    }

    public void deleteAction(Integer id) {
        moderationActionRepository.deleteById(id);
    }

    /** build & persist a ModerationAction record */
    private ModerationAction recordAction(Long moderatorId,
                                          Long targetUserId,
                                          String actionType,
                                          String details) {
        ModerationAction a = new ModerationAction();
        a.setModeratorId(moderatorId);
        a.setTargetUserId(targetUserId);
        a.setActionType(actionType);
        a.setDetails(details);
        a.setCreatedAt(new Date());
        return moderationActionRepository.save(a);
    }

    //bonus feature methods

    @Transactional
    public void banUser(Long moderatorId, Long userId, String reason) {
        logger.info("Attempting to ban user with ID {} by moderator with ID {}", userId, moderatorId);
        try {
            User u = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            logger.info("Found user to ban: {}", u.getUsername());

            u.setBanned(true);
            userRepository.save(u);
            logger.info("User {} banned status updated and saved.", u.getUsername());

            // notify
            try {
                emailService.send(u.getEmail(),
                        "You have been banned",
                        "Reason: " + reason);
                logger.info("Ban email sent to {}", u.getEmail());
            } catch (Exception e) {
                logger.error("Failed to send ban email to user: {}", u.getEmail(), e);
                // Decide if you want to rethrow the exception or continue
            }

            try {
                smsService.send(u.getPhone(),
                        "BANNED: " + reason);
                logger.info("Ban SMS sent to {}", u.getPhone());
            } catch (Exception e) {
                logger.error("Failed to send ban SMS to user: {}", u.getPhone(), e);
                // Decide if you want to rethrow the exception or continue
            }

            recordAction(moderatorId, userId, "BAN_USER", reason);
            logger.info("Ban action recorded for user {}", u.getUsername());

        } catch (IllegalArgumentException e) {
            logger.error("Error banning user: {}", e.getMessage());
            throw new RuntimeException("Failed to ban user: " + e.getMessage(), e); // Re-throw as RuntimeException for Spring to handle
        } catch (Exception e) {
            logger.error("An unexpected error occurred during user ban for user ID {}", userId, e);
            throw new RuntimeException("An unexpected error occurred during user ban.", e); // Catch other exceptions and re-throw
        }
    }

    @Transactional
    public void unbanUser(Long moderatorId, Long userId) {
        logger.info("Attempting to unban user with ID {} by moderator with ID {}", userId, moderatorId);
        try {
            User u = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            logger.info("Found user to unban: {}", u.getUsername());

            u.setBanned(false);
            userRepository.save(u);
            logger.info("User {} banned status updated and saved.", u.getUsername());

            // notify
            try {
                emailService.send(u.getEmail(),
                        "You have been un-banned",
                        "You may now log in again.");
                logger.info("Unban email sent to {}", u.getEmail());
            } catch (Exception e) {
                logger.error("Failed to send unban email to user: {}", u.getEmail(), e);
                // Decide if you want to rethrow the exception or continue
            }

            try {
                smsService.send(u.getPhone(),
                        "Your ban has been lifted.");
                logger.info("Unban SMS sent to {}", u.getPhone());
            } catch (Exception e) {
                logger.error("Failed to send unban SMS to user: {}", u.getPhone(), e);
                // Decide if you want to rethrow the exception or continue
            }

            recordAction(moderatorId, userId, "UNBAN_USER", null);
            logger.info("Unban action recorded for user {}", u.getUsername());

        } catch (IllegalArgumentException e) {
            logger.error("Error unbanning user: {}", e.getMessage());
            throw new RuntimeException("Failed to unban user: " + e.getMessage(), e); // Re-throw as RuntimeException for Spring to handle
        } catch (Exception e) {
            logger.error("An unexpected error occurred during user unban for user ID {}", userId, e);
            throw new RuntimeException("An unexpected error occurred during user unban.", e); // Catch other exceptions and re-throw
        }
    }

    @Transactional
    public void removeBug(Long moderatorId, Long bugId) {
        logger.info("Attempting to remove bug with ID {} by moderator with ID {}", bugId, moderatorId);
        try {
            bugService.deleteBug(bugId, moderatorId);
            recordAction(moderatorId, null, "REMOVE_BUG", "Removed bug id=" + bugId);
            logger.info("Bug with ID {} removed.", bugId);
        } catch (Exception e) {
            logger.error("Error removing bug with ID {}", bugId, e);
            throw new RuntimeException("Failed to remove bug: " + e.getMessage(), e);
        }
    }

    @Transactional
    public ModerationAction editBug(Long moderatorId, Long bugId, String newTitle, String newDesc) {
        logger.info("Attempting to edit bug with ID {} by moderator with ID {}", bugId, moderatorId);
        try {
            Map<String,Object> payload = new HashMap<>();
            payload.put("title", newTitle);
            payload.put("description", newDesc);
            bugService.updateBug(bugId, moderatorId, payload);
            ModerationAction action = recordAction(moderatorId, null, "EDIT_BUG", "Edited bug id=" + bugId);
            logger.info("Bug with ID {} edited.", bugId);
            return action;
        } catch (Exception e) {
            logger.error("Error editing bug with ID {}", bugId, e);
            throw new RuntimeException("Failed to edit bug: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void removeComment(Long moderatorId, Long commentId) {
        logger.info("Attempting to remove comment with ID {} by moderator with ID {}", commentId, moderatorId);
        try {
            commentService.deleteComment(commentId, moderatorId);
            recordAction(moderatorId, null, "REMOVE_COMMENT", "Removed comment id=" + commentId);
            logger.info("Comment with ID {} removed.", commentId);
        } catch (Exception e) {
            logger.error("Error removing comment with ID {}", commentId, e);
            throw new RuntimeException("Failed to remove comment: " + e.getMessage(), e);
        }
    }

    @Transactional
    public ModerationAction editComment(Long moderatorId, Long commentId, String newText) {
        logger.info("Attempting to edit comment with ID {} by moderator with ID {}", commentId, moderatorId);
        try {
            Map<String,Object> payload = new HashMap<>();
            payload.put("text", newText);
            commentService.updateComment(commentId, moderatorId, payload);
            ModerationAction action = recordAction(moderatorId, null, "EDIT_COMMENT", "Edited comment id=" + commentId);
            logger.info("Comment with ID {} edited.", commentId);
            return action;
        } catch (Exception e) {
            logger.error("Error editing comment with ID {}", commentId, e);
            throw new RuntimeException("Failed to edit comment: " + e.getMessage(), e);
        }
    }
}
