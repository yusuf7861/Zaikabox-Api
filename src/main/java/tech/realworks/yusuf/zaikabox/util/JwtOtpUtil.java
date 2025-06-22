package tech.realworks.yusuf.zaikabox.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtOtpUtil {
    @Value( "${jwt.otp.secret.key}")
    private String SECRET_KEY;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateOtpToken(String email, String otp, int expiryMinutes) {
        return Jwts.builder()
                .subject(email)
                .claim("otp", otp)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES)))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims validateTokenAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
