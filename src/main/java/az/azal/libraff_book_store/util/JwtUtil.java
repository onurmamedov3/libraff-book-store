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
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;

	private SecretKey SECRET_KEY; // declared but not initialized here

	@PostConstruct // Spring guarantees @Value is done before this runs
	private void init() {
		this.SECRET_KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		return createToken(claims, userDetails.getUsername());
	}

	private String createToken(Map<String, Object> claims, String subject) {
		return Jwts.builder().claims(claims) // ✅ replaces setClaims()
				.subject(subject) // ✅ replaces setSubject()
				.issuedAt(new Date(System.currentTimeMillis())) // ✅ replaces setIssuedAt()
				.expiration(new Date(System.currentTimeMillis() + 1000 * 900)) // ✅ replaces setExpiration()
				.signWith(SECRET_KEY) // ✅ replaces signWith(Algorithm, String)
				.compact();
	}

	public boolean validateToken(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
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
		return Jwts.parser().verifyWith(SECRET_KEY) // ✅ replaces parserBuilder().setSigningKey()
				.build().parseSignedClaims(token) // ✅ replaces parseClaimsJws()
				.getPayload(); // ✅ replaces getBody()
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}
}