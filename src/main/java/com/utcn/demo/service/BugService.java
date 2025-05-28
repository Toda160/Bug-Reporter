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

@Service
public class BugService {
    private final BugRepository bugRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final BugTagRepository bugTagRepository; // Inject BugTagRepository
    private final VoteService voteService; // Assuming VoteService handles vote deletion
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
    public void deleteBug(Long id, Long userId) {
        try {
            Bug bug = bugRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Bug not found with id: " + id));

            // Check if the logged-in user is the author of the bug
            if (!bug.getAuthor().getId().equals(userId)) {
                throw new RuntimeException("Only the bug creator can delete their bug");
            }

            // Delete associated BugTag entries first
            bugTagRepository.deleteByBugId(id); // Ensure this method exists and works

            // Delete associated votes for the bug and its comments (Crucial: do this BEFORE deleting comments)
            // This method in VoteService needs to handle deleting votes linked to this bug
            // AND votes linked to comments under this bug.
            voteService.deleteVotesForBug(id); // Ensure this method exists and works correctly

            // Delete associated comments next
            commentRepository.deleteByBugId(id); // Ensure this method exists and works

            // Finally, delete the bug
            bugRepository.deleteById(id);
        } catch (RuntimeException e) {
            // Log the full stack trace on the backend console
            e.printStackTrace();
            // Re-throw the exception so the controller can catch and return the 400
            throw e;
        }
    }

    public List<Bug> getBugsByUserId(Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bugRepository.findByAuthor(author);
    }

    public Bug updateBug(Long id, Map<String, Object> payload) {
        Bug bug = bugRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bug not found with id: " + id));

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
            String status = (String) payload.get("status");
            if (status != null && !status.isEmpty()) {
                if ("Solved".equals(status) && commentService.getCommentsByBugId(id).isEmpty()) {
                    throw new RuntimeException("Cannot mark bug as solved without comments");
                }
                bug.setStatus(status);
            }
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
}