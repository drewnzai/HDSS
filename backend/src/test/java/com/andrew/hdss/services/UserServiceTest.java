package com.andrew.hdss.services;


import com.andrew.hdss.dtos.BatchResponse;
import com.andrew.hdss.dtos.ResourceRequest;
import com.andrew.hdss.dtos.UserCreationRequest;
import com.andrew.hdss.dtos.UserDto;
import com.andrew.hdss.exceptions.EntityNotFoundException;
import com.andrew.hdss.exceptions.ResourceAlreadyExistsException;
import com.andrew.hdss.models.RefreshToken;
import com.andrew.hdss.models.User;
import com.andrew.hdss.models.VerificationToken;
import com.andrew.hdss.models.enums.UserRole;
import com.andrew.hdss.repositories.RefreshTokenRepository;
import com.andrew.hdss.repositories.UserRepository;
import com.andrew.hdss.repositories.VerificationTokenRepository;
import com.andrew.hdss.utils.NotificationEmail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private MailService mailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        clearAllCaches();
    }

    // -------------------------------------------------------------------------
    // createUser()
    // -------------------------------------------------------------------------

    @Test
    void createUser_shouldThrowWhenEmailAlreadyExists() {
        UserCreationRequest request = createUserRequest(
                "John",
                "Doe",
                "john@example.com",
                "password",
                "USER"
        );

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("Email is already in use");

        verify(userRepository)
                .existsByEmail("john@example.com");

        verify(userRepository, never())
                .save(any(User.class));

        verifyNoInteractions(
                passwordEncoder,
                verificationTokenRepository,
                refreshTokenRepository,
                mailService
        );
    }

    @Test
    void createUser_shouldCreateEnabledUserWhenRoleIsUser() throws Exception {
        UserCreationRequest request = createUserRequest(
                "John",
                "Doe",
                "john@example.com",
                "password",
                "USER"
        );

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password"))
                .thenReturn("encoded-password");

        userService.createUser(request);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository)
                .save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getUsername())
                .matches("JD[A-Za-z0-9]{4}");

        assertThat(savedUser.getFirstName())
                .isEqualTo("John");

        assertThat(savedUser.getLastName())
                .isEqualTo("Doe");

        assertThat(savedUser.getEmail())
                .isEqualTo("john@example.com");

        assertThat(savedUser.getPassword())
                .isEqualTo("encoded-password");

        assertThat(savedUser.getRole())
                .isEqualTo(UserRole.USER);

        assertThat(savedUser.isEnabled())
                .isTrue();

        assertThat(savedUser.isDeleted())
                .isFalse();

        verify(passwordEncoder)
                .encode("password");

        verifyNoInteractions(
                verificationTokenRepository,
                mailService,
                refreshTokenRepository
        );
    }

    @Test
    void createUser_shouldCreateDisabledAdminAndSendVerificationEmail()
            throws Exception {

        UserCreationRequest request = createUserRequest(
                "John",
                "Doe",
                "john@example.com",
                "password",
                "ADMIN"
        );

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password"))
                .thenReturn("encoded-password");

        userService.createUser(request);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository)
                .save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getUsername())
                .matches("JD[A-Za-z0-9]{4}");

        assertThat(savedUser.getFirstName())
                .isEqualTo("John");

        assertThat(savedUser.getLastName())
                .isEqualTo("Doe");

        assertThat(savedUser.getEmail())
                .isEqualTo("john@example.com");

        assertThat(savedUser.getPassword())
                .isEqualTo("encoded-password");

        assertThat(savedUser.getRole())
                .isEqualTo(UserRole.ADMIN);

        assertThat(savedUser.isEnabled())
                .isFalse();

        assertThat(savedUser.isDeleted())
                .isFalse();

        // Verify verification token
        ArgumentCaptor<VerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(VerificationToken.class);

        verify(verificationTokenRepository)
                .save(tokenCaptor.capture());

        VerificationToken savedToken = tokenCaptor.getValue();

        assertThat(savedToken.getToken())
                .isNotBlank();

        assertThat(savedToken.getUser())
                .isSameAs(savedUser);

        assertThat(savedToken.getExpiryDate())
                .isAfter(Instant.now());

        // Verify email
        ArgumentCaptor<NotificationEmail> emailCaptor =
                ArgumentCaptor.forClass(NotificationEmail.class);

        verify(mailService)
                .sendMail(emailCaptor.capture());

        NotificationEmail email = emailCaptor.getValue();

        assertThat(email.getSubject())
                .isEqualTo("Account Verification");

        assertThat(email.getRecipient())
                .isEqualTo("john@example.com");

        assertThat(email.getTitle())
                .isEqualTo("HDSS Account Verification");

        assertThat(email.getBody())
                .contains("John");

        assertThat(email.getBody())
                .contains(savedToken.getToken());
    }

    @Test
    void createUser_shouldEncodePasswordBeforeSaving() throws Exception {
        UserCreationRequest request = createUserRequest(
                "John",
                "Doe",
                "john@example.com",
                "plain-password",
                "USER"
        );

        when(userRepository.existsByEmail("john@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("plain-password"))
                .thenReturn("encoded-password");

        userService.createUser(request);

        verify(passwordEncoder)
                .encode("plain-password");

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository)
                .save(captor.capture());

        assertThat(captor.getValue().getPassword())
                .isEqualTo("encoded-password");
    }

    // -------------------------------------------------------------------------
    // deleteUser()
    // -------------------------------------------------------------------------

    @Test
    void deleteUser_shouldThrowWhenUserDoesNotExist() throws Exception {
        UserDto userDto = UserDto.builder()
                .email("john@example.com")
                .build();

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(userDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User does not exist");

        verify(userRepository)
                .findByEmail("john@example.com");

        verify(userRepository, never())
                .save(any());

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void deleteUser_shouldDoNothingWhenUserIsAlreadyDeleted()
            throws Exception {

        UserDto userDto = UserDto.builder()
                .email("john@example.com")
                .build();

        User user = createUser();

        user.setDeleted(true);
        user.setEnabled(false);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        userService.deleteUser(userDto);

        verify(userRepository, never())
                .save(any());

        verifyNoInteractions(refreshTokenRepository);

        assertThat(user.isDeleted())
                .isTrue();

        assertThat(user.isEnabled())
                .isFalse();
    }

    @Test
    void deleteUser_shouldDisableAndDeleteUserAndRefreshTokens()
            throws Exception {

        UserDto userDto = UserDto.builder()
                .email("john@example.com")
                .build();

        User user = createUser();

        RefreshToken refreshToken1 = new RefreshToken();
        RefreshToken refreshToken2 = new RefreshToken();

        List<RefreshToken> refreshTokens =
                List.of(refreshToken1, refreshToken2);

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(refreshTokenRepository.findByUser(user))
                .thenReturn(refreshTokens);

        userService.deleteUser(userDto);

        assertThat(user.isDeleted())
                .isTrue();

        assertThat(user.isEnabled())
                .isFalse();

        verify(refreshTokenRepository)
                .findByUser(user);

        verify(refreshTokenRepository)
                .deleteAll(refreshTokens);

        verify(userRepository)
                .save(user);
    }

    @Test
    void deleteUser_shouldNotDeleteRefreshTokensWhenNoneExist()
            throws Exception {

        UserDto userDto = UserDto.builder()
                .email("john@example.com")
                .build();

        User user = createUser();

        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(user));

        when(refreshTokenRepository.findByUser(user))
                .thenReturn(Collections.emptyList());

        userService.deleteUser(userDto);

        assertThat(user.isDeleted())
                .isTrue();

        assertThat(user.isEnabled())
                .isFalse();

        verify(refreshTokenRepository)
                .findByUser(user);

        verify(refreshTokenRepository, never())
                .deleteAll(any());

        verify(userRepository)
                .save(user);
    }

    // -------------------------------------------------------------------------
    // getUsers()
    // -------------------------------------------------------------------------

    @Test
    void getUsers_shouldReturnPaginatedUsers() {
        ResourceRequest request = new ResourceRequest();
        request.setPage(0);
        request.setSize(10);

        User user1 = createUser();
        user1.setUsername("JD1234");
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("john@example.com");
        user1.setRole(UserRole.USER);
        user1.setEnabled(true);
        user1.setDeleted(false);

        User user2 = createUser();
        user2.setUsername("JS5678");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane@example.com");
        user2.setRole(UserRole.ADMIN);
        user2.setEnabled(false);
        user2.setDeleted(false);

        Page<User> page = new PageImpl<>(
                List.of(user1, user2),
                PageRequest.of(0, 10),
                2
        );

        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        BatchResponse<UserDto> response =
                userService.getUsers(request);

        assertThat(response)
                .isNotNull();

        assertThat(response.getPage())
                .isEqualTo(0);

        assertThat(response.getSize())
                .isEqualTo(10);

        assertThat(response.getTotalPages())
                .isEqualTo(1);

        assertThat(response.getTotalElements())
                .isEqualTo(2);

        assertThat(response.getData())
                .hasSize(2);

        UserDto first = response.getData().getFirst();

        assertThat(first.getUsername())
                .isEqualTo("JD1234");

        assertThat(first.getFirstName())
                .isEqualTo("John");

        assertThat(first.getLastName())
                .isEqualTo("Doe");

        assertThat(first.getEmail())
                .isEqualTo("john@example.com");

        assertThat(first.getRole())
                .isEqualTo("USER");

        assertThat(first.isEnabled())
                .isTrue();

        assertThat(first.isDeleted())
                .isFalse();

        UserDto second = response.getData().get(1);

        assertThat(second.getUsername())
                .isEqualTo("JS5678");

        assertThat(second.getRole())
                .isEqualTo("ADMIN");

        assertThat(second.isEnabled())
                .isFalse();
    }

    @Test
    void getUsers_shouldReturnEmptyResponseWhenNoUsersExist() {
        ResourceRequest request = new ResourceRequest();
        request.setPage(0);
        request.setSize(10);

        Page<User> page = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 10),
                0
        );

        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        BatchResponse<UserDto> response =
                userService.getUsers(request);

        assertThat(response)
                .isNotNull();

        assertThat(response.getData())
                .isEmpty();

        assertThat(response.getTotalElements())
                .isZero();

        assertThat(response.getTotalPages())
                .isZero();
    }

    // -------------------------------------------------------------------------
    // getUser()
    // -------------------------------------------------------------------------

    @Test
    void getUser_shouldReturnUserDto() {
        User user = createUser();

        user.setUsername("JD1234");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setDeleted(false);

        when(userRepository.findByUsername("JD1234"))
                .thenReturn(Optional.of(user));

        UserDto result =
                userService.getUser("JD1234");

        assertThat(result)
                .isNotNull();

        assertThat(result.getUsername())
                .isEqualTo("JD1234");

        assertThat(result.getFirstName())
                .isEqualTo("John");

        assertThat(result.getLastName())
                .isEqualTo("Doe");

        assertThat(result.getEmail())
                .isEqualTo("john@example.com");

        assertThat(result.getRole())
                .isEqualTo("USER");

        assertThat(result.isEnabled())
                .isTrue();

        assertThat(result.isDeleted())
                .isFalse();

        verify(userRepository)
                .findByUsername("JD1234");
    }

    @Test
    void getUser_shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser("unknown"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(
                        "User does not exist with username: unknown"
                );
    }

    // -------------------------------------------------------------------------
    // Test helpers
    // -------------------------------------------------------------------------

    private User createUser() {
        User user = new User();

        user.setUsername("JD1234");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");
        user.setPassword("encoded-password");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setDeleted(false);

        return user;
    }

    private UserCreationRequest createUserRequest(
            String firstName,
            String lastName,
            String email,
            String password,
            String role
    ) {
        UserCreationRequest request = new UserCreationRequest();

        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setEmail(email);
        request.setPassword(password);
        request.setRole(role);

        return request;
    }

    private void clearAllCaches() {
        // UserService does not directly use SecurityContextHolder,
        // so there is currently no per-test security state to clear.
    }
}