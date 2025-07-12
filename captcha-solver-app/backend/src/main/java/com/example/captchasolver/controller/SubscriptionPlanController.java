package com.example.captchasolver.controller;

import com.example.captchasolver.model.SubscriptionPlan;
import com.example.captchasolver.repository.SubscriptionPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/subscription-plans")
public class SubscriptionPlanController {

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @GetMapping
    public List<SubscriptionPlan> getAllSubscriptionPlans() {
        return subscriptionPlanRepository.findAll();
    }
}
