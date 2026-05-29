package edu.mirea.qwerdsa53.taskTracker.admin;

import java.util.List;
import java.util.Optional;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import edu.mirea.qwerdsa53.taskTracker.domain.user.User;
import edu.mirea.qwerdsa53.taskTracker.repository.UserRepository;

/**
 * One-off admin tasks dispatched via `--task=<name>` argument.
 *
 * Why a single dispatcher class instead of separate Spring Boot apps:
 *  - exact same image / classpath / config as the server, so admin tasks see the
 *    same DB URL, secrets, and Hibernate mappings → no drift;
 *  - tasks reuse existing Spring beans (Flyway, UserRepository, PasswordEncoder);
 *  - launched via the `app` wrapper which sets `spring.main.web-application-type=none`
 *    so no port is opened and the JVM exits cleanly when the task is done.
 */
@Component
public class AdminTaskRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(AdminTaskRunner.class);
	private static final String TASK = "task";

	private final ConfigurableApplicationContext ctx;
	private final ObjectProvider<Flyway> flyway;
	private final ObjectProvider<UserRepository> userRepository;
	private final ObjectProvider<PasswordEncoder> passwordEncoder;
	private final ObjectProvider<StringRedisTemplate> redis;

	public AdminTaskRunner(
			ConfigurableApplicationContext ctx,
			ObjectProvider<Flyway> flyway,
			ObjectProvider<UserRepository> userRepository,
			ObjectProvider<PasswordEncoder> passwordEncoder,
			ObjectProvider<StringRedisTemplate> redis) {
		this.ctx = ctx;
		this.flyway = flyway;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.redis = redis;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!args.containsOption(TASK)) {
			return; // normal server mode — let the web app come up
		}
		String task = args.getOptionValues(TASK).get(0);
		int code;
		try {
			code = switch (task) {
				case "migrate" -> migrate();
				case "create-admin" -> createAdmin(args);
				case "cache-clear" -> cacheClear();
				default -> {
					log.error("unknown --task={} (valid: migrate, create-admin, cache-clear)", task);
					yield 2;
				}
			};
		} catch (RuntimeException e) {
			log.error("task '{}' failed: {}", task, e.getMessage(), e);
			code = 1;
		}
		final int exitCode = code;
		System.exit(SpringApplication.exit(ctx, () -> exitCode));
	}

	private int migrate() {
		Flyway f = flyway.getIfAvailable();
		if (f == null) {
			log.error("Flyway bean missing — run with -Dspring.flyway.enabled=true");
			return 1;
		}
		MigrateResult r = f.migrate();
		log.info(
				"migrate ok: applied={} target={} initialSchemaVersion={}",
				r.migrationsExecuted,
				r.targetSchemaVersion,
				r.initialSchemaVersion);
		return 0;
	}

	private int createAdmin(ApplicationArguments args) {
		String email = required(args, "email");
		String password = required(args, "password");
		String username = first(args, "username").orElse(email);
		String timezone = first(args, "timezone").orElse("UTC");

		UserRepository repo = userRepository.getObject();
		if (repo.existsByEmail(email)) {
			log.error("create-admin: email '{}' already exists", email);
			return 1;
		}
		User u = new User();
		u.setEmail(email);
		u.setUsername(username);
		u.setTimezone(timezone);
		u.setPasswordHash(passwordEncoder.getObject().encode(password));
		u.setEmailVerified(true);
		repo.save(u);
		log.info("create-admin ok: id={} email={}", u.getId(), email);
		return 0;
	}

	private int cacheClear() {
		StringRedisTemplate t = redis.getIfAvailable();
		if (t == null) {
			log.error("Redis bean missing — task requires Redis to be configured");
			return 1;
		}
		t.getRequiredConnectionFactory().getConnection().serverCommands().flushDb();
		log.info("cache-clear ok: redis flushdb done");
		return 0;
	}

	private static String required(ApplicationArguments args, String name) {
		return first(args, name)
				.orElseThrow(() -> new IllegalArgumentException("missing required --" + name));
	}

	private static Optional<String> first(ApplicationArguments args, String name) {
		List<String> values = args.getOptionValues(name);
		return (values == null || values.isEmpty()) ? Optional.empty() : Optional.of(values.get(0));
	}
}
