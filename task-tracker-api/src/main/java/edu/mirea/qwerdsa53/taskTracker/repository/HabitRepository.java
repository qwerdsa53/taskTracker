package edu.mirea.qwerdsa53.taskTracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.mirea.qwerdsa53.taskTracker.domain.habit.Habit;

public interface HabitRepository extends JpaRepository<Habit, Long> {

	@EntityGraph(attributePaths = "owner")
	@Query("select h from Habit h where h.owner.id = :ownerId")
	List<Habit> findByOwner_Id(@Param("ownerId") Long ownerId);

	@EntityGraph(attributePaths = "owner")
	@Query("select h from Habit h where h.id = :id and h.owner.id = :ownerId")
	Optional<Habit> findByIdAndOwner_Id(@Param("id") Long id, @Param("ownerId") Long ownerId);
}
