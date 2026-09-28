package com.andrew.hdss.services;

import com.andrew.hdss.auth.UserDetailsImpl;
import com.andrew.hdss.dtos.LoginRequest;
import com.andrew.hdss.dtos.LoginResponse;
import com.andrew.hdss.dtos.RefreshTokenRequest;
import com.andrew.hdss.exceptions.EntityNotFoundException;
import com.andrew.hdss.exceptions.ExpiredTokenException;
import com.andrew.hdss.exceptions.UserDeletedException;
import com.andrew.hdss.exceptions.UserNotVerifiedException;
import com.andrew.hdss.models.RefreshToken;
import com.andrew.hdss.models.User;
import com.andrew.hdss.models.VerificationToken;
import com.andrew.hdss.models.enums.UserRole;
import com.andrew.hdss.repositories.RefreshTokenRepository;
import com.andrew.hdss.repositories.UserRepository;
import com.andrew.hdss.repositories.VerificationTokenRepository;
import com.andrew.hdss.utils.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Authentication Service Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // -------------------------------------------------------------------------
    // verifyAccount()
    // -------------------------------------------------------------------------

    @Test
    void verifyAccount_shouldEnableUserAndDeleteVerificationToken() {
        String token = "verification-token";

        User user = new User();
        user.setUsername("john");
        user.setEnabled(false);

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(Instant.now().plusSeconds(3600));

        when(verificationTokenRepository.findByToken(token))
                .thenReturn(Optional.of(verificationToken));

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        authService.verifyAccount(token);

        assertThat(user.isEnabled()).isTrue();

        verify(verificationTokenRepository)
                .delete(verificationToken);

        verify(userRepository)
                .save(user);
    }

    @Test
    void verifyAccount_shouldThrowWhenVerificationTokenDoesNotExist() {
        String token = "invalid-token";

        when(verificationTokenRepository.findByToken(token))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyAccount(token))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Could not find verification token");

        verifyNoInteractions(userRepository);
        verify(verificationTokenRepository, never())
                .delete(any());
    }

    @Test
    void verifyAccount_shouldThrowWhenUserDoesNotExist() {
        String token = "verification-token";

        User user = new User();
        user.setUsername("john");

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(Instant.now().plusSeconds(3600));

        when(verificationTokenRepository.findByToken(token))
                .thenReturn(Optional.of(verificationToken));

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyAccount(token))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Could not find user");

        verify(verificationTokenRepository, never())
                .delete(any());

        verify(userRepository, never())
                .save(any());
    }

    @Test
    void verifyAccount_shouldDeleteExpiredTokenAndThrow() {
        String token = "expired-token";

        User user = new User();
        user.setUsername("john");

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(Instant.now().minusSeconds(3600));

        when(verificationTokenRepository.findByToken(token))
                .thenReturn(Optional.of(verificationToken));

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.verifyAccount(token))
                .isInstanceOf(ExpiredTokenException.class)
                .hasMessage("Token has expired");

        verify(verificationTokenRepository)
                .delete(verificationToken);

        verify(userRepository, never())
                .save(any());

        assertThat(user.isEnabled()).isFalse();
    }

    // -------------------------------------------------------------------------
    // getCurrentUser()
    // -------------------------------------------------------------------------

    @Test
    void getCurrentUser_shouldReturnAuthenticatedUser() {
        User user = new User();
        user.setUsername("john");

        UserDetailsImpl principal = mock(UserDetailsImpl.class);

        when(principal.getUsername())
                .thenReturn("john");

        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal())
                .thenReturn(principal);

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        User result = authService.getCurrentUser();

        assertThat(result).isSameAs(user);

        verify(userRepository)
                .findByUsername("john");
    }

    @Test
    void getCurrentUser_shouldThrowWhenUserDoesNotExist() {
        UserDetailsImpl principal = mock(UserDetailsImpl.class);

        when(principal.getUsername())
                .thenReturn("john");

        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal())
                .thenReturn(principal);

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Could not find user");
    }

    // -------------------------------------------------------------------------
    // login()
    // -------------------------------------------------------------------------

    @Test
    void login_shouldAuthenticateCreateRefreshTokenAndReturnLoginResponse() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password");

        User user = new User();
        user.setUsername("john");
        user.setFirstName("John");
        user.setEnabled(true);
        user.setDeleted(false);

        // Set according to your actual User role enum.
        user.setRole(UserRole.USER);

        Authentication authentication = mock(Authentication.class);

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtUtil.generateJwtToken(authentication))
                .thenReturn("jwt-token");

        when(jwtUtil.getJwtExpiration())
                .thenReturn(3600L);

        LoginResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAuthenticationToken())
                .isEqualTo("jwt-token");
        assertThat(response.getUsername())
                .isEqualTo("john");
        assertThat(response.getFirstName())
                .isEqualTo("John");
        assertThat(response.getRole())
                .isEqualTo("USER");
        assertThat(response.getRefreshToken())
                .isNotBlank();
        assertThat(response.getExpiresAt())
                .isAfter(Instant.now());

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(jwtUtil)
                .generateJwtToken(authentication);

        verify(refreshTokenRepository)
                .save(any(RefreshToken.class));

        assertThat(SecurityContextHolder.getContext()
                .getAuthentication())
                .isSameAs(authentication);
    }

    @Test
    void login_shouldThrowWhenUserDoesNotExist() {
        LoginRequest request = new LoginRequest();
        request.setUsername("unknown");
        request.setPassword("password");

        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(
                        org.springframework.security.core.userdetails.UsernameNotFoundException.class)
                .hasMessage("Could not find user");

        verifyNoInteractions(authenticationManager);
        verifyNoInteractions(refreshTokenRepository);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void login_shouldThrowWhenUserIsNotVerified() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password");

        User user = new User();
        user.setUsername("john");
        user.setEnabled(false);
        user.setDeleted(false);

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserNotVerifiedException.class)
                .hasMessage("User is not verified. Please check email and verify");

        verifyNoInteractions(authenticationManager);
        verifyNoInteractions(refreshTokenRepository);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void login_shouldThrowWhenUserIsDeleted() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password");

        User user = new User();
        user.setUsername("john");
        user.setEnabled(true);
        user.setDeleted(true);

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserDeletedException.class)
                .hasMessage("The user is deleted");

        verifyNoInteractions(authenticationManager);
        verifyNoInteractions(refreshTokenRepository);
        verifyNoInteractions(jwtUtil);
    }

    // -------------------------------------------------------------------------
    // refresh()
    // -------------------------------------------------------------------------

    @Test
    void refresh_shouldReturnNewJwtWhenRefreshTokenIsValid() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setUsername("john");
        request.setToken("refresh-token");

        User user = new User();
        user.setUsername("john");
        user.setFirstName("John");
        user.setDeleted(false);
        user.setRole(UserRole.USER);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");
        refreshToken.setUser(user);
        refreshToken.setExpirationDate(
                Instant.now().plusSeconds(3600)
        );

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(refreshTokenRepository.findByTokenAndUser(
                "refresh-token",
                user
        )).thenReturn(Optional.of(refreshToken));

        when(jwtUtil.generateJwtTokenFromUsername("john"))
                .thenReturn("new-jwt-token");

        when(jwtUtil.getJwtExpiration())
                .thenReturn(3600L);

        LoginResponse response = authService.refresh(request);

        assertThat(response).isNotNull();
        assertThat(response.getAuthenticationToken())
                .isEqualTo("new-jwt-token");
        assertThat(response.getRefreshToken())
                .isEqualTo("refresh-token");
        assertThat(response.getUsername())
                .isEqualTo("john");

        verify(jwtUtil)
                .generateJwtTokenFromUsername("john");

        verify(refreshTokenRepository, never())
                .delete(any());
    }

    @Test
    void refresh_shouldThrowWhenUserDoesNotExist() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setUsername("unknown");
        request.setToken("refresh-token");

        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Could not find user");

        verifyNoInteractions(refreshTokenRepository);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void refresh_shouldThrowWhenUserIsDeleted() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setUsername("john");
        request.setToken("refresh-token");

        User user = new User();
        user.setUsername("john");
        user.setDeleted(true);

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(UserDeletedException.class)
                .hasMessage("The user is deleted");

        verifyNoInteractions(refreshTokenRepository);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void refresh_shouldThrowWhenRefreshTokenDoesNotExist() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setUsername("john");
        request.setToken("invalid-token");

        User user = new User();
        user.setUsername("john");
        user.setDeleted(false);

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(refreshTokenRepository.findByTokenAndUser(
                "invalid-token",
                user
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Could not find refresh token");

        verifyNoInteractions(jwtUtil);
    }

    @Test
    void refresh_shouldDeleteExpiredTokenAndThrow() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setUsername("john");
        request.setToken("expired-token");

        User user = new User();
        user.setUsername("john");
        user.setDeleted(false);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("expired-token");
        refreshToken.setUser(user);
        refreshToken.setExpirationDate(
                Instant.now().minusSeconds(3600)
        );

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(refreshTokenRepository.findByTokenAndUser(
                "expired-token",
                user
        )).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(ExpiredTokenException.class)
                .hasMessage("Refresh Token has expired");

        verify(refreshTokenRepository)
                .delete(refreshToken);

        verifyNoInteractions(jwtUtil);
    }

    // -------------------------------------------------------------------------
    // isLoggedIn()
    // -------------------------------------------------------------------------

    @Test
    void isLoggedIn_shouldReturnTrueWhenAuthenticationIsAuthenticated() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated())
                .thenReturn(true);

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThat(authService.isLoggedIn())
                .isTrue();
    }

    @Test
    void isLoggedIn_shouldReturnFalseWhenAuthenticationIsNotAuthenticated() {
        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated())
                .thenReturn(false);

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThat(authService.isLoggedIn())
                .isFalse();
    }

    @Test
    void isLoggedIn_shouldReturnFalseForAnonymousAuthentication() {
        Authentication authentication = mock(AnonymousAuthenticationToken.class);

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        assertThat(authService.isLoggedIn())
                .isFalse();

        verify(authentication, never())
                .isAuthenticated();
    }
}