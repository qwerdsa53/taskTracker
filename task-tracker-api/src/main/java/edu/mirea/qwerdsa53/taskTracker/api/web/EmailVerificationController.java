package edu.mirea.qwerdsa53.taskTracker.api.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.mirea.qwerdsa53.taskTracker.api.dto.UserResponse;
import edu.mirea.qwerdsa53.taskTracker.service.UserService;

@RestController
@RequestMapping("/api/v1/verification")
public class EmailVerificationController {

	private final UserService userService;

	public EmailVerificationController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/email")
	public UserResponse verifyEmail(@RequestParam("token") String token) {
		return userService.verifyEmail(token);
	}
}
