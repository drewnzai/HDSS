package com.andrew.hdss.services;

import com.andrew.hdss.dtos.UserDto;
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
    public void createUser(UserDto userDto) throws Exception {
        if(userRepository.existsByEmail(userDto.getEmail())){
            throw new ResourceAlreadyExistsException("Email is already in use");
        }
        else{
            String username = String.valueOf(Character.toUpperCase(userDto.getFirstName().charAt(0))) +
                    Character.toUpperCase(userDto.getLastName().charAt(0)) +
                    Util.generateRandomString(4);

            UserRole userRole = UserRole.valueOf(userDto.getRole());

            User user = new User();
            user.setUsername(username);
            user.setFirstName(userDto.getFirstName());
            user.setLastName(userDto.getLastName());
            user.setEmail(userDto.getEmail());
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setRole(userRole);

            if(userRole == UserRole.ADMIN){
                user.setEnabled(false);

                String token = generateVerificationToken(user);

                NotificationEmail email = NotificationEmail.builder()
                        .subject("Account Verification")
                        .recipient(user.getEmail())
                        .title("HDSS Account Verification")
                        .body("Thank you for signing up to HDSS, " + userDto.getFirstName() +
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
    public void deleteUser(UserDto userDto) throws Exception {
        User user = userRepository.findByUsername(userDto.getEmail())
                .orElseThrow(
                        () -> new Exception("User does not exist")
                );

        userRepository.delete(user);
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
