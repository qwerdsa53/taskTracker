package edu.mirea.qwerdsa53.taskTracker.api.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import edu.mirea.qwerdsa53.taskTracker.presence.OnlineCountPayload;
import edu.mirea.qwerdsa53.taskTracker.presence.OnlinePresenceService;

// REST snapshot of the online count. Live updates come via STOMP topic /topic/presence.
@RestController
@RequestMapping("/api/v1/presence")
@SecurityRequirement(name = "bearer-jwt")
public class PresenceController {

	private final OnlinePresenceService onlinePresenceService;

	public PresenceController(OnlinePresenceService onlinePresenceService) {
		this.onlinePresenceService = onlinePresenceService;
	}

	@GetMapping("/online-count")
	public OnlineCountPayload getOnlineCount() {
		return new OnlineCountPayload(onlinePresenceService.getOnlineCount());
	}
}
