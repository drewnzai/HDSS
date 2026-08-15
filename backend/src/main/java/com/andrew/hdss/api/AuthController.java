package com.andrew.hdss.api;

import com.andrew.hdss.api.swaggerinterfaces.AuthApi;
import com.andrew.hdss.dtos.LoginRequest;
import com.andrew.hdss.dtos.LoginResponse;
import com.andrew.hdss.dtos.RefreshTokenRequest;
import com.andrew.hdss.services.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/")
@AllArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @GetMapping("accountVerification/{token}")
    @Override
    public ResponseEntity<String> verifyAccount(@PathVariable("token") String token) {
        authService.verifyAccount(token);
        return new ResponseEntity<>("Account Activated Successfully",
                HttpStatus.OK);
    }

    @PostMapping("login")
    @Override
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return new ResponseEntity<>(authService.login(loginRequest),
                HttpStatus.OK);
    }

    @PostMapping("refresh")
    @Override
    public ResponseEntity<LoginResponse> refresh(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return new ResponseEntity<>(authService.refresh(refreshTokenRequest),
                HttpStatus.OK);
    }

}