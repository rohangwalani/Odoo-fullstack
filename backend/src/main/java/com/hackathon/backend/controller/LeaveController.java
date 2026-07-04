package com.hackathon.backend.controller;

import com.hackathon.backend.dto.LeaveRequestDTO;
import com.hackathon.backend.dto.LeaveResponseDTO;
import com.hackathon.backend.service.LeaveService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping
    public ResponseEntity<LeaveResponseDTO> applyLeave(@RequestBody LeaveRequestDTO dto, Authentication authentication) {
        return ResponseEntity.ok(leaveService.applyForLeave(authentication.getName(), dto));
    }

    @GetMapping("/me")
    public ResponseEntity<List<LeaveResponseDTO>> getMyLeaves(Authentication authentication) {
        return ResponseEntity.ok(leaveService.getMyLeaves(authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<LeaveResponseDTO>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveResponseDTO> approveLeave(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String comments = body != null ? body.get("comments") : null;
        return ResponseEntity.ok(leaveService.approveLeave(id, comments));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveResponseDTO> rejectLeave(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String comments = body != null ? body.get("comments") : null;
        return ResponseEntity.ok(leaveService.rejectLeave(id, comments));
    }
}
