package edu.mirea.qwerdsa53.taskTracker.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
		@NotBlank @Email String email,
		@NotBlank @Size(min = 8, max = 128) String password,
		@NotBlank @Size(max = 128) String username,
		@NotBlank @Size(max = 64) String timezone) {
}
