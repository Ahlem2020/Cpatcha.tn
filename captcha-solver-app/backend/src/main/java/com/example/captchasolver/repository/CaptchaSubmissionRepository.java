package com.example.captchasolver.repository;

import com.example.captchasolver.model.CaptchaSubmission;
import com.example.captchasolver.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaptchaSubmissionRepository extends JpaRepository<CaptchaSubmission, Long> {

    Optional<CaptchaSubmission> findByIdAndUser(Long id, User user);

    List<CaptchaSubmission> findByUserOrderBySubmittedAtDesc(User user);

    // You might add methods for querying by status, etc.
    // List<CaptchaSubmission> findByStatus(CaptchaSubmission.CaptchaStatus status);
}
