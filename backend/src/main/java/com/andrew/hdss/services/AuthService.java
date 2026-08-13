package com.andrew.hdss.services;

import com.andrew.hdss.auth.UserDetailsImpl;
import com.andrew.hdss.dtos.LoginRequest;
import com.andrew.hdss.dtos.LoginResponse;
import com.andrew.hdss.dtos.RefreshTokenRequest;
import com.andrew.hdss.dtos.RegisterRequest;
import com.andrew.hdss.exceptions.ExpiredTokenException;
import com.andrew.hdss.exceptions.ResourceAlreadyExistsException;
import com.andrew.hdss.exceptions.UserNotVerifiedException;
import com.andrew.hdss.models.RefreshToken;
import com.andrew.hdss.models.User;
import com.andrew.hdss.models.VerificationToken;
import com.andrew.hdss.models.enums.UserRole;
import com.andrew.hdss.repositories.RefreshTokenRepository;
import com.andrew.hdss.repositories.UserRepository;
import com.andrew.hdss.repositories.VerificationTokenRepository;
import com.andrew.hdss.utils.JwtUtil;
import com.andrew.hdss.utils.NotificationEmail;
import com.andrew.hdss.utils.Util;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public void register(RegisterRequest registerRequest) throws Exception {
        if(userRepository.existsByEmail(registerRequest.getEmail())){
            throw new ResourceAlreadyExistsException("Email is already in use");
        }
        else{

            String username = String.valueOf(Character.toUpperCase(registerRequest.getFirstName().charAt(0))) +
                    Character.toUpperCase(registerRequest.getLastName().charAt(0)) +
                    Util.generateRandomString(4);

            UserRole userRole = UserRole.valueOf(registerRequest.getRole());

            User user = new User();
            user.setUsername(username);
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setRole(userRole);
            user.setEnabled(false);

            String token = generateVerificationToken(user);

            NotificationEmail email = NotificationEmail.builder()
                            .subject("Account Verification")
                            .recipient(user.getEmail())
                            .title("HDSS Account Verification")
                            .body("Thank you for signing up to HDSS, " + registerRequest.getFirstName() +
                                    " please click on the below url to activate your account: " +
                                    "http://localhost:8080/api/auth/accountVerification/" + token)
                            .build();

            mailService.sendMail(email);

            userRepository.save(user);
        }
    }

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
            throw new ExpiredTokenException("Token has expired");
        }
    }

    private String generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setExpiryDate(Instant.now().plusSeconds(864000L));
        verificationToken.setUser(user);

        verificationTokenRepository.save(verificationToken);
        return token;
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
                , refreshToken.getToken());
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
                    , refreshTokenRequest.getToken());
        }
        else {
            refreshTokenRepository.delete(refreshToken);

            throw new ExpiredTokenException("Refresh Token has expired");
        }
    }

    private LoginResponse build(String token, String refreshToken){
        return LoginResponse.builder()
                .authenticationToken(token)
                .refreshToken(refreshToken)
                .expiresAt(Instant.now().plusSeconds(jwtUtil.getJwtExpiration()))
                .build();
    }

    public boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
    }

}
