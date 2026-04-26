package com.extradict.jobqueue.controller;

import com.extradict.jobqueue.dto.AuthRequest;
import com.extradict.jobqueue.dto.AuthResponse;
import com.extradict.jobqueue.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/token")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            System.out.println("Controller HIT");
            System.out.println(request.getUsername() + " " + request.getPassword());

            AuthResponse response = authService.login(request);
            System.out.println("Token generated: " + response.getToken());
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            System.out.println("Bad credentials: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");

        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Auth failed: " + e.getMessage());
        }
    }
}