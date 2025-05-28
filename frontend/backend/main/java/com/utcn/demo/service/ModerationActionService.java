package com.utcn.demo.service;

import com.utcn.demo.entity.ModerationAction;
import com.utcn.demo.repository.ModerationActionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModerationActionService {

    private final ModerationActionRepository moderationActionRepository;

    public ModerationActionService(ModerationActionRepository moderationActionRepository) {
        this.moderationActionRepository = moderationActionRepository;
    }

    public List<ModerationAction> getAllActions() {
        return moderationActionRepository.findAll();
    }

    public ModerationAction createAction(ModerationAction action) {
        return moderationActionRepository.save(action);
    }

    public void deleteAction(Integer id) {
        moderationActionRepository.deleteById(id);
    }
}
