package edu.mirea.qwerdsa53.taskTracker.openapi;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

/**
 * SpringDoc serves Swagger UI under {@code /swagger-ui.html}, not at {@code /}. Enable with
 * {@code app.openapi.redirect-root-to-swagger=true}.
 */
@Controller
@ConditionalOnProperty(prefix = "app.openapi", name = "redirect-root-to-swagger", havingValue = "true")
public class SwaggerRootRedirectController {

	@GetMapping("/")
	public RedirectView redirectRootToSwaggerUi() {
		// SpringDoc 3 serves UI under /swagger-ui.html (may redirect to /swagger-ui/index.html)
		return new RedirectView("/swagger-ui/index.html", true);
	}
}
