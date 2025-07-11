package com.example.captchasolver.dto;

import com.example.captchasolver.model.CaptchaSubmission;

public class CaptchaSubmitResponseDto {
    private Long submissionId;
    private CaptchaSubmission.CaptchaStatus status;
    private String message;

    public CaptchaSubmitResponseDto(Long submissionId, CaptchaSubmission.CaptchaStatus status, String message) {
        this.submissionId = submissionId;
        this.status = status;
        this.message = message;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
