package edu.mirea.qwerdsa53.taskTracker.presence;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Counts STOMP sessions (each WebSocket session id after CONNECT).
 */
@Component
public class StompPresenceEventListener {

	private final OnlinePresenceService presenceService;

	public StompPresenceEventListener(OnlinePresenceService presenceService) {
		this.presenceService = presenceService;
	}

	@EventListener
	public void onConnect(SessionConnectEvent event) {
		String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
		presenceService.onStompSessionConnected(sessionId);
	}

	@EventListener
	public void onDisconnect(SessionDisconnectEvent event) {
		String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
		presenceService.onStompSessionDisconnected(sessionId);
	}
}
