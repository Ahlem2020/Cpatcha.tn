package com.example.captchasolver.dto;

import com.example.captchasolver.model.CaptchaSubmission;

import java.time.LocalDateTime;

public class CaptchaSolutionResponseDto {
    private Long submissionId;
    private CaptchaSubmission.CaptchaStatus status;
    private String solution;
    private LocalDateTime submittedAt;
    private LocalDateTime solvedAt;
    private String originalFileName;


    public CaptchaSolutionResponseDto(Long submissionId, CaptchaSubmission.CaptchaStatus status, String solution, LocalDateTime submittedAt, LocalDateTime solvedAt, String originalFileName) {
        this.submissionId = submissionId;
        this.status = status;
        this.solution = solution;
        this.submittedAt = submittedAt;
        this.solvedAt = solvedAt;
        this.originalFileName = originalFileName;
    }

    // Static factory method for convenience
    public static CaptchaSolutionResponseDto fromEntity(CaptchaSubmission submission) {
        return new CaptchaSolutionResponseDto(
                submission.getId(),
                submission.getStatus(),
                submission.getSolution(),
                submission.getSubmittedAt(),
                submission.getSolvedAt(),
                submission.getOriginalFileName()
        );
    }


    // Getters and Setters
    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public CaptchaSubmission.CaptchaStatus getStatus() {
        return status;
    }

    public void setStatus(CaptchaSubmission.CaptchaStatus status) {
        this.status = status;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getSolvedAt() {
        return solvedAt;
    }

    public void setSolvedAt(LocalDateTime solvedAt) {
        this.solvedAt = solvedAt;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
}
