package az.azal.libraff_book_store.controller;

import az.azal.libraff_book_store.entity.EmployeeEntity;
import az.azal.libraff_book_store.enums.ErrorStatus;
import az.azal.libraff_book_store.exception.MyException;
import az.azal.libraff_book_store.repository.EmployeeRepository;
import az.azal.libraff_book_store.request.*;
import az.azal.libraff_book_store.service.EmailService;
import az.azal.libraff_book_store.service.RedisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import az.azal.libraff_book_store.response.AuthResponse;
import az.azal.libraff_book_store.service.UserDetailsServiceImpl;
import az.azal.libraff_book_store.util.JwtUtil;
import az.azal.libraff_book_store.util.RefreshTokenUtil;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "/apis")
@RequiredArgsConstructor
public class AuthController {

	private final AuthenticationManager authenticationManager;

	private final JwtUtil jwtUtil;

	private final UserDetailsServiceImpl userDetailsService;

	private final RefreshTokenUtil refreshTokenUtil;

	private final RedisService redisService;

	private final EmployeeRepository employeeRepository;

	private final EmailService emailService;

	private final PasswordEncoder passwordEncoder;

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
	public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRequest tokenRequest) {
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

	@PostMapping("/auth/forgot-password")
	public void sendOtpCodeWhenEmployeeExists(@Valid @RequestBody ResetPasswordRequest request) {


		System.out.println(request.getFin());
		System.out.println(request.getEmail());

		EmployeeEntity employee = employeeRepository
				.findByFINAndEmail(request.getFin(), request.getEmail())
				.orElseThrow(() -> new MyException("No matching employee found.", ErrorStatus.EMPLOYEE_NOT_FOUND));

		String employeeEmail = employee.getEmail();
		String otp = generateOtp();

		emailService.sendOtpCode(otp, employeeEmail);

		redisService.otpCachePut(request.getFin(), otp);

	}

	@PostMapping("auth/verify-code")
	public ResponseEntity<Map<String, String>> verifyOtpCodeSentByUser(@Valid @RequestBody VerifyOtpRequest request) {
		if(!employeeRepository.existsByFIN(request.getFin())){
			throw new MyException("Employee with FIN not found!", ErrorStatus.EMPLOYEE_NOT_FOUND);
		}
		String cachedOtp = redisService.getOtpCode(request.getFin());

		if (cachedOtp == null) {
			throw new MyException("OTP code expired or not found!", ErrorStatus.INVALID_OTP);
		}

		if (!cachedOtp.equals(request.getOtpCode())) {
			throw new MyException("Invalid OTP code!", ErrorStatus.INVALID_OTP);
		}

		redisService.deleteOtp(request.getFin());

		String resetToken  = UUID.randomUUID().toString();
		redisService.resetPasswordCachePut(resetToken, request.getFin());

		return ResponseEntity.ok(Map.of("resetToken", resetToken));
	}

	@PostMapping("auth/reset-password")
	public void resetPassword(@Valid @RequestBody ResetPasswordConfirmRequest request) {

		String resetToken = request.getResetToken();
		String newPassword = request.getNewPassword();

		if(!redisService.isResetPasswordVerified(resetToken)){
			throw new MyException("Invalid reset token!", ErrorStatus.INVALID_RESET_TOKEN);
		}

		String FIN = redisService.getResetPasswordCode(resetToken);

		EmployeeEntity employee = employeeRepository.findByFIN(FIN)
				.orElseThrow(() -> new MyException("Employee with FIN not found!", ErrorStatus.EMPLOYEE_NOT_FOUND));

		if (passwordEncoder.matches(newPassword, employee.getPassword())){
			throw new MyException("New password cannot be the same as the old password!", ErrorStatus.INVALID_OTP);
		}

		employee.setPassword(passwordEncoder.encode(newPassword));
		employeeRepository.save(employee);
		redisService.deleteResetPassword(request.getResetToken());
	}

	private String generateOtp() {
		SecureRandom secureRandom = new SecureRandom();
		// range: 100000–999999
		return String.valueOf(100000 + secureRandom.nextInt(900000));
	}

}