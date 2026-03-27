package edu.mirea.qwerdsa53.taskTracker.security;

import java.io.Serializable;

public record UserPrincipal(Long id, String email) implements Serializable {}
