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

import java.util.List;

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

    public Vote addVote(Long userId, Long bugId, Long commentId, String voteType) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Bug bug = (bugId != null) ? bugRepository.findById(bugId).orElse(null) : null;
        Comment comment = (commentId != null) ? commentRepository.findById(commentId).orElse(null) : null;

        if (bug == null && comment == null) {
            throw new RuntimeException("Vote must be associated with a bug or a comment");
        }

        Vote vote = new Vote(user, bug, comment, voteType);
        return voteRepository.save(vote);
    }

    public List<Vote> getVotesForBug(Long bugId) {
        Bug bug = bugRepository.findById(bugId).orElseThrow(() -> new RuntimeException("Bug not found"));
        return voteRepository.findByBug(bug);
    }

    public List<Vote> getVotesForComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        return voteRepository.findByComment(comment);
    }
}
