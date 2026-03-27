package edu.mirea.qwerdsa53.taskTracker.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.mirea.qwerdsa53.taskTracker.api.dto.DtoMapper;
import edu.mirea.qwerdsa53.taskTracker.api.dto.HabitRequest;
import edu.mirea.qwerdsa53.taskTracker.api.dto.HabitResponse;
import edu.mirea.qwerdsa53.taskTracker.api.error.NotFoundException;
import edu.mirea.qwerdsa53.taskTracker.domain.habit.Habit;
import edu.mirea.qwerdsa53.taskTracker.domain.user.User;
import edu.mirea.qwerdsa53.taskTracker.repository.HabitRepository;

@Service
@Transactional
public class HabitService {

	private final HabitRepository habitRepository;
	private final UserService userService;

	public HabitService(HabitRepository habitRepository, UserService userService) {
		this.habitRepository = habitRepository;
		this.userService = userService;
	}

	@Transactional(readOnly = true)
	public List<HabitResponse> findAllForUser(Long userId) {
		userService.getEntityById(userId);
		return habitRepository.findByOwner_Id(userId).stream()
				.map(DtoMapper::toHabitResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public HabitResponse getByIdForUser(Long userId, Long habitId) {
		return DtoMapper.toHabitResponse(getHabitForUser(userId, habitId));
	}

	@Transactional(readOnly = true)
	public Habit getHabitForUser(Long userId, Long habitId) {
		return habitRepository
				.findByIdAndOwner_Id(habitId, userId)
				.orElseThrow(() -> new NotFoundException("Habit not found"));
	}

	public HabitResponse create(Long userId, HabitRequest request) {
		User owner = userService.getEntityById(userId);
		Habit h = new Habit();
		h.setOwner(owner);
		applyRequest(h, request);
		habitRepository.save(h);
		return DtoMapper.toHabitResponse(h);
	}

	public HabitResponse update(Long userId, Long habitId, HabitRequest request) {
		Habit h = getHabitForUser(userId, habitId);
		applyRequest(h, request);
		return DtoMapper.toHabitResponse(h);
	}

	public void delete(Long userId, Long habitId) {
		Habit h = getHabitForUser(userId, habitId);
		habitRepository.delete(h);
	}

	private void applyRequest(Habit h, HabitRequest request) {
		h.setTitle(request.title());
		h.setDescription(request.description());
		h.setColor(request.color());
		h.setIconKey(request.iconKey());
		h.setArchived(request.archived());
		h.setSchedule(DtoMapper.toFrequency(request.schedule()));
	}
}
