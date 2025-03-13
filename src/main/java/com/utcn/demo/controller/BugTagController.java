package com.utcn.demo.controller;

import com.utcn.demo.entity.BugTag;
import com.utcn.demo.service.BugTagService;
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

    @GetMapping
    public List<BugTag> getAllBugTags() {
        return bugTagService.getAllBugTags();
    }

    @PostMapping
    public BugTag createBugTag(@RequestBody BugTag bugTag) {
        return bugTagService.createBugTag(bugTag);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBugTag(@PathVariable Integer id) {
        bugTagService.deleteBugTag(id);
        return ResponseEntity.noContent().build();
    }
}
