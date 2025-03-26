package com.utcn.demo.controller;

import com.utcn.demo.entity.Bug;
import com.utcn.demo.service.BugService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bugs")
public class BugController {
    private final BugService bugService;

    public BugController(BugService bugService) {
        this.bugService = bugService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<Bug>> listAllBugs() {
        List<Bug> bugs = bugService.getAllBugs();
        return ResponseEntity.ok(bugs);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<Bug> retrieveBugById(@PathVariable Long id) {
        return bugService.getBugById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/report")
    public ResponseEntity<Bug> reportNewBug(@RequestBody Map<String, Object> payload) {
        Long authorId = Long.valueOf(payload.get("authorId").toString());
        String title = (String) payload.get("title");
        String description = (String) payload.get("description");
        String image = (String) payload.get("image");
        String status = (String) payload.get("status");

        Bug createdBug = bugService.createBug(authorId, title, description, image, status);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBug);
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<Void> removeBugById(@PathVariable Long id) {
        bugService.deleteBug(id);
        return ResponseEntity.noContent().build();
    }
}