package com.utcn.demo.controller;

import com.utcn.demo.entity.BugTag;
import com.utcn.demo.service.BugTagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bug-tags")
public class BugTagController {

    private final BugTagService bugTagService;

    public BugTagController(BugTagService bugTagService) {
        this.bugTagService = bugTagService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<BugTag>> listAllBugTags() {
        List<BugTag> bugTags = bugTagService.getAllBugTags();
        return ResponseEntity.ok(bugTags);
    }

    @PostMapping("/create")
    public ResponseEntity<BugTag> createNewBugTag(@RequestBody BugTag bugTag) {
        BugTag createdBugTag = bugTagService.createBugTag(bugTag);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBugTag);
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<Void> removeBugTagById(@PathVariable Integer id) {
        bugTagService.deleteBugTag(id);
        return ResponseEntity.noContent().build();
    }
}