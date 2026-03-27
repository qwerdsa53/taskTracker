package edu.mirea.qwerdsa53.taskTracker.presence;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class OnlinePresenceService {

	public static final String PRESENCE_TOPIC = "/topic/presence";

	private final Set<String> stompSessions = ConcurrentHashMap.newKeySet();
	private final SimpMessagingTemplate messagingTemplate;

	public OnlinePresenceService(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	public int getOnlineCount() {
		return stompSessions.size();
	}

	public void onStompSessionConnected(String sessionId) {
		if (sessionId == null) {
			return;
		}
		if (stompSessions.add(sessionId)) {
			broadcast(stompSessions.size());
		}
	}

	public void onStompSessionDisconnected(String sessionId) {
		if (sessionId == null) {
			return;
		}
		if (stompSessions.remove(sessionId)) {
			broadcast(stompSessions.size());
		}
	}

	private void broadcast(int count) {
		messagingTemplate.convertAndSend(PRESENCE_TOPIC, new OnlineCountPayload(count));
	}
}
