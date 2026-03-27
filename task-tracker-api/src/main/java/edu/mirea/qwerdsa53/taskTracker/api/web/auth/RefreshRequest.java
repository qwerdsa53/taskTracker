package edu.mirea.qwerdsa53.taskTracker.api.web.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank String refreshToken) {}
