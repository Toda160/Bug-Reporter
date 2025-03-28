package com.utcn.demo.controller;

import com.utcn.demo.entity.Ban;
import com.utcn.demo.service.BanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bans")
public class BanController {

    private final BanService banService;

    public BanController(BanService banService) {
        this.banService = banService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<Ban>> listAllBans() {
        List<Ban> bans = banService.getAllBans();
        return ResponseEntity.ok(bans);
    }

    @PostMapping("/create")
    public ResponseEntity<Ban> createNewBan(@RequestBody Ban ban) {
        Ban createdBan = banService.createBan(ban);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBan);
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<Void> removeBanById(@PathVariable Integer id) {
        banService.deleteBan(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Ban> updateBan(@PathVariable Integer id, @RequestBody Ban updatedBan) {
        Ban ban = banService.updateBan(id, updatedBan);
        return ResponseEntity.ok(ban);
    }

}