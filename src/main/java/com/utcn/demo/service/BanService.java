package com.utcn.demo.service;

import com.utcn.demo.entity.Ban;
import com.utcn.demo.repository.BanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BanService {

    private final BanRepository banRepository;

    public BanService(BanRepository banRepository) {
        this.banRepository = banRepository;
    }

    public List<Ban> getAllBans() {
        return banRepository.findAll();
    }

    public Ban createBan(Ban ban) {
        return banRepository.save(ban);
    }

    public void deleteBan(Integer id) {
        banRepository.deleteById(id);
    }

    public Ban updateBan(Integer id, Ban updatedBan) {
        return banRepository.findById(id).map(ban -> {
            ban.setReason(updatedBan.getReason()); // Actualizează doar motivul banului
            return banRepository.save(ban);
        }).orElseThrow(() -> new RuntimeException("Ban not found"));
    }


}
