package edu.mirea.qwerdsa53.taskTracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.mirea.qwerdsa53.taskTracker.domain.completion.HabitCompletion;

public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Long> {

	List<HabitCompletion> findByHabit_Id(Long habitId);

	Optional<HabitCompletion> findByIdAndHabit_Id(Long id, Long habitId);
}
