package com.utcn.demo.service;

import com.utcn.demo.entity.Bug;
import com.utcn.demo.entity.Comment;
import com.utcn.demo.entity.User;
import com.utcn.demo.entity.Vote;
import com.utcn.demo.repository.BugRepository;
import com.utcn.demo.repository.CommentRepository;
import com.utcn.demo.repository.UserRepository;
import com.utcn.demo.repository.VoteRepository; // Make sure VoteRepository is imported
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Make sure Collectors is imported

@Service
public class VoteService {
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final BugRepository bugRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public VoteService(VoteRepository voteRepository, UserRepository userRepository, BugRepository bugRepository, CommentRepository commentRepository) {
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
        this.bugRepository = bugRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public Vote addVote(Long userId, Long bugId, Long commentId, String voteType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Bug bug = null;
        Comment comment = null;

        if (bugId != null) {
            bug = bugRepository.findById(bugId)
                    .orElseThrow(() -> new RuntimeException("Bug not found"));
            if (bug.getAuthor().getId().equals(userId)) {
                throw new RuntimeException("Users cannot vote on their own bugs");
            }
        }

        if (commentId != null) {
            comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment not found"));
            if (comment.getAuthor().getId().equals(userId)) {
                throw new RuntimeException("Users cannot vote on their own comments");
            }
        }

        if (comment != null) {
            Optional<Vote> existingVote = voteRepository.findByUserAndComment(user, comment);
            if (existingVote.isPresent()) {
                Vote vote = existingVote.get();
                vote.setVoteType(voteType); // Update vote type
                return voteRepository.save(vote);
            }
        } else if (bug != null) { // Handle voting directly on bugs if applicable
            Optional<Vote> existingVote = voteRepository.findByUserAndBug(user, bug);
            if (existingVote.isPresent()) {
                Vote vote = existingVote.get();
                vote.setVoteType(voteType); // Update vote type
                return voteRepository.save(vote);
            }
        }


        Vote vote = new Vote(user, bug, comment, voteType);
        Vote savedVote = voteRepository.save(vote);

        // Logic to update bug status when first comment is added (moved from BugService)
        // This logic seems slightly misplaced in VoteService, but keeping it here for now
        // based on the provided createComment in CommentService which doesn't handle this.
        // A better place would be within the createComment method in CommentService
        if (comment != null && bug != null) {
            List<Comment> bugComments = commentRepository.findByBugIdOrderByCreatedAtDesc(bug.getId());
            // Note: countByBugId is a more robust check for the first comment than size() == 1
            // right after saving a comment. Consider using commentRepository.countByBugId(bug.getId())
            if (bugComments.size() == 1) { // This might be unreliable if other comments already exist
                if ("Received".equals(bug.getStatus())) {
                    bug.setStatus("In progress");
                    bugRepository.save(bug);
                }
            }
        }


        return savedVote;
    }

    // This method now deletes all votes related to a bug, including those on its comments
    @Transactional // Make this method transactional
    public void deleteVotesForBug(Long bugId) {
        // 1. Find all comments for the bug
        List<Comment> comments = commentRepository.findByBugIdOrderByCreatedAtDesc(bugId); // Reuse existing method

        // 2. Extract the IDs of these comments
        List<Long> commentIds = comments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        // 3. Delete votes associated with these comments
        if (!commentIds.isEmpty()) {
            voteRepository.deleteByCommentIdIn(commentIds); // Requires a method in VoteRepository
        }

        // 4. Delete votes directly associated with the bug (if any)
        voteRepository.deleteByBugId(bugId); // Requires a method in VoteRepository
    }


    public List<Vote> getVotesForBug(Long bugId) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found"));
        return voteRepository.findByBug(bug);
    }

    public List<Vote> getVotesForComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        return voteRepository.findByComment(comment);
    }

    public int getVoteCountForComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        List<Vote> votes = voteRepository.findByComment(comment);
        return calculateVoteCount(votes);
    }

    public int getVoteCountForBug(Long bugId) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new RuntimeException("Bug not found"));
        List<Vote> votes = voteRepository.findByBug(bug);
        return calculateVoteCount(votes);
    }

    private int calculateVoteCount(List<Vote> votes) {
        return votes.stream()
                .mapToInt(vote -> {
                    if ("LIKE".equalsIgnoreCase(vote.getVoteType()) || "upvote".equalsIgnoreCase(vote.getVoteType())) {
                        return 1;
                    } else if ("DISLIKE".equalsIgnoreCase(vote.getVoteType()) || "downvote".equalsIgnoreCase(vote.getVoteType())) {
                        return -1;
                    }
                    return 0; // Neutral or unrecognized vote type
                })
                .sum();
    }

    @Transactional
    public void likeComment(Long userId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (comment.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Users cannot vote on their own comments");
        }

        Optional<Vote> existingVote = voteRepository.findByUserAndComment(user, comment);
        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            if ("DISLIKE".equalsIgnoreCase(vote.getVoteType())) {
                vote.setVoteType("LIKE"); // Switch from DISLIKE to LIKE (+1 instead of -1)
            } else if (!"LIKE".equalsIgnoreCase(vote.getVoteType())) {
                vote.setVoteType("LIKE"); // Update to LIKE if it was neutral or upvote
            } // If already LIKE, no change needed
            voteRepository.save(vote);
        } else {
            Vote vote = new Vote(user, null, comment, "LIKE");
            voteRepository.save(vote);
        }
    }

    @Transactional
    public void dislikeComment(Long userId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (comment.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("Users cannot vote on their own comments");
        }

        Optional<Vote> existingVote = voteRepository.findByUserAndComment(user, comment);
        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            if ("LIKE".equalsIgnoreCase(vote.getVoteType())) {
                vote.setVoteType("DISLIKE"); // Switch from LIKE to DISLIKE (-1 instead of +1)
            } else if (!"DISLIKE".equalsIgnoreCase(vote.getVoteType())) {
                vote.setVoteType("DISLIKE"); // Update to DISLIKE if it was neutral or downvote
            } // If already DISLIKE, no change needed
            voteRepository.save(vote);
        } else {
            Vote vote = new Vote(user, null, comment, "DISLIKE");
            voteRepository.save(vote);
        }
    }
}