package com.secureauth.auth.controller;

import com.secureauth.auth.dto.UserSummaryDto;
import com.secureauth.auth.model.User;
import com.secureauth.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserSummaryDto> listUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserSummaryDto(u.getId(), u.getEmail(), u.isEnabled(), u.getFailedAttempts()))
                .toList();
    }
}