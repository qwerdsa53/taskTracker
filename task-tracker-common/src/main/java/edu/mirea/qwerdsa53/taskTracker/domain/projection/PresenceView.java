package edu.mirea.qwerdsa53.taskTracker.domain.projection;

import java.time.Instant;

public record PresenceView(Long userId, Instant lastSeenAt, boolean online) {}
