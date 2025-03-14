package com.utcn.demo.controller;

import com.utcn.demo.entity.Ban;
import com.utcn.demo.service.BanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
///////




////
//
//
@RestController
@RequestMapping("/bans")
public class BanController {

    private final BanService banService;

    public BanController(BanService banService) {
        this.banService = banService;
    }

    @GetMapping
    public List<Ban> getAllBans() {
        return banService.getAllBans();
    }

    @PostMapping
    public Ban createBan(@RequestBody Ban ban) {
        return banService.createBan(ban);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBan(@PathVariable Integer id) {
        banService.deleteBan(id);
        return ResponseEntity.noContent().build();
    }
}
