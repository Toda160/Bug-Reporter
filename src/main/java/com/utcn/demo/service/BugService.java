package com.utcn.demo.service;

import com.utcn.demo.entity.Comment;
import com.utcn.demo.entity.Bug;
import com.utcn.demo.entity.User;
import com.utcn.demo.repository.CommentRepository;
import com.utcn.demo.repository.BugRepository;
import com.utcn.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.utcn.demo.entity.BugTag;
import com.utcn.demo.repository.TagRepository;
import com.utcn.demo.repository.BugTagRepository; // Import BugTagRepository
import com.utcn.demo.entity.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


@Service
public class BugService {
    private final BugRepository bugRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final BugTagRepository bugTagRepository; // Inject BugTagRepository
    private final VoteService voteService; // Assuming VoteService handles vote deletion and retrieval
    private final CommentService commentService;
    private final CommentRepository commentRepository; // Assuming CommentRepository handles comment deletion

    @Autowired
    public BugService(BugRepository bugRepository, UserRepository userRepository,
                      TagRepository tagRepository, BugTagRepository bugTagRepository, // Inject BugTagRepository
                      VoteService voteService, CommentService commentService,
                      CommentRepository commentRepository) {
        this.bugRepository = bugRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.bugTagRepository = bugTagRepository; // Assign BugTagRepository
        this.voteService = voteService;
        this.commentService = commentService;
        this.commentRepository = commentRepository;
    }

    public List<Bug> getAllBugs() {
        return bugRepository.findAll();
    }

    public Optional<Bug> getBugById(Long id) {
        return bugRepository.findById(id);
    }

    public Bug createBug(Long authorId, String title, String description, String image, String status, List<Integer> tagIds) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Bug newBug = new Bug(author, title, description, image, "Received");
        Bug savedBug = bugRepository.save(newBug);

        if (tagIds != null) {
            for (Integer tagId : tagIds) {
                Tag tag = tagRepository.findById(tagId)
                        .orElseThrow(() -> new RuntimeException("Tag not found"));
                BugTag bugTag = new BugTag();
                bugTag.setBug(savedBug);
                bugTag.setTag(tag);
                bugTagRepository.save(bugTag);
            }
        }
        return savedBug;
    }

    @Transactional
    public void deleteBug(Long bugId, Long actorUserId) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (actor.getBanned() == true) {
            throw new RuntimeException("Banned users cannot delete bugs");
        }

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found with id: " + bugId));

        boolean isAuthor    = bug.getAuthor().getId().equals(actorUserId);
        boolean isModerator = "MODERATOR".equals(actor.getRole());
        if (!isAuthor && !isModerator) {
            throw new RuntimeException("Only the creator or a moderator can delete this bug");
        }

        // same cleanup order as before:
        bugTagRepository.deleteByBugId(bugId);
        voteService.deleteVotesForBug(bugId);
        commentRepository.deleteByBugId(bugId);
        bugRepository.deleteById(bugId);
    }

    public List<Bug> getBugsByUserId(Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bugRepository.findByAuthor(author);
    }

    @Transactional
    public Bug updateBug(Long bugId, Long actorUserId, Map<String,Object> payload) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2) Load the bug
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found with id: " + bugId));

        // 3) Enforce "only author or moderator" rule
        boolean isAuthor    = bug.getAuthor().getId().equals(actorUserId);
        boolean isModerator = "MODERATOR".equals(actor.getRole());
        if (!isAuthor && !isModerator) {
            throw new RuntimeException("Only the creator or a moderator can edit this bug");
        }

        // 4) Apply any fields from the payload exactly as before
        if (payload.containsKey("title")) {
            bug.setTitle((String) payload.get("title"));
        }
        if (payload.containsKey("description")) {
            bug.setDescription((String) payload.get("description"));
        }
        if (payload.containsKey("image")) {
            bug.setImage((String) payload.get("image"));
        }
        if (payload.containsKey("status")) {
            String newStatus = (String) payload.get("status");
            // preserve your "can't mark Solved without comments" rule
            if ("Solved".equals(newStatus)
                    && commentService.getCommentsByBugId(bugId).isEmpty()) {
                throw new RuntimeException("Cannot mark bug as solved without comments");
            }
            bug.setStatus(newStatus);
        }

        return bugRepository.save(bug);
    }


    @Transactional
    public Bug acceptComment(Long bugId, Long commentId, Long userId) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!bug.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Only the bug creator can accept a comment");
        }

        if (!comment.getBug().getId().equals(bugId)) {
            throw new RuntimeException("Comment does not belong to this bug");
        }

        comment.setAccepted(true);
        commentRepository.save(comment);

        bug.setAcceptedComment(comment);
        bug.setStatus("Solved");
        return bugRepository.save(bug);
    }

    public long getBugCount() {
        return bugRepository.count();
    }

    // Added method to get bug vote count by calling VoteService
    public int getVoteCountForBug(Long bugId) {
        return voteService.getVoteCountForBug(bugId);
    }
}