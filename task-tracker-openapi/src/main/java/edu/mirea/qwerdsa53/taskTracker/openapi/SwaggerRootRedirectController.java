package edu.mirea.qwerdsa53.taskTracker.openapi;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

// Redirects GET / to Swagger UI. Enabled via app.openapi.redirect-root-to-swagger=true.
@Controller
@ConditionalOnProperty(prefix = "app.openapi", name = "redirect-root-to-swagger", havingValue = "true")
public class SwaggerRootRedirectController {

	@GetMapping("/")
	public RedirectView redirectRootToSwaggerUi() {
		return new RedirectView("/swagger-ui/index.html", true);
	}
}
