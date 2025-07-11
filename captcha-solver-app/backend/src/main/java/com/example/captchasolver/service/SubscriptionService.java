package com.example.captchasolver.service;

import com.example.captchasolver.model.SubscriptionStatus;
import com.example.captchasolver.model.User;
import com.example.captchasolver.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class SubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    @Autowired
    private UserRepository userRepository;

    public static final int DEFAULT_MONTHLY_QUOTA = 10; // As per $5 for 10 resolutions

    // This method would typically be called by an admin or after a successful payment webhook
    @Transactional
    // @PreAuthorize("hasRole('ADMIN')") // Secure this endpoint/method appropriately
    public User activateSubscription(String username, int months) {
        if (months <= 0) {
            throw new IllegalArgumentException("Subscription duration in months must be positive.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found with username: " + username));

        user.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        user.setMonthlyQuota(DEFAULT_MONTHLY_QUOTA); // Set the standard quota
        user.setQuotaUsed(0); // Reset quota used
        user.setSubscriptionStartDate(LocalDate.now());
        user.setSubscriptionEndDate(LocalDate.now().plusMonths(months));

        logger.info("Activated {} month(s) subscription for user {}. Quota: {}. End date: {}",
                months, username, user.getMonthlyQuota(), user.getSubscriptionEndDate());
        return userRepository.save(user);
    }

    @Transactional
    // @PreAuthorize("hasRole('ADMIN')")
    public User cancelSubscription(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found with username: " + username));

        user.setSubscriptionStatus(SubscriptionStatus.CANCELED);
        // Optionally, you might want to keep the end date or set it to now.
        // Quota could be prorated or zeroed out depending on business rules.
        // For simplicity, we'll just mark as canceled. The quota reset logic should handle expired/canceled status.
        logger.info("Canceled subscription for user {}", username);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found with username: " + username));

        return user.getSubscriptionStatus() == SubscriptionStatus.ACTIVE &&
               (user.getSubscriptionEndDate() == null || user.getSubscriptionEndDate().isAfter(LocalDate.now().minusDays(1))); // end date is inclusive
    }

    @Transactional(readOnly = true)
    public boolean hasAvailableQuota(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: User not found with username: " + username));

        return user.getQuotaUsed() < user.getMonthlyQuota();
    }

    @Transactional
    public void recordQuotaUsage(User user, int usage) {
        if (user.getSubscriptionStatus() != SubscriptionStatus.ACTIVE) {
            // This check might be redundant if hasActiveSubscription is called first, but good for safety
            throw new IllegalStateException("User does not have an active subscription.");
        }
        if (user.getQuotaUsed() + usage > user.getMonthlyQuota()) {
            throw new IllegalStateException("Quota exceeded for user: " + user.getUsername());
        }
        user.setQuotaUsed(user.getQuotaUsed() + usage);
        userRepository.save(user);
        logger.info("Recorded {} quota usage for user {}. New quota used: {}/{}", usage, user.getUsername(), user.getQuotaUsed(), user.getMonthlyQuota());
    }

    // Method for admin/test controller to call
    // This is just a placeholder for where such a controller method might live.
    // It's better to have a dedicated AdminController.
    public User activateTestSubscription(String username) {
        return activateSubscription(username, 1); // Activate a 1-month subscription
    }
}
