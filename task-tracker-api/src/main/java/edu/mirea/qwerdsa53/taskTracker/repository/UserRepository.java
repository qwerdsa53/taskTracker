package edu.mirea.qwerdsa53.taskTracker.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.mirea.qwerdsa53.taskTracker.domain.user.User;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByEmail(String email);

	Optional<User> findByEmail(String email);
}
