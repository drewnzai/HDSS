package com.andrew.hdss.utils;

import com.andrew.hdss.auth.UserDetailsImpl;
import com.andrew.hdss.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.expiration.time}")
    private Long jwtExpirationInSeconds;

    @Value("${jwt.issuer}")
    private String issuer;

    private final KeyPair keyPair;

    public JwtUtil() {
        this.keyPair = loadKeyPair();
    }

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(keyPair.getPublic())
                    .requireIssuer(issuer)
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // includes expired, malformed, unsupported, bad signature
            return false;
        }
    }

    public String getUsernameFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(keyPair.getPublic())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }


    public String generateJwtToken(Authentication authentication) {

        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        return generateToken(principal.getUser());
    }

    private Date expirationDate() {
        return Date.from(Instant.now().plusSeconds(jwtExpirationInSeconds));
    }

    public String generateJwtTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate())
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();
    }

    private String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuer(issuer)
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(expirationDate())
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();
    }

    private KeyPair loadKeyPair() {
        try {
            PrivateKey privateKey = readPrivateKey();
            PublicKey publicKey = readPublicKey();
            return new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA key pair", e);
        }
    }

    private PrivateKey readPrivateKey() throws Exception {
        String key = readKey("private.pem");
        byte[] decoded = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey readPublicKey() throws Exception {
        String key = readKey("public.pem");
        byte[] decoded = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private String readKey(String filename) throws IOException {
        ClassPathResource resource = new ClassPathResource(filename);
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return content
                .replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public long getJwtExpiration() {
        return jwtExpirationInSeconds;
    }
}
