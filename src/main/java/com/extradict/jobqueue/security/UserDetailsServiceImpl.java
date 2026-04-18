package com.extradict.jobqueue.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    // Hardcoded users for L3
    // In L4 this moves to a users table in PostgreSQL
    private static final Map<String, String> USERS = Map.of(
            "producer_one", "secret123",
            "producer_two", "secret456"
    );

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        String password = USERS.get(username);

        if (password == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return User.builder()
                .username(username)
                .password(password) // {noop} = no encoding for L3
                .roles("PRODUCER")
                .build();
    }
}