package com.utcn.demo.controller;

import com.utcn.demo.entity.Bug;
import com.utcn.demo.service.BugService;
import com.utcn.demo.dto.BugDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bugs")
public class BugController {
    private final BugService bugService;

    public BugController(BugService bugService) {
        this.bugService = bugService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<BugDTO>> listAllBugs() {
        List<BugDTO> bugDTOs = bugService.getAllBugs().stream()
            .map(bugService::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(bugDTOs);
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
        List<Integer> tagIds = null;
        if (payload.get("tagIds") != null) {
            tagIds = (List<Integer>) payload.get("tagIds");
        }
        Bug createdBug = bugService.createBug(authorId, title, description, image, status, tagIds);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBug);
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<Void> removeBugById(@PathVariable Long id) {
        bugService.deleteBug(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Bug>> getBugsByUserId(@PathVariable Long userId) {
        List<Bug> bugs = bugService.getBugsByUserId(userId);
        return ResponseEntity.ok(bugs);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Bug> updateBug(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            Bug updatedBug = bugService.updateBug(id, payload);
            return ResponseEntity.ok(updatedBug);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}