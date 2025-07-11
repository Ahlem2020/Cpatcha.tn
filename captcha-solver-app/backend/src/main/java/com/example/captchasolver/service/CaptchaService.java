package com.example.captchasolver.service;

import com.example.captchasolver.dto.CaptchaSolutionResponseDto;
import com.example.captchasolver.model.CaptchaSubmission;
import com.example.captchasolver.model.User;
import com.example.captchasolver.repository.CaptchaSubmissionRepository;
import com.example.captchasolver.repository.UserRepository;
import com.example.captchasolver.dto.CaptchaSolutionResponseDto; // Ensure this is imported
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate; // Import SimpMessagingTemplate
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CaptchaService {

    private static final Logger logger = LoggerFactory.getLogger(CaptchaService.class);

    @Autowired
    private CaptchaSubmissionRepository captchaSubmissionRepository;

    @Autowired
    private UserRepository userRepository;

    // Scheduler for mocking async solving
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    private SubscriptionService subscriptionService; // Injected SubscriptionService

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // Injected SimpMessagingTemplate

    @Transactional
    public CaptchaSubmission submitCaptcha(MultipartFile imageFile, String username) throws IOException, IllegalStateException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Check subscription and quota before proceeding
        if (!subscriptionService.hasActiveSubscription(username)) {
            throw new IllegalStateException("User does not have an active subscription.");
        }
        if (!subscriptionService.hasAvailableQuota(username)) {
            throw new IllegalStateException("User has exceeded their monthly CAPTCHA quota.");
        }

        CaptchaSubmission submission = new CaptchaSubmission();
        submission.setUser(user);
        submission.setImageData(imageFile.getBytes());
        submission.setOriginalFileName(imageFile.getOriginalFilename());
        submission.setContentType(imageFile.getContentType());
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus(CaptchaSubmission.CaptchaStatus.PENDING);

        CaptchaSubmission savedSubmission = captchaSubmissionRepository.save(submission);

        // Mock asynchronous solving process
        mockSolveCaptchaAsync(savedSubmission.getId(), username); // Pass username

        return savedSubmission;
    }

    private void mockSolveCaptchaAsync(Long submissionId, String username) { // Added username parameter
        scheduler.schedule(() -> {
            // Re-fetch user within this async task if needed by subscriptionService.recordQuotaUsage,
            // or pass the User object if appropriate and safe (beware detached entities in new transactions).
            // For simplicity, subscriptionService.recordQuotaUsage might need to fetch the user by username.
            // Let's assume subscriptionService.recordQuotaUsage can take username or fetches user itself.
            // The current SubscriptionService.recordQuotaUsage takes a User object.
            // So we should fetch the user here.

            Optional<CaptchaSubmission> optSubmission = captchaSubmissionRepository.findById(submissionId);
            if (optSubmission.isPresent()) {
                CaptchaSubmission submissionToUpdate = optSubmission.get();
                try {
                    // Simulate work
                    submissionToUpdate.setStatus(CaptchaSubmission.CaptchaStatus.SOLVING);
                    captchaSubmissionRepository.save(submissionToUpdate); // Save "SOLVING" state
                    logger.info("Captcha {} is now SOLVING.", submissionId);

                    Thread.sleep(5000); // Simulate 5 seconds of processing time

                    // Simulate a 80% success rate
                    if (Math.random() < 0.8) {
                        submissionToUpdate.setSolution("MOCK_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
                        submissionToUpdate.setStatus(CaptchaSubmission.CaptchaStatus.SOLVED);
                        submissionToUpdate.setSolvedAt(LocalDateTime.now());
                        captchaSubmissionRepository.save(submissionToUpdate); // Save before quota usage

                        // Record quota usage only on successful solve
                        User user = userRepository.findByUsername(username)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found for quota update: " + username));
                        subscriptionService.recordQuotaUsage(user, 1);

                        logger.info("Captcha {} SOLVED with solution: {}. Quota updated for user {}", submissionId, submissionToUpdate.getSolution(), username);
                        notifyCaptchaUpdate(username, submissionToUpdate);
                    } else {
                        submissionToUpdate.setStatus(CaptchaSubmission.CaptchaStatus.FAILED);
                        submissionToUpdate.setSolvedAt(LocalDateTime.now()); // Mark solve attempt time even for failure
                        captchaSubmissionRepository.save(submissionToUpdate);
                        logger.info("Captcha {} FAILED to solve.", submissionId);
                        notifyCaptchaUpdate(username, submissionToUpdate);
                    }
                } catch (InterruptedException e) {
                    logger.error("Mock solving interrupted for submission ID: {}", submissionId, e);
                    if (submissionToUpdate != null) { // submissionToUpdate might be null if findById failed, though unlikely here
                        submissionToUpdate.setStatus(CaptchaSubmission.CaptchaStatus.FAILED);
                        captchaSubmissionRepository.save(submissionToUpdate);
                        notifyCaptchaUpdate(username, submissionToUpdate); // Notify failure
                    }
                    Thread.currentThread().interrupt();
                } catch (Exception e) { // Catch broader exceptions during solving/notification
                    logger.error("Error during mock solving or notification for submission ID: {}", submissionId, e);
                    if (submissionToUpdate != null) {
                        submissionToUpdate.setStatus(CaptchaSubmission.CaptchaStatus.FAILED);
                        captchaSubmissionRepository.save(submissionToUpdate);
                        notifyCaptchaUpdate(username, submissionToUpdate); // Notify failure
                    }
                }
            } else {
                logger.warn("Submission ID {} not found for mock solving.", submissionId);
            }
        }, 1, TimeUnit.SECONDS); // Start solving after 1 second
    }

    private void notifyCaptchaUpdate(String username, CaptchaSubmission submission) {
        try {
            CaptchaSolutionResponseDto payload = CaptchaSolutionResponseDto.fromEntity(submission);
            // Destination: /user/{username}/queue/captcha-updates
            messagingTemplate.convertAndSendToUser(username, "/queue/captcha-updates", payload);
            logger.info("Sent CAPTCHA update to user {} for submission ID {}: Status {}", username, submission.getId(), submission.getStatus());
        } catch (Exception e) {
            logger.error("Error sending CAPTCHA update notification to user {} for submission ID {}: {}", username, submission.getId(), e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Optional<CaptchaSolutionResponseDto> getCaptchaSolution(Long submissionId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return captchaSubmissionRepository.findByIdAndUser(submissionId, user)
                .map(CaptchaSolutionResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<CaptchaSolutionResponseDto> getAllSubmissionsForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return captchaSubmissionRepository.findByUserOrderBySubmittedAtDesc(user)
                .stream()
                .map(CaptchaSolutionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // Consider adding a @PreDestroy method to shut down the scheduler gracefully
    // public void cleanup() {
    //     scheduler.shutdown();
    // }
}
