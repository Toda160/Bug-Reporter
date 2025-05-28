package com.utcn.demo.service;

import com.utcn.demo.entity.BugTag;
import com.utcn.demo.repository.BugTagRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BugTagService {

    private final BugTagRepository bugTagRepository;

    public BugTagService(BugTagRepository bugTagRepository) {
        this.bugTagRepository = bugTagRepository;
    }

    public List<BugTag> getAllBugTags() {
        return bugTagRepository.findAll();
    }

    public BugTag createBugTag(BugTag bugTag) {
        return bugTagRepository.save(bugTag);
    }

    public void deleteBugTag(Integer id) {
        bugTagRepository.deleteById(id);
    }
}
