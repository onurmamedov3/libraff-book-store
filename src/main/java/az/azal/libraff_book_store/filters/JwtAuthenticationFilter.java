package az.azal.libraff_book_store.filters;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import az.azal.libraff_book_store.service.UserDetailsServiceImpl;
import az.azal.libraff_book_store.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;

	private final UserDetailsServiceImpl userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		final String authorizationHeader = request.getHeader("Authorization");

		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String jwt = authorizationHeader.substring(7);
		String username;

		try {
			username = jwtUtil.extractUsername(jwt);
		} catch (ExpiredJwtException e) {
			log.warn("Expired JWT token - URI: {}, IP: {}", request.getRequestURI(), request.getRemoteAddr());
			writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_EXPIRED",
					"JWT token has expired, please refresh your token.");
			return;
		} catch (MalformedJwtException | SignatureException e) {
			log.warn("Invalid JWT token - URI: {}, IP: {}", request.getRequestURI(), request.getRemoteAddr());
			writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_INVALID", "JWT token is invalid.");
			return;
		} catch (JwtException e) {
			log.warn("JWT error - URI: {}, IP: {}", request.getRequestURI(), request.getRemoteAddr());
			writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_ERROR", "JWT token error.");
			return;
		}

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);
			if (jwtUtil.validateToken(jwt, userDetails)) {
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
						null, userDetails.getAuthorities());
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}
		filterChain.doFilter(request, response);
	}

	private void writeErrorResponse(HttpServletResponse response, int status, String error, String message)
			throws IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(String.format("""
				{"error": "%s", "message": "%s"}
				""", error, message));
	}
}
