package com.gr6.SmartCart.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
// ham mã hóa tạo token
@Component
public class JwtTokenProvider {

    // Lấy giá trị từ file .env mà team đã cấu hình
    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${JWT_EXPIRATION}")
    private long jwtExpirationInMs;

    // 1. Hàm tạo Token (Hưởng sẽ gọi hàm này trong LoginService) [cite: 1740]
    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(email) // Lưu email vào token để nhận diện [cite: 1722]
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // 2. Hàm lấy Email từ Token (Dùng cho các module khác của Sáng, Huy, Toàn) [cite: 1725]
    public String getEmailFromJwt(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // 3. Hàm kiểm tra Token còn hạn và hợp lệ hay không [cite: 881]
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException ex) {
            System.err.println("Token không hợp lệ");
        } catch (ExpiredJwtException ex) {
            System.err.println("Token đã hết hạn");
        } catch (UnsupportedJwtException ex) {
            System.err.println("Token không được hỗ trợ");
        } catch (IllegalArgumentException ex) {
            System.err.println("Chuỗi Claims trống");
        }
        return false;
    }
}