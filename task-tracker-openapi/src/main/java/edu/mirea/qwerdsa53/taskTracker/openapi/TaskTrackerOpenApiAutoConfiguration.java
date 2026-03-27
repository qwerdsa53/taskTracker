package edu.mirea.qwerdsa53.taskTracker.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springdoc.core.customizers.OpenApiCustomizer;

/**
 * Do not register a separate {@link OpenAPI} bean — SpringDoc already builds one; a second bean breaks
 * Swagger UI. Only customize via {@link OpenApiCustomizer}.
 */
@AutoConfiguration
@ConditionalOnClass(OpenAPI.class)
@EnableConfigurationProperties(OpenApiInfoProperties.class)
@ConditionalOnProperty(prefix = "app.openapi", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TaskTrackerOpenApiAutoConfiguration {

	@Bean
	public OpenApiCustomizer taskTrackerOpenApiCustomizer(OpenApiInfoProperties props) {
		return openApi -> {
			if (openApi.getInfo() == null) {
				openApi.setInfo(new Info());
			}
			openApi.getInfo()
					.title(props.getTitle())
					.description(props.getDescription())
					.version(props.getVersion());
			if (openApi.getOpenapi() == null || openApi.getOpenapi().isBlank()) {
				openApi.setOpenapi("3.0.1");
			}
			if (openApi.getComponents() == null) {
				openApi.setComponents(new Components());
			}
			openApi
					.getComponents()
					.addSecuritySchemes(
							"bearer-jwt",
							new SecurityScheme()
									.type(SecurityScheme.Type.HTTP)
									.scheme("bearer")
									.bearerFormat("JWT")
									.description(
											"Access token in header Authorization: Bearer … "
													+ "Получить: POST /api/v1/auth/login или POST /api/v1/auth/refresh. "
													+ "В Swagger замок только у операций, где нужна авторизация."));
		};
	}
}
