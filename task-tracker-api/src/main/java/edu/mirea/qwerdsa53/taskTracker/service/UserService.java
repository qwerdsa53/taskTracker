package edu.mirea.qwerdsa53.taskTracker.service;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.mirea.qwerdsa53.taskTracker.api.dto.DtoMapper;
import edu.mirea.qwerdsa53.taskTracker.api.dto.UserRequest;
import edu.mirea.qwerdsa53.taskTracker.api.dto.UserResponse;
import edu.mirea.qwerdsa53.taskTracker.api.error.ConflictException;
import edu.mirea.qwerdsa53.taskTracker.api.error.NotFoundException;
import edu.mirea.qwerdsa53.taskTracker.config.AppMailProperties;
import edu.mirea.qwerdsa53.taskTracker.domain.user.User;
import edu.mirea.qwerdsa53.taskTracker.mail.MailNotificationService;
import edu.mirea.qwerdsa53.taskTracker.repository.UserRepository;
import edu.mirea.qwerdsa53.taskTracker.verification.EmailVerificationTokenStore;

@Service
@Transactional
public class UserService {

	private static final SecureRandom RANDOM = new SecureRandom();

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final MailNotificationService mailNotificationService;
	private final AppMailProperties mailProperties;
	private final EmailVerificationTokenStore emailVerificationTokenStore;

	public UserService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			MailNotificationService mailNotificationService,
			AppMailProperties mailProperties,
			EmailVerificationTokenStore emailVerificationTokenStore) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.mailNotificationService = mailNotificationService;
		this.mailProperties = mailProperties;
		this.emailVerificationTokenStore = emailVerificationTokenStore;
	}

	@Transactional(readOnly = true)
	public List<UserResponse> findAll() {
		return userRepository.findAll().stream().map(DtoMapper::toUserResponse).toList();
	}

	@Transactional(readOnly = true)
	public UserResponse getById(Long id) {
		return DtoMapper.toUserResponse(getEntityById(id));
	}

	@Transactional(readOnly = true)
	public User getEntityById(Long id) {
		return userRepository
				.findById(id)
				.orElseThrow(() -> new NotFoundException("User not found"));
	}

	public UserResponse verifyEmail(String token) {
		long userId =
				emailVerificationTokenStore
						.consume(token)
						.orElseThrow(() -> new NotFoundException("Invalid or expired verification link"));
		User u = getEntityById(userId);
		if (u.isEmailVerified()) {
			throw new ConflictException("Email already verified");
		}
		u.setEmailVerified(true);
		return DtoMapper.toUserResponse(u);
	}

	public UserResponse create(UserRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new ConflictException("Email already in use");
		}
		User u = new User();
		u.setEmail(request.email());
		u.setPasswordHash(passwordEncoder.encode(request.password()));
		u.setUsername(request.username());
		u.setTimezone(request.timezone());
		u.setEmailVerified(false);
		userRepository.save(u);
		String verifyToken = newEmailVerificationToken(u.getId());
		mailNotificationService.sendEmailVerification(u.getEmail(), verificationUrl(verifyToken));
		return DtoMapper.toUserResponse(u);
	}

	public UserResponse update(Long id, UserRequest request) {
		User u = getEntityById(id);
		boolean emailTakenByOther =
				!u.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email());
		if (emailTakenByOther) {
			throw new ConflictException("Email already in use");
		}
		boolean emailChanged = !u.getEmail().equals(request.email());
		u.setEmail(request.email());
		u.setPasswordHash(passwordEncoder.encode(request.password()));
		u.setUsername(request.username());
		u.setTimezone(request.timezone());
		if (emailChanged) {
			u.setEmailVerified(false);
		}
		userRepository.save(u);
		if (emailChanged) {
			String verifyToken = newEmailVerificationToken(u.getId());
			mailNotificationService.sendEmailVerification(u.getEmail(), verificationUrl(verifyToken));
		}
		return DtoMapper.toUserResponse(u);
	}

	public void delete(Long id) {
		if (!userRepository.existsById(id)) {
			throw new NotFoundException("User not found");
		}
		userRepository.deleteById(id);
	}

	private String newEmailVerificationToken(long userId) {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		String token = HexFormat.of().formatHex(bytes);
		emailVerificationTokenStore.store(token, userId, mailProperties.getVerificationTokenTtl());
		return token;
	}

	private String verificationUrl(String token) {
		String base = mailProperties.getVerificationBaseUrl().replaceAll("/+$", "");
		return base + "/api/v1/verification/email?token=" + token;
	}
}
