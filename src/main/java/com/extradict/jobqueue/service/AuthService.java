package com.extradict.jobqueue.service;

import com.extradict.jobqueue.dto.AuthRequest;
import com.extradict.jobqueue.dto.AuthResponse;
import com.extradict.jobqueue.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthResponse login(AuthRequest request) {
        // 1. Verify username + password
        // Throws exception automatically if wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 2. Load user details
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.getUsername());

        // 3. Generate JWT token
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token);
    }
}