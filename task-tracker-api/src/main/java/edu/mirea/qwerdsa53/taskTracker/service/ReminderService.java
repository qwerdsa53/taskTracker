package edu.mirea.qwerdsa53.taskTracker.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.mirea.qwerdsa53.taskTracker.api.dto.DtoMapper;
import edu.mirea.qwerdsa53.taskTracker.api.dto.ReminderRequest;
import edu.mirea.qwerdsa53.taskTracker.api.dto.ReminderResponse;
import edu.mirea.qwerdsa53.taskTracker.api.error.NotFoundException;
import edu.mirea.qwerdsa53.taskTracker.domain.habit.Habit;
import edu.mirea.qwerdsa53.taskTracker.domain.reminder.Reminder;
import edu.mirea.qwerdsa53.taskTracker.repository.ReminderRepository;

@Service
@Transactional
public class ReminderService {

	private final ReminderRepository reminderRepository;
	private final HabitService habitService;

	public ReminderService(ReminderRepository reminderRepository, HabitService habitService) {
		this.reminderRepository = reminderRepository;
		this.habitService = habitService;
	}

	@Transactional(readOnly = true)
	public List<ReminderResponse> findAll(Long userId, Long habitId) {
		habitService.getHabitForUser(userId, habitId);
		return reminderRepository.findByHabit_Id(habitId).stream()
				.map(DtoMapper::toReminderResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public ReminderResponse getById(Long userId, Long habitId, Long reminderId) {
		return DtoMapper.toReminderResponse(getForUser(userId, habitId, reminderId));
	}

	@Transactional(readOnly = true)
	public Reminder getForUser(Long userId, Long habitId, Long reminderId) {
		Habit habit = habitService.getHabitForUser(userId, habitId);
		return reminderRepository
				.findByIdAndHabit_Id(reminderId, habit.getId())
				.orElseThrow(() -> new NotFoundException("Reminder not found"));
	}

	public ReminderResponse create(Long userId, Long habitId, ReminderRequest request) {
		Habit habit = habitService.getHabitForUser(userId, habitId);
		Reminder r = DtoMapper.newReminder(request);
		r.setHabit(habit);
		reminderRepository.save(r);
		return DtoMapper.toReminderResponse(r);
	}

	public ReminderResponse update(Long userId, Long habitId, Long reminderId, ReminderRequest request) {
		Reminder r = getForUser(userId, habitId, reminderId);
		DtoMapper.applyReminder(r, request);
		return DtoMapper.toReminderResponse(r);
	}

	public void delete(Long userId, Long habitId, Long reminderId) {
		Reminder r = getForUser(userId, habitId, reminderId);
		reminderRepository.delete(r);
	}
}
