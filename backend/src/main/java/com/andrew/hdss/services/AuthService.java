package com.andrew.hdss.services;

import com.andrew.hdss.auth.UserDetailsImpl;
import com.andrew.hdss.dtos.LoginRequest;
import com.andrew.hdss.dtos.LoginResponse;
import com.andrew.hdss.dtos.RefreshTokenRequest;
import com.andrew.hdss.exceptions.ExpiredTokenException;
import com.andrew.hdss.exceptions.UserNotVerifiedException;
import com.andrew.hdss.models.RefreshToken;
import com.andrew.hdss.models.User;
import com.andrew.hdss.models.VerificationToken;
import com.andrew.hdss.repositories.RefreshTokenRepository;
import com.andrew.hdss.repositories.UserRepository;
import com.andrew.hdss.repositories.VerificationTokenRepository;
import com.andrew.hdss.utils.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    private void fetchUserAndEnable(VerificationToken verificationToken)  {
        String username = verificationToken.getUser().getUsername();
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new EntityNotFoundException("Could not find user")
        );
        Instant current = Instant.now();

        if(verificationToken.getExpiryDate().isAfter(current)){
            user.setEnabled(true);

            verificationTokenRepository.delete(verificationToken);
            userRepository.save(user);
        }else{
            verificationTokenRepository.delete(verificationToken);
            throw new ExpiredTokenException("Token has expired");
        }
    }

    public void verifyAccount(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(
                        () -> new EntityNotFoundException("Could not find verification token")
                );
        fetchUserAndEnable(verificationToken);
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        UserDetailsImpl principal = (UserDetailsImpl) SecurityContextHolder.
                getContext().getAuthentication().getPrincipal();

        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(
                        () -> new EntityNotFoundException("Could not find user")
                );
    }

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow(
                () -> new UsernameNotFoundException("Could not find user")
        );

        if(!user.isEnabled()){
            throw new UserNotVerifiedException("User is not verified. Please check email and verify");
        }

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpirationDate(Instant.now().plusSeconds(2592000));
        refreshToken.setUser(user);
        refreshTokenRepository.save(refreshToken);

        return build(jwtUtil.generateJwtToken(authentication)
                , refreshToken.getToken(), user);
    }

    public LoginResponse refresh(RefreshTokenRequest refreshTokenRequest) {

        User user = userRepository.findByUsername(refreshTokenRequest.getUsername()).orElseThrow(
                () -> new EntityNotFoundException("Could not find user")
        );
        RefreshToken refreshToken = refreshTokenRepository.
                findByTokenAndUser(refreshTokenRequest.getToken(), user)
                .orElseThrow(
                        () -> new EntityNotFoundException("Could not find refresh token")
                );

        boolean isNotExpired = Instant.now().isBefore(refreshToken.getExpirationDate());

        if(isNotExpired){
            return build(jwtUtil.generateJwtTokenFromUsername(refreshTokenRequest.getUsername())
                    , refreshTokenRequest.getToken(), user);
        }
        else {
            refreshTokenRepository.delete(refreshToken);

            throw new ExpiredTokenException("Refresh Token has expired");
        }
    }

    private LoginResponse build(String token, String refreshToken, User user){
        return LoginResponse.builder()
                .authenticationToken(token)
                .firstName(user.getFirstName())
                .role(user.getRole().name())
                .refreshToken(refreshToken)
                .expiresAt(Instant.now().plusSeconds(jwtUtil.getJwtExpiration()))
                .build();
    }

    public boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
    }

}
