package az.azal.libraff_book_store.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import az.azal.libraff_book_store.filters.JwtAuthenticationFilter;
import az.azal.libraff_book_store.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final UserDetailsServiceImpl userDetailsService;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, UserDetailsServiceImpl userDetailsService) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.userDetailsService = userDetailsService;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
				.authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/apis/login").permitAll()
						.requestMatchers(HttpMethod.POST, "/apis/refresh-token").permitAll()
						.requestMatchers(HttpMethod.GET, "/books").permitAll().requestMatchers("/h2-console/**")
						.permitAll().requestMatchers("/", "/index.html", "/style.css", "/script.js").permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						.anyRequest().authenticated())
				.exceptionHandling(ex -> ex
						// Handles AuthenticationException - unauthenticated access (401)
						.authenticationEntryPoint((request, response, e) -> {
							log.warn("Unauthenticated access attempt - URI: {}, IP: {}", request.getRequestURI(),
									request.getRemoteAddr());
							response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							response.setContentType("application/json");
							response.setCharacterEncoding("UTF-8");
							response.getWriter()
									.write("""
											{"error": "UNAUTHORIZED", "message": "Authentication is required to access this resource."}
											""");
						})
						// Handles AccessDeniedException & AuthorizationDeniedException - forbidden
						// access (403)
						.accessDeniedHandler((request, response, e) -> {
							log.warn("Access denied - URI: {}, IP: {}, Reason: {}", request.getRequestURI(),
									request.getRemoteAddr(), e.getMessage());
							response.setStatus(HttpServletResponse.SC_FORBIDDEN);
							response.setContentType("application/json");
							response.setCharacterEncoding("UTF-8");
							response.getWriter()
									.write("""
											{"error": "ACCESS_DENIED", "message": "You do not have the required permissions to perform this action."}
											""");
						}))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

		authManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());

		return authManagerBuilder.build();
	}
}
