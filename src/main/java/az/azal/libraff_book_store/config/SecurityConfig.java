package az.azal.libraff_book_store.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import az.azal.libraff_book_store.filters.JwtAuthenticationFilter;
import az.azal.libraff_book_store.service.UserDetailsServiceImpl;

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
		http.csrf().disable()
				.authorizeHttpRequests(authorizeRequests -> authorizeRequests.requestMatchers(HttpMethod.OPTIONS, "/**")
						.permitAll().requestMatchers(HttpMethod.POST, "/apis/login").permitAll()
						.requestMatchers("/h2-console/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/apis/refresh-token").permitAll()
						.requestMatchers(HttpMethod.GET, "/books").permitAll()
						.requestMatchers("/", "/index.html", "/style.css", "/script.js").permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						.anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// Add JWT filter before the default username/password filter
		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		http.headers().frameOptions().disable();
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		return http.getSharedObject(AuthenticationManagerBuilder.class).userDetailsService(userDetailsService)
				.passwordEncoder(passwordEncoder()).and().build();
	}
}
