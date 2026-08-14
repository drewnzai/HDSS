package com.andrew.hdss.services;

import com.andrew.hdss.dtos.UserCreationRequest;
import com.andrew.hdss.exceptions.ResourceAlreadyExistsException;
import com.andrew.hdss.models.User;
import com.andrew.hdss.models.VerificationToken;
import com.andrew.hdss.models.enums.UserRole;
import com.andrew.hdss.repositories.UserRepository;
import com.andrew.hdss.repositories.VerificationTokenRepository;
import com.andrew.hdss.utils.NotificationEmail;
import com.andrew.hdss.utils.Util;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
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
    public void deleteUser(UserCreationRequest userCreationRequest) throws Exception {
        User user = userRepository.findByUsername(userCreationRequest.getEmail())
                .orElseThrow(
                        () -> new Exception("User does not exist")
                );

        user.setEnabled(false);
        user.setDeleted(true);

        userRepository.save(user);
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
}
