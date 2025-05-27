package com.fullStack.expenseTracker.controllers;

import com.fullStack.expenseTracker.dto.reponses.JwtResponseDto;
import com.fullStack.expenseTracker.dto.requests.SignInRequestDto;
import com.fullStack.expenseTracker.security.UserDetailsImpl;
import com.fullStack.expenseTracker.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class SignInController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Authenticates user and returns JWT token
     * @param signInRequestDto Contains email and password
     * @return ResponseEntity with JWT token and user details
     */
    @PostMapping("/login")
    public ResponseEntity<?> signIn(@RequestBody @Valid SignInRequestDto signInRequestDto) {
        try {
            // Authenticate user credentials
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    signInRequestDto.getEmail(), 
                    signInRequestDto.getPassword()
                )
            );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Generate JWT token
            String jwt = jwtUtils.generateJwtToken(authentication);

            // Get user details from authentication object
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            // Extract user roles
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // Build and return response
            return ResponseEntity.ok(
                JwtResponseDto.builder()
                    .username(userDetails.getUsername())
                    .email(userDetails.getEmail())
                    .id(userDetails.getId())
                    .token(jwt)
                    .roles(roles)
                    .build()
            );

        } catch (Exception e) {
            // Log authentication failures (don't reveal specific failure reason for security)
            return ResponseEntity.badRequest().body("Authentication failed: Invalid credentials");
        }
    }

    
}