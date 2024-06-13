package com.nure.apz.fatianov.daniil.AuthService.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-service/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Retrieve all users",
            description = "Fetches all registered users from the system.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of users retrieved successfully")
            })
    @GetMapping("/all")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<UserModel>> getAll() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Retrieve a user by ID",
            description = "Fetches a user by their unique identifier.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            })
    @GetMapping("/id")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<UserModel> getById(
            @RequestParam("id") Integer id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @Operation(summary = "Retrieve user ID by token",
            description = "Fetches the user ID associated with the provided authentication token.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User ID retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid token")
            })
    @GetMapping("/get-userId")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Integer> getUserId(
            @RequestParam String token
    ) {
        return ResponseEntity.ok(userService.getUserId(token));
    }

    @Operation(summary = "Retrieve user email by ID",
            description = "Fetches the email of the user identified by the given ID.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "User not found or error occurred")
            })
    @GetMapping("/get-user-email")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> getUserEmail(
            @RequestParam Integer id
    ) {
        try {
            return ResponseEntity.ok().body(userService.getUserEmail(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Ping test",
            description = "Ping to test the availability of the service.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Service available")
            })
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
