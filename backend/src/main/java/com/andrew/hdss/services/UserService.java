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
import com.andrew.hdss.utils.PaginationRequest;
import com.andrew.hdss.utils.Util;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @CacheEvict(cacheNames = "users", allEntries = true)
    public void createUser(UserCreationRequest userCreationRequest) throws Exception {
        if(userRepository.existsByEmail(userCreationRequest.getEmail())){
            throw new ResourceAlreadyExistsException("Email is already in use");
        }
        else{
            String username = String.valueOf(Character.toUpperCase(userCreationRequest.getFirstName().charAt(0))) +
                    Character.toUpperCase(userCreationRequest.getLastName().charAt(0)) +
                    Util.generateRandomString(4);

            UserRole userRole = UserRole.valueOf(userCreationRequest.getRole());

            User user = new User();
            user.setUsername(username);
            user.setFirstName(userCreationRequest.getFirstName());
            user.setLastName(userCreationRequest.getLastName());
            user.setEmail(userCreationRequest.getEmail());
            user.setPassword(passwordEncoder.encode(userCreationRequest.getPassword()));
            user.setDeleted(false);
            user.setRole(userRole);

            if(userRole == UserRole.ADMIN){
                user.setEnabled(false);

                String token = generateVerificationToken(user);

                NotificationEmail email = NotificationEmail.builder()
                        .subject("Account Verification")
                        .recipient(user.getEmail())
                        .title("HDSS Account Verification")
                        .body("Thank you for signing up to HDSS, " + userCreationRequest.getFirstName() +
                                " please click on the below url to activate your account: " +
                                "http://localhost:8080/api/auth/accountVerification/" + token)
                        .build();

                mailService.sendMail(email);
            }else {
                user.setEnabled(true);
            }

            userRepository.save(user);
        }
    }

    @Transactional
    @CacheEvict(cacheNames = "users", allEntries = true)
    public void deleteUser(UserDto userDto) throws Exception {
        User user = userRepository.findByEmail(userDto.getEmail())
                .orElseThrow(
                        () -> new EntityNotFoundException("User does not exist")
                );

        if(user.isDeleted()){
            return;
        }

        user.setEnabled(false);
        user.setDeleted(true);

        List<RefreshToken> refreshTokens = refreshTokenRepository.findByUser(user);
        if(!refreshTokens.isEmpty()){
            refreshTokenRepository.deleteAll(refreshTokens);
        }

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = "users",
            key = "#resourceRequest.page + '-' + #resourceRequest.size"
    )
    public BatchResponse<UserDto> getUsers(ResourceRequest resourceRequest){
        PaginationRequest paginationRequest = PaginationRequest.builder()
                .page(resourceRequest.getPage())
                .size(resourceRequest.getSize())
                .build();

        Pageable pageable = Util.getPageable(paginationRequest);

        Page<User> usersPage = userRepository.findAll(pageable);
        List<UserDto> users = usersPage.stream()
                .map(this::convertToDto)
                .toList();

        BatchResponse<UserDto> response = new BatchResponse<>();
        response.setPage(usersPage.getNumber());
        response.setSize(usersPage.getSize());
        response.setTotalPages(usersPage.getTotalPages());
        response.setTotalElements(usersPage.getTotalElements());
        response.setData(users);

        return response;
    }

    @Transactional(readOnly = true)
    public UserDto getUser(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(
                        () -> new EntityNotFoundException("User does not exist with username: " + username)
                );

        return convertToDto(user);
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

    private UserDto convertToDto(User user){
        return UserDto.builder()
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .deleted(user.isDeleted())
                .build();
    }
}
