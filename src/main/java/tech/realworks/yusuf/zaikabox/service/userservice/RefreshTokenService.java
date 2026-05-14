package tech.realworks.yusuf.zaikabox.service.userservice;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.realworks.yusuf.zaikabox.entity.RefreshTokenEntity;
import tech.realworks.yusuf.zaikabox.repository.RefreshTokenRepository;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh.expiration.days:7}")
    private long refreshTokenExpirationDays;

    public RefreshTokenEntity issueToken(String userId, String email) {
        refreshTokenRepository.deleteByUserId(userId);
        return refreshTokenRepository.save(
                RefreshTokenEntity.builder()
                        .token(UUID.randomUUID().toString())
                        .userId(userId)
                        .email(email)
                        .createdAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays))
                        .revoked(false)
                        .build()
        );
    }

    public RefreshTokenEntity rotateToken(String existingToken) {
        RefreshTokenEntity existing = validate(existingToken);
        refreshTokenRepository.deleteByToken(existingToken);
        return issueToken(existing.getUserId(), existing.getEmail());
    }

    public RefreshTokenEntity validate(String token) {
        RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new NoSuchElementException("Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Refresh token expired or revoked");
        }
        return refreshToken;
    }

    public void revoke(String token) {
        if (token != null && !token.isBlank()) {
            refreshTokenRepository.deleteByToken(token);
        }
    }
}
