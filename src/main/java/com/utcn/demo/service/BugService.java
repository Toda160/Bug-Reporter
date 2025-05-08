package com.utcn.demo.service;

import com.utcn.demo.entity.Bug;
import com.utcn.demo.entity.User;
import com.utcn.demo.repository.BugRepository;
import com.utcn.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.utcn.demo.dto.BugDTO;
import com.utcn.demo.dto.TagDTO;
import com.utcn.demo.dto.UserDTO;
import com.utcn.demo.entity.BugTag;
import java.util.stream.Collectors;
import com.utcn.demo.repository.TagRepository;
import com.utcn.demo.repository.BugTagRepository;
import com.utcn.demo.entity.Tag;

@Service
public class BugService {
    private final BugRepository bugRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final BugTagRepository bugTagRepository;

    @Autowired
    public BugService(BugRepository bugRepository, UserRepository userRepository, TagRepository tagRepository, BugTagRepository bugTagRepository) {
        this.bugRepository = bugRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.bugTagRepository = bugTagRepository;
    }

    public List<Bug> getAllBugs() {
        return bugRepository.findAll();
    }

    public Optional<Bug> getBugById(Long id) {
        return bugRepository.findById(id);
    }

    public Bug createBug(Long authorId, String title, String description, String image, String status, List<Integer> tagIds) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Bug newBug = new Bug(author, title, description, image, status);
        Bug savedBug = bugRepository.save(newBug);

        if (tagIds != null) {
            for (Integer tagId : tagIds) {
                Tag tag = tagRepository.findById(tagId)
                        .orElseThrow(() -> new RuntimeException("Tag not found"));
                BugTag bugTag = new BugTag();
                bugTag.setBug(savedBug);
                bugTag.setTag(tag);
                bugTagRepository.save(bugTag);
            }
        }
        return savedBug;
    }

    public void deleteBug(Long id) {
        bugRepository.deleteById(id);
    }

    public List<Bug> getBugsByUserId(Long userId) {
        User author = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return bugRepository.findByAuthor(author);
    }

    public Bug updateBug(Long id, Map<String, Object> payload) {
        Bug bug = bugRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bug not found with id: " + id));
        
        if (payload.containsKey("title")) {
            bug.setTitle((String) payload.get("title"));
        }
        if (payload.containsKey("description")) {
            bug.setDescription((String) payload.get("description"));
        }
        if (payload.containsKey("image")) {
            bug.setImage((String) payload.get("image"));
        }
        if (payload.containsKey("status")) {
            String status = (String) payload.get("status");
            if (status != null && !status.isEmpty()) {
                bug.setStatus(status);
            }
        }
        
        return bugRepository.save(bug);
    }

    public BugDTO toDTO(Bug bug) {
        BugDTO dto = new BugDTO();
        dto.id = bug.getId();
        dto.title = bug.getTitle();
        dto.description = bug.getDescription();
        dto.image = bug.getImage();
        dto.status = bug.getStatus();
        dto.createdAt = bug.getCreatedAt();

        // Author
        UserDTO userDTO = new UserDTO();
        userDTO.id = bug.getAuthor().getId();
        userDTO.username = bug.getAuthor().getUsername();
        dto.author = userDTO;

        // Tags
        dto.tags = bug.getBugTags().stream().map(bugTag -> {
            TagDTO tagDTO = new TagDTO();
            tagDTO.id = bugTag.getTag().getId();
            tagDTO.name = bugTag.getTag().getName();
            return tagDTO;
        }).collect(Collectors.toList());

        return dto;
    }
}
