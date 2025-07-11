package com.example.captchasolver.controller;

import com.example.captchasolver.dto.CaptchaSolutionResponseDto;
import com.example.captchasolver.dto.CaptchaSubmitResponseDto;
import com.example.captchasolver.dto.MessageResponse;
import com.example.captchasolver.model.CaptchaSubmission;
import com.example.captchasolver.service.CaptchaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600) // Allow all origins for now
@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    private static final Logger logger = LoggerFactory.getLogger(CaptchaController.class);

    @Autowired
    private CaptchaService captchaService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitCaptcha(@RequestParam("image") MultipartFile imageFile, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Error: User not authenticated."));
        }
        String username = principal.getName();

        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Image file is empty."));
        }

        // Optional: Add more validation for file type, size, etc.
        // if (!imageFile.getContentType().startsWith("image/")) {
        //     return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid file type. Only images are allowed."));
        // }

        try {
            CaptchaSubmission submission = captchaService.submitCaptcha(imageFile, username);
            return ResponseEntity.ok(new CaptchaSubmitResponseDto(
                    submission.getId(),
                    submission.getStatus(),
                    "CAPTCHA submitted successfully. ID: " + submission.getId()
            ));
        } catch (IOException e) {
            logger.error("Error processing uploaded CAPTCHA image for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error: Could not process image. " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error submitting CAPTCHA for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error: An unexpected error occurred. " + e.getMessage()));
        }
    }

    @GetMapping("/solution/{id}")
    public ResponseEntity<?> getCaptchaSolution(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Error: User not authenticated."));
        }
        String username = principal.getName();

        Optional<CaptchaSolutionResponseDto> solutionDto = captchaService.getCaptchaSolution(id, username);

        return solutionDto.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Error: CAPTCHA submission not found or access denied.")));
    }

    @GetMapping("/history")
    public ResponseEntity<?> getCaptchaHistory(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Error: User not authenticated."));
        }
        String username = principal.getName();
        List<CaptchaSolutionResponseDto> history = captchaService.getAllSubmissionsForUser(username);
        return ResponseEntity.ok(history);
    }

    // Helper to get username, can also use @AuthenticationPrincipal UserDetails userDetails
    private String getUsername(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        if (principal instanceof String) {
            return (String) principal;
        }
        return null;
    }
}
