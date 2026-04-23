package com.cinetix.identity.adapter.rest;

import com.cinetix.common.web.ApiResponse;
import com.cinetix.identity.domain.model.UserProfile;
import com.cinetix.identity.infrastructure.persistence.UserProfileJpaRepository;
import com.cinetix.security.CurrentUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileJpaRepository userRepo;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getMe(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        UserProfile profile = userRepo.findById(userId).orElseGet(() -> {
            // Auto-create on first access (sync from Keycloak token claims)
            var p = UserProfile.builder()
                .id(userId)
                .email(jwt.getClaimAsString("email"))
                .fullName(jwt.getClaimAsString("name") != null ? jwt.getClaimAsString("name") : jwt.getClaimAsString("preferred_username"))
                .build();
            return userRepo.save(p);
        });
        return ResponseEntity.ok(ApiResponse.success(toDto(profile)));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateMe(
        @AuthenticationPrincipal Jwt jwt,
        @RequestBody UpdateProfileRequest req) {
        UUID userId = UUID.fromString(jwt.getSubject());
        UserProfile profile = userRepo.findById(userId)
            .orElseThrow(() -> new com.cinetix.common.exception.ResourceNotFoundException("User not found"));
        profile.update(req.fullName(), req.phone(), req.avatarUrl());
        userRepo.save(profile);
        return ResponseEntity.ok(ApiResponse.success(toDto(profile)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileDto>> getById(@PathVariable UUID id) {
        return userRepo.findById(id)
            .map(p -> ResponseEntity.ok(ApiResponse.success(toDto(p))))
            .orElse(ResponseEntity.notFound().build());
    }

    private UserProfileDto toDto(UserProfile p) {
        return new UserProfileDto(p.getId(), p.getEmail(), p.getFullName(),
            p.getPhone(), p.getAvatarUrl(), p.getStatus().name(), p.getCreatedAt());
    }

    public record UserProfileDto(UUID id, String email, String fullName,
                                  String phone, String avatarUrl, String status, Instant createdAt) {}

    public record UpdateProfileRequest(String fullName, String phone, String avatarUrl) {}
}
