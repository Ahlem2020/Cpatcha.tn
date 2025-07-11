package com.example.captchasolver.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "captcha_submissions")
public class CaptchaSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // For simplicity, storing image as byte array. For large images or many submissions,
    // storing paths to files on a file system or cloud storage is better.
    @Lob // Large Object
    @Column(name = "image_data", columnDefinition="BLOB") // Use BLOB for byte array
    private byte[] imageData;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaptchaStatus status;

    private String solution; // The solved text

    private LocalDateTime solvedAt;

    public enum CaptchaStatus {
        PENDING,
        SOLVING,
        SOLVED,
        FAILED
    }

    // Constructors
    public CaptchaSubmission() {
        this.submittedAt = LocalDateTime.now();
        this.status = CaptchaStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public CaptchaStatus getStatus() {
        return status;
    }

    public void setStatus(CaptchaStatus status) {
        this.status = status;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public LocalDateTime getSolvedAt() {
        return solvedAt;
    }

    public void setSolvedAt(LocalDateTime solvedAt) {
        this.solvedAt = solvedAt;
    }
}
