package tech.realworks.yusuf.zaikabox.service.userservice;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.realworks.yusuf.zaikabox.entity.AdminPasswordResetToken;
import tech.realworks.yusuf.zaikabox.entity.Role;
import tech.realworks.yusuf.zaikabox.entity.UserEntity;
import tech.realworks.yusuf.zaikabox.repository.AdminPasswordResetTokenRepository;
import tech.realworks.yusuf.zaikabox.repository.userRepo.UserRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AdminPasswordResetService {
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final AdminPasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    public void sendOtp(String email) {
        UserEntity admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));
        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Password reset is available for admin accounts only");
        }

        String otp = generateOtp();
        Instant expiresAt = Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES);

        tokenRepository.deleteByEmail(email);
        AdminPasswordResetToken token = AdminPasswordResetToken.builder()
                .email(email)
                .hashedOtp(passwordEncoder.encode(otp))
                .expiresAt(expiresAt)
                .consumed(false)
                .attempts(0)
                .createdAt(Instant.now())
                .build();
        tokenRepository.save(token);

        sendEmail(email, otp, OTP_EXPIRY_MINUTES);
    }

    public void verifyOtp(String email, String otp) {
        AdminPasswordResetToken token = requireActiveToken(email);
        ensureAttemptLimit(token);
        if (!passwordEncoder.matches(otp, token.getHashedOtp())) {
            recordFailedAttempt(token);
            throw new IllegalArgumentException("Invalid OTP");
        }
    }

    public void resetPassword(String email, String otp, String newPassword) {
        AdminPasswordResetToken token = requireActiveToken(email);
        ensureAttemptLimit(token);
        if (!passwordEncoder.matches(otp, token.getHashedOtp())) {
            recordFailedAttempt(token);
            throw new IllegalArgumentException("Invalid OTP");
        }

        UserEntity admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));
        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Password reset is available for admin accounts only");
        }

        admin.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(admin);

        token.setConsumed(true);
        tokenRepository.save(token);
        tokenRepository.deleteByEmail(email);
    }

    private AdminPasswordResetToken requireActiveToken(String email) {
        AdminPasswordResetToken token = tokenRepository.findFirstByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("OTP not found for the provided email"));

        if (token.isConsumed()) {
            throw new IllegalArgumentException("OTP already used");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            tokenRepository.deleteByEmail(email);
            throw new IllegalArgumentException("OTP has expired");
        }
        return token;
    }

    private void ensureAttemptLimit(AdminPasswordResetToken token) {
        if (token.getAttempts() >= MAX_ATTEMPTS) {
            tokenRepository.deleteByEmail(token.getEmail());
            throw new IllegalArgumentException("OTP locked due to too many attempts");
        }
    }

    private void recordFailedAttempt(AdminPasswordResetToken token) {
        token.setAttempts(token.getAttempts() + 1);
        tokenRepository.save(token);
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private void sendEmail(String email, String otp, int validityMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("yjamal12feb@gmail.com");
        message.setTo(email);
        message.setSubject("Admin Password Reset OTP - Zaikabox");
        message.setText("Your admin password reset OTP is " + otp + ". It is valid for " + validityMinutes + " minutes.");
        mailSender.send(message);
    }
}
