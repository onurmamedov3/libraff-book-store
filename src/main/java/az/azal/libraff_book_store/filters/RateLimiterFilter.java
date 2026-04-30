package az.azal.libraff_book_store.filters;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import az.azal.libraff_book_store.util.JwtUtil;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimiterFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;

	private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

	@Value("${app.rateLimitPerMinute}")
	private int limitPerMinute;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		if (request.getRequestURI().startsWith("/h2-console")) {
			filterChain.doFilter(request, response);
			return;
		}

		String header = request.getHeader("Authorization");

		boolean allowed = false;

		if (header != null && header.startsWith("Bearer ")) {

			String FIN = jwtUtil.extractUsername(header.substring(7));

			allowed = applyFINRateLimit(FIN);

		}

		else {
			allowed = applyIpRateLimit(request.getRemoteAddr());
		}

		if (!allowed) {
			response.setStatus(429); // 429
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write("""
					{"error": "TOO_MANY_REQUESTS", "message": "Too many requests! Please try again later."}
					""");

			log.warn("Rate limit exceeded - URI: {}, Method: {}, IP: {}, User: {}", request.getRequestURI(),
					request.getMethod(), request.getRemoteAddr(),
					header != null ? jwtUtil.extractUsername(header.substring(7)) : "anonymous");
			return;
		}

		filterChain.doFilter(request, response);
	}

	private boolean applyFINRateLimit(String FIN) {
		if (FIN == null || FIN.isBlank()) {
			return false; // Invalid FIN, block the request
		}
		Bucket bucket = buckets.computeIfAbsent(FIN, k -> createBucket());
		return bucket.tryConsume(1); // Try to consume 1 token, return false if rate limit exceeded
	}

	private boolean applyIpRateLimit(String ip) {

		Bucket bucket = buckets.computeIfAbsent(ip, k -> createBucket());

		return bucket.tryConsume(1); // Return true if allowed, false if rate limit exceeded

	}

	private Bucket createBucket() {
		return Bucket.builder()
				.addLimit(limit -> limit.capacity(limitPerMinute).refillGreedy(limitPerMinute, Duration.ofMinutes(1)))
				.build();
	}
}
