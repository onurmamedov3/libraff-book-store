package az.azal.libraff_book_store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate redisTemplate;

    public void otpCachePut(String fin, String otp) {
        redisTemplate.opsForValue().set(fin, otp, 5, TimeUnit.MINUTES);
    }

    public void resetPasswordCachePut(String resetToken, String fin) {
        redisTemplate.opsForValue().set(resetToken, fin, 10, TimeUnit.MINUTES);
    }

    public void deleteOtp(String fin) {
        redisTemplate.delete(fin);
    }

    public void deleteResetPassword(String resetToken) {
        redisTemplate.delete(resetToken);
    }

    public Boolean isResetPasswordVerified(String resetToken) {
        return redisTemplate.hasKey(resetToken);
    }

    public String getResetPasswordCode(String resetToken) {
        if (isResetPasswordVerified(resetToken)) {
            return redisTemplate.opsForValue().get(resetToken);
        }
        return null;
    }

    public Boolean isOtpVerified(String fin) {
        return redisTemplate.hasKey(fin);
    }

    public String getOtpCode(String fin) {
        if (isOtpVerified(fin)) {
            return redisTemplate.opsForValue().get(fin);
        }
        return null;
    }
}