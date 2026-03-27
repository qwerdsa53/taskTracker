package edu.mirea.qwerdsa53.taskTracker.presence;

/** Broadcast over STOMP / topic and returned by REST snapshot. */
public record OnlineCountPayload(int onlineCount) {}
