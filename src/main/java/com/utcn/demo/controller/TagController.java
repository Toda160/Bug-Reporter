package com.utcn.demo.controller;

import com.utcn.demo.entity.Tag;
import com.utcn.demo.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<Tag>> listAllTags() {
        List<Tag> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<Tag> retrieveTagById(@PathVariable Integer id) {
        Optional<Tag> tag = tagService.getTagById(id);
        return tag.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public ResponseEntity<Tag> createNewTag(@RequestBody Tag tag) {
        Tag createdTag = tagService.createTag(tag);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<Void> removeTagById(@PathVariable Integer id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}