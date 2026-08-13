package com.andrew.hdss.auth;

import com.andrew.hdss.exceptions.UserNotVerifiedException;
import com.andrew.hdss.models.User;
import com.andrew.hdss.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("Username not found")
        );

        if(!user.isEnabled()){
            throw new UserNotVerifiedException("User is not verified. Please check email and verify");
        }

        return new UserDetailsImpl(user);
    }

}