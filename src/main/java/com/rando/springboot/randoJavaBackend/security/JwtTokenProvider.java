package com.rando.springboot.randoJavaBackend.security;

import com.rando.springboot.randoJavaBackend.controller.UserController;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")   // 從application.properties或application.yml中取得秘密金鑰
    private String jwtSecret;

    @Value("${jwt.expiration}")   // 從application.properties或application.yml中取得憑證有效期
    private long jwtExpiration;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    // 創建一個新的JWT憑證
    public String createToken(String userphone) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(userphone)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    // 從請求中取得JWT憑證
    public String resolveToken(HttpServletRequest req) {
        log.info("resolveToken " );
        String bearerToken = req.getHeader("Authorization");
        log.info("bearerToken: " + bearerToken);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 驗證JWT憑證
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 從JWT憑證中取得使用者名稱
    public String getUserphone(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (JwtException e) {
            log.info(e.toString()); // 输出异常信息
            return null;
        }

    }
}
