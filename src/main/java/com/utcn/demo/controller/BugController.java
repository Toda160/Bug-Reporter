package com.utcn.demo.controller;

import com.utcn.demo.dto.BugDTO;
import com.utcn.demo.entity.Bug;
import com.utcn.demo.service.BugService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap; // Import HashMap

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
    public ResponseEntity<BugDTO> retrieveBugById(@PathVariable Long id) {
        Optional<Bug> bugOpt = bugService.getBugById(id);
        if (bugOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Bug bug = bugOpt.get();

        // Map Bug entity to BugDTO
        BugDTO dto = new BugDTO();
        dto.setId(bug.getId());
        dto.setTitle(bug.getTitle());
        dto.setDescription(bug.getDescription());
        dto.setImage(bug.getImage());
        dto.setStatus(bug.getStatus());
        dto.setCreatedAt(bug.getCreatedAt().toString());

        BugDTO.AuthorDTO authorDTO = new BugDTO.AuthorDTO();
        authorDTO.setId(bug.getAuthor().getId());
        authorDTO.setUsername(bug.getAuthor().getUsername());
        dto.setAuthor(authorDTO);

        return ResponseEntity.ok(dto);
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
    public ResponseEntity<?> removeBugById(@PathVariable Long id, @RequestParam Long userId) { // Changed return type to <?>
        try {
            bugService.deleteBug(id, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            Map<String, String> responseBody = new HashMap<>(); // Use HashMap
            if (e.getMessage() != null && e.getMessage().equals("Only the bug creator can delete their bug")) {
                responseBody.put("error", e.getMessage()); // Include message for forbidden
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseBody);
            }
            // Include the actual exception message for bad requests
            responseBody.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        }
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

    @PostMapping("/{bugId}/accept/{commentId}")
    public ResponseEntity<?> acceptComment(@PathVariable Long bugId, @PathVariable Long commentId,
                                           @RequestBody Map<String, Object> payload) {
        try {
            Long userId = Long.valueOf(payload.get("userId").toString());
            Bug updatedBug = bugService.acceptComment(bugId, commentId, userId);
            return ResponseEntity.ok(updatedBug);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            if (e.getMessage().equals("Only the bug creator can accept a comment")) {
                response.put("error", "Only the bug creator can accept a comment");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}