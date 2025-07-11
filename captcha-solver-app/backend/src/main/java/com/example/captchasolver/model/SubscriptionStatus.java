package com.example.captchasolver.model;

public enum SubscriptionStatus {
    NONE,    // Default status, no active subscription
    ACTIVE,  // User has an active subscription
    CANCELED, // User had a subscription but it's canceled
    EXPIRED  // Subscription period has ended (alternative to CANCELED or for specific logic)
    // You could also add PENDING_PAYMENT, TRIAL, etc. if needed later
}
