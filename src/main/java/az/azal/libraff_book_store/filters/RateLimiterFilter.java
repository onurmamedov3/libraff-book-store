package az.azal.libraff_book_store.filters;


import az.azal.libraff_book_store.util.JwtUtil;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimiterFilter extends OncePerRequestFilter {


    private final JwtUtil jwtUtil;


    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    @Value("${app.rateLimitPerMinute}")
    private int limitPerMinute;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        boolean allowed;
        if (header != null && header.startsWith("Bearer ")) {
            String FIN = jwtUtil.extractUsername(header.substring(7));
            allowed = applyFINRateLimit(FIN);
        } else {
            allowed = applyIpRateLimit(request.getRemoteAddr());
        }
        if (!allowed) {
            response.sendError(429, "Too many requests! Please try again later.");
            return;
        }

filterChain.doFilter(request, response);
    }


    private boolean applyFINRateLimit(String FIN) {
       if (FIN == null || FIN.isBlank()) {
            return false; // Invalid FIN, block the request
       }
       Bucket bucket = this.buckets.computeIfAbsent(FIN, k -> createBucket());
       return bucket.tryConsume(1); // Try to consume 1 token, return false if rate limit exceeded
    }

    private boolean applyIpRateLimit(String ip) {
        Bucket bucket = this.buckets.computeIfAbsent(ip, k -> createBucket());
        return bucket.tryConsume(1); // Return true if allowed, false if rate limit exceeded
    }

    private Bucket createBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(limitPerMinute)
                        .refillGreedy(limitPerMinute, Duration.ofMinutes(1))) //her saniyede 1.67 sorgu elave olunur
                .build();
    }
}
