package com.andrew.hdss.repositories;

import com.andrew.hdss.models.RefreshToken;
import com.andrew.hdss.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenAndUser(String token, User user);

    List<RefreshToken> findByUser(User user);
}
