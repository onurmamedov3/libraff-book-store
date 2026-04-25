package az.azal.libraff_book_store.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class RefreshTokenUtil {

	@Value("${jwt.refresh-secret}")
	private String secret;

	private SecretKey REFRESH_SECRET_KEY;

	@PostConstruct
	private void init() {
		this.REFRESH_SECRET_KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser().verifyWith(REFRESH_SECRET_KEY) // ✅ replaces parser().setSigningKey(String)
				.build().parseSignedClaims(token) // ✅ replaces parseClaimsJws()
				.getPayload(); // ✅ replaces getBody()
	}

	private Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	public String generateRefreshToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		return createToken(claims, userDetails.getUsername());
	}

	private String createToken(Map<String, Object> claims, String subject) {
		return Jwts.builder().claims(claims) // ✅ replaces setClaims()
				.subject(subject) // ✅ replaces setSubject()
				.issuedAt(new Date(System.currentTimeMillis())) // ✅ replaces setIssuedAt()
				.expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7)) // ✅ replaces
																								// setExpiration()
				.signWith(REFRESH_SECRET_KEY) // ✅ replaces signWith(SignatureAlgorithm, String)
				.compact();
	}
}