package edu.mirea.qwerdsa53.taskTracker.api.web.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
