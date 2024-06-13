package com.nure.apz.fatianov.daniil.AuthService.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-service/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Register a new user",
            description = "Registers a new user and returns authentication details.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User registered successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data")
            })
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ){
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @Operation(summary = "Authenticate a user",
            description = "Authenticates a user and returns authentication token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authentication successful"),
                    @ApiResponse(responseCode = "401", description = "Authentication failed")
            })
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody AuthenticationRequest request
    ){
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @Operation(summary = "Check admin rights",
            description = "Checks if the token provided belongs to an admin user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Admin rights confirmed"),
                    @ApiResponse(responseCode = "400", description = "Bad request, invalid token")
            })
    @GetMapping("/is-admin")
    public ResponseEntity<Boolean> isAdmin(
            @RequestParam String token
    ) {
        try{
            return ResponseEntity.ok().body(authenticationService.isAdmin(token));
        }catch(Exception ex){
            return ResponseEntity.badRequest().body(false);
        }

    }

    @Operation(summary = "Check user status",
            description = "Checks if the token provided belongs to a regular user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User status confirmed"),
                    @ApiResponse(responseCode = "400", description = "Bad request, invalid token")
            })
    @GetMapping("/is-user")
    public ResponseEntity<Boolean> isUser(
            @RequestParam String token
    ) {
        try{
            return ResponseEntity.ok().body(authenticationService.isUser(token));
        }catch(Exception ex){
            return ResponseEntity.badRequest().body(false);
        }

    }

    @Operation(summary = "Validate token",
            description = "Validates the provided token to see if it is still active.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token is valid"),
                    @ApiResponse(responseCode = "400", description = "Bad request, invalid token")
            })
    @GetMapping("/is-valid")
    public ResponseEntity<Boolean> isValid(
            @RequestParam String token
    ) {
        try{
            return ResponseEntity.ok().body(authenticationService.isValid(token));
        }catch(Exception ex){
            return ResponseEntity.badRequest().body(false);
        }

    }

    @Operation(summary = "Ping test",
            description = "Ping to test the availability of the service.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Service available")
            })
    @GetMapping("/ping")
    public ResponseEntity<String> ping(){
        return ResponseEntity.ok("pong");
    }
}
