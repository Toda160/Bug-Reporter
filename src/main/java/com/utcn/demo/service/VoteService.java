package com.utcn.demo.service;

import com.utcn.demo.entity.Bug;
import com.utcn.demo.entity.Comment;
import com.utcn.demo.entity.User;
import com.utcn.demo.entity.Vote;
import com.utcn.demo.repository.BugRepository;
import com.utcn.demo.repository.CommentRepository;
import com.utcn.demo.repository.UserRepository;
import com.utcn.demo.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VoteService {
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final BugRepository bugRepository;
    private final CommentRepository commentRepository;
    private final UserService userService; // Inject UserService

    @Autowired
    public VoteService(VoteRepository voteRepository, UserRepository userRepository, BugRepository bugRepository, CommentRepository commentRepository, UserService userService) {
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
        this.bugRepository = bugRepository;
        this.commentRepository = commentRepository;
        this.userService = userService; // Assign UserService
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

        Vote savedVote;
        String oldVoteType = null;

        if (comment != null) {
            Optional<Vote> existingVote = voteRepository.findByUserAndComment(user, comment);
            if (existingVote.isPresent()) {
                Vote vote = existingVote.get();
                oldVoteType = vote.getVoteType();
                vote.setVoteType(voteType); // Update vote type
                savedVote = voteRepository.save(vote);

                double scoreChangeForAuthor = calculateCommentVoteScoreChange(oldVoteType, voteType);
                double scoreChangeForVoter = 0.0;
                if (!user.getId().equals(comment.getAuthor().getId())) { // Apply voter score change only if not author
                    if ("downvote".equalsIgnoreCase(oldVoteType) && !"downvote".equalsIgnoreCase(voteType)) {
                        scoreChangeForVoter += 1.5; // Voter is no longer downvoting, gain back points
                    } else if (!"downvote".equalsIgnoreCase(oldVoteType) && "downvote".equalsIgnoreCase(voteType)) {
                        scoreChangeForVoter -= 1.5; // Voter is now downvoting, lose points
                    }
                }


                if (scoreChangeForAuthor != 0.0) {
                    userService.updateUserScore(comment.getAuthor().getId(), scoreChangeForAuthor);
                }
                if (scoreChangeForVoter != 0.0) {
                    userService.updateUserScore(user.getId(), scoreChangeForVoter);
                }

            } else {
                // New vote on a comment
                Vote vote = new Vote(user, null, comment, voteType);
                savedVote = voteRepository.save(vote);

                double scoreChangeForAuthor = "upvote".equalsIgnoreCase(voteType) ? 5.0 : ("downvote".equalsIgnoreCase(voteType) ? -2.5 : 0.0);
                double scoreChangeForVoter = 0.0;
                if ("downvote".equalsIgnoreCase(voteType) && !user.getId().equals(comment.getAuthor().getId())) {
                    scoreChangeForVoter -= 1.5; // Voter downvoted, lose points (if not author)
                }


                if (scoreChangeForAuthor != 0.0) {
                    userService.updateUserScore(comment.getAuthor().getId(), scoreChangeForAuthor);
                }
                if (scoreChangeForVoter != 0.0) {
                    userService.updateUserScore(user.getId(), scoreChangeForVoter);
                }
            }

        } else if (bug != null) {
            Optional<Vote> existingVote = voteRepository.findByUserAndBug(user, bug);
            if (existingVote.isPresent()) {
                Vote vote = existingVote.get();
                oldVoteType = vote.getVoteType();
                vote.setVoteType(voteType); // Update vote type
                savedVote = voteRepository.save(vote);
                // Calculate and apply score change for bug author based on vote type change
                double scoreChangeForAuthor = calculateBugVoteScoreChange(oldVoteType, voteType);
                if (scoreChangeForAuthor != 0.0) {
                    userService.updateUserScore(bug.getAuthor().getId(), scoreChangeForAuthor);
                }

            } else {
                // New vote on a bug
                Vote vote = new Vote(user, bug, null, voteType);
                savedVote = voteRepository.save(vote);
                // Calculate and apply score change for bug author
                double scoreChangeForAuthor = "upvote".equalsIgnoreCase(voteType) ? 2.5 : ("downvote".equalsIgnoreCase(voteType) ? -1.5 : 0.0);
                if (scoreChangeForAuthor != 0.0) {
                    userService.updateUserScore(bug.getAuthor().getId(), scoreChangeForAuthor);
                }
            }
        } else {
            throw new RuntimeException("Vote must be associated with a bug or a comment");
        }


        // Logic to update bug status when first comment is added
        // This logic is still here but consider moving it to CommentService.createComment
        if (comment != null && bug != null) {
            long commentCount = commentRepository.countByBugId(bug.getId()); // Using countByBugId for robustness
            if (commentCount == 1) { // Check if this was the first comment added
                if ("Received".equals(bug.getStatus())) {
                    bug.setStatus("In progress");
                    bugRepository.save(bug);
                }
            }
        }


        return savedVote;
    }

    // Helper to calculate score change for comment votes
    private double calculateCommentVoteScoreChange(String oldVoteType, String newVoteType) {
        double oldScore = "upvote".equalsIgnoreCase(oldVoteType) ? 5.0 : ("downvote".equalsIgnoreCase(oldVoteType) ? -2.5 : 0.0);
        double newScore = "upvote".equalsIgnoreCase(newVoteType) ? 5.0 : ("downvote".equalsIgnoreCase(newVoteType) ? -2.5 : 0.0);
        return newScore - oldScore;
    }

    // Helper to calculate score change for bug votes
    private double calculateBugVoteScoreChange(String oldVoteType, String newVoteType) {
        double oldScore = "upvote".equalsIgnoreCase(oldVoteType) ? 2.5 : ("downvote".equalsIgnoreCase(oldVoteType) ? -1.5 : 0.0);
        double newScore = "upvote".equalsIgnoreCase(newVoteType) ? 2.5 : ("downvote".equalsIgnoreCase(newVoteType) ? -1.5 : 0.0);
        return newScore - oldScore;
    }


    @Transactional // Make this method transactional
    public void deleteVotesForBug(Long bugId) {
        // Before deleting votes, fetch related comments and votes to adjust user scores
        List<Comment> comments = commentRepository.findByBugIdOrderByCreatedAtDesc(bugId);
        List<Long> commentIds = comments.stream().map(Comment::getId).collect(Collectors.toList());

        // Revert scores for votes on comments
        if (!commentIds.isEmpty()) {
            List<Vote> votesOnComments = voteRepository.findByCommentIdIn(commentIds);
            for (Vote vote : votesOnComments) {
                double scoreToRevertAuthor = calculateCommentVoteScoreChange(vote.getVoteType(), "neutral"); // Revert the score gained/lost by author
                userService.updateUserScore(vote.getComment().getAuthor().getId(), scoreToRevertAuthor);

                // Revert voter's score if they downvoted a comment
                if ("downvote".equalsIgnoreCase(vote.getVoteType()) && !vote.getUser().getId().equals(vote.getComment().getAuthor().getId())) {
                    userService.updateUserScore(vote.getUser().getId(), 1.5); // Gain back points lost for downvoting
                }
            }
            // Now delete the votes on comments
            voteRepository.deleteByCommentIdIn(commentIds);
        }

        // Revert scores for votes directly on the bug
        List<Vote> votesOnBug = voteRepository.findByBugId(bugId);
        if (!votesOnBug.isEmpty()) {
            for (Vote vote : votesOnBug) {
                double scoreToRevertAuthor = calculateBugVoteScoreChange(vote.getVoteType(), "neutral"); // Revert the score gained/lost by author
                userService.updateUserScore(vote.getBug().getAuthor().getId(), scoreToRevertAuthor);
                // No voter score change for downvoting bugs based on requirements
            }
            // Now delete the votes on the bug
            voteRepository.deleteByBugId(bugId);
        }
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

    // likeComment and dislikeComment can now primarily call addVote with the correct type
    @Transactional
    public void likeComment(Long userId, Long commentId) {
        addVote(userId, null, commentId, "upvote");
    }

    public long getVoteCount() {
        return voteRepository.count();
    }

    @Transactional
    public void dislikeComment(Long userId, Long commentId) {
        addVote(userId, null, commentId, "downvote");
    }


}