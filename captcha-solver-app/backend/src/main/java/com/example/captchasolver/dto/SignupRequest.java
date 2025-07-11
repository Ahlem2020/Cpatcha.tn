package com.example.captchasolver.dto;

// Add validation annotations later (e.g., @NotBlank, @Size, @Email)
public class SignupRequest {
    private String username;
    private String email;
    private String password;
    // Add roles if you want to allow role selection during signup, otherwise set default server-side
    // private Set<String> roles;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // public Set<String> getRoles() {
    //     return roles;
    // }

    // public void setRoles(Set<String> roles) {
    //     this.roles = roles;
    // }
}
