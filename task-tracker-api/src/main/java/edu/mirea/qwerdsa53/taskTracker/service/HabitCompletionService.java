package edu.mirea.qwerdsa53.taskTracker.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.mirea.qwerdsa53.taskTracker.api.dto.DtoMapper;
import edu.mirea.qwerdsa53.taskTracker.api.dto.HabitCompletionRequest;
import edu.mirea.qwerdsa53.taskTracker.api.dto.HabitCompletionResponse;
import edu.mirea.qwerdsa53.taskTracker.api.error.NotFoundException;
import edu.mirea.qwerdsa53.taskTracker.domain.completion.HabitCompletion;
import edu.mirea.qwerdsa53.taskTracker.domain.habit.Habit;
import edu.mirea.qwerdsa53.taskTracker.repository.HabitCompletionRepository;

@Service
@Transactional
public class HabitCompletionService {

	private final HabitCompletionRepository completionRepository;
	private final HabitService habitService;

	public HabitCompletionService(HabitCompletionRepository completionRepository, HabitService habitService) {
		this.completionRepository = completionRepository;
		this.habitService = habitService;
	}

	@Transactional(readOnly = true)
	public List<HabitCompletionResponse> findAll(Long userId, Long habitId) {
		habitService.getHabitForUser(userId, habitId);
		return completionRepository.findByHabit_Id(habitId).stream()
				.map(DtoMapper::toCompletionResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public HabitCompletionResponse getById(Long userId, Long habitId, Long completionId) {
		return DtoMapper.toCompletionResponse(getForUser(userId, habitId, completionId));
	}

	@Transactional(readOnly = true)
	public HabitCompletion getForUser(Long userId, Long habitId, Long completionId) {
		Habit habit = habitService.getHabitForUser(userId, habitId);
		return completionRepository
				.findByIdAndHabit_Id(completionId, habit.getId())
				.orElseThrow(() -> new NotFoundException("Completion not found"));
	}

	public HabitCompletionResponse create(Long userId, Long habitId, HabitCompletionRequest request) {
		Habit habit = habitService.getHabitForUser(userId, habitId);
		HabitCompletion c = new HabitCompletion();
		c.setHabit(habit);
		c.setCompletedOn(request.completedOn());
		c.setNote(request.note());
		c.setQuantity(request.quantity());
		completionRepository.save(c);
		return DtoMapper.toCompletionResponse(c);
	}

	public HabitCompletionResponse update(
			Long userId, Long habitId, Long completionId, HabitCompletionRequest request) {
		HabitCompletion c = getForUser(userId, habitId, completionId);
		c.setCompletedOn(request.completedOn());
		c.setNote(request.note());
		c.setQuantity(request.quantity());
		return DtoMapper.toCompletionResponse(c);
	}

	public void delete(Long userId, Long habitId, Long completionId) {
		HabitCompletion c = getForUser(userId, habitId, completionId);
		completionRepository.delete(c);
	}
}
