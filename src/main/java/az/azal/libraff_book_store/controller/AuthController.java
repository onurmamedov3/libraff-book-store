package az.azal.libraff_book_store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import az.azal.libraff_book_store.request.AuthRequest;
import az.azal.libraff_book_store.request.TokenRequest;
import az.azal.libraff_book_store.response.AuthResponse;
import az.azal.libraff_book_store.service.UserDetailsServiceImpl;
import az.azal.libraff_book_store.util.JwtUtil;
import az.azal.libraff_book_store.util.RefreshTokenUtil;

@RestController
@RequestMapping(path = "/apis")
public class AuthController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	@Autowired
	private RefreshTokenUtil refreshTokenUtil;

	@PostMapping("/login")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) throws Exception {
		try {
			// Tell the manager to authenticate using the FIN and password
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(authRequest.getFIN(), authRequest.getPassword()));
		} catch (BadCredentialsException e) {
			throw new Exception("Incorrect FIN or password", e);
		}

		// Load the user details using the FIN
		final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getFIN());

		// Generate the tokens (The FIN is now safely stored inside the token payload!)
		final String jwt = jwtUtil.generateToken(userDetails);
		final String refreshToken = refreshTokenUtil.generateRefreshToken(userDetails);

		return ResponseEntity.ok(new AuthResponse(jwt, refreshToken));
	}

	@PostMapping("/refresh-token")
	public ResponseEntity<?> refreshToken(@RequestBody TokenRequest tokenRequest) {
		String refreshToken = tokenRequest.getRefreshToken();

		String username = refreshTokenUtil.extractUsername(refreshToken);
		UserDetails userDetails = userDetailsService.loadUserByUsername(username);

		if (refreshTokenUtil.validateToken(refreshToken, userDetails)) {
			final String newAccessToken = jwtUtil.generateToken(userDetails);
			return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken)); // Keep the same refresh token
		} else {
			return ResponseEntity.status(403).body("Invalid refresh token");
		}
	}

}
