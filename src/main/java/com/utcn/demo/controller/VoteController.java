package com.utcn.demo.controller;

import com.utcn.demo.entity.Vote;
import com.utcn.demo.service.VoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/votes")
public class VoteController {
    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping("/create")
    public ResponseEntity<Vote> registerNewVote(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        Long bugId = payload.get("bugId") != null ? Long.valueOf(payload.get("bugId").toString()) : null;
        Long commentId = payload.get("commentId") != null ? Long.valueOf(payload.get("commentId").toString()) : null;
        String voteType = (String) payload.get("voteType");

        Vote createdVote = voteService.addVote(userId, bugId, commentId, voteType);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVote);
    }
}