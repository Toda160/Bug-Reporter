package com.utcn.demo.service;

import com.utcn.demo.entity.Tag;
import com.utcn.demo.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public Optional<Tag> getTagById(Integer id) {
        return tagRepository.findById(id);
    }

    public Tag createTag(Tag tag) {
        return tagRepository.save(tag);
    }

    public void deleteTag(Integer id) {
        tagRepository.deleteById(id);
    }
}
