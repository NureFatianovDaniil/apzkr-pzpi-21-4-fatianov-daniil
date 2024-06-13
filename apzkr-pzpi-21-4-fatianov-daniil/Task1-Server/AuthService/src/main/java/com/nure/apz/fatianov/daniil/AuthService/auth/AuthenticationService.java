package com.nure.apz.fatianov.daniil.AuthService.auth;

import com.nure.apz.fatianov.daniil.AuthService.config.JwtService;
import com.nure.apz.fatianov.daniil.AuthService.user.Role;
import com.nure.apz.fatianov.daniil.AuthService.user.User;
import com.nure.apz.fatianov.daniil.AuthService.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user in the system based on the provided registration details and issues a JWT token.
     * This method takes a registration request containing user details, creates a new user entity, saves it in the database,
     * and generates a JWT token for the newly registered user.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Building a new user entity from the registration request details.</li>
     *     <li>Encoding the user's password for secure storage.</li>
     *     <li>Saving the new user entity to the user repository.</li>
     *     <li>Generating a JWT token for authentication and authorization purposes.</li>
     * </ul>
     * </p>
     *
     * @param request A {@link RegisterRequest} containing the user's name, surname, email, phone, password, birthday, gender, and desired role.
     * @return An {@link AuthenticationResponse} containing the JWT token for the newly registered user.
     */
    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .birthday(request.getBirthday())
                .gender(request.getGender())
                .creationDate(LocalDateTime.now().atZone(ZoneOffset.UTC))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        var jwtTokent = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtTokent)
                .build();
    }

    /**
     * Authenticates a user based on their email and password. If authentication succeeds, a JWT token is issued.
     * This method checks the user's credentials against the stored details in the database. If the credentials are valid,
     * it generates a JWT token for the user, which can be used for further authentication and authorization tasks.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Authenticating the user credentials using the authentication manager.</li>
     *     <li>Retrieving the user entity from the repository based on the email provided.</li>
     *     <li>Generating a JWT token for the authenticated user.</li>
     * </ul>
     * </p>
     *
     * @param request A {@link AuthenticationRequest} containing the user's email and password.
     * @return An {@link AuthenticationResponse} containing the JWT token.
     * @throws UsernameNotFoundException If no user is found with the provided email, indicating authentication failure.
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(request.getEmail()));
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    /**
     * Checks if the user associated with the provided JWT token has administrative privileges.
     * This method extracts the username from the token, retrieves the corresponding user from the database,
     * and then checks if the user has an administrator role.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Extracting the username from the JWT token.</li>
     *     <li>Finding the user in the repository based on the extracted username.</li>
     *     <li>Checking if the user has administrative privileges using the JWT service.</li>
     * </ul>
     * </p>
     *
     * @param token The JWT token used to identify the user.
     * @return A Boolean indicating whether the user is an administrator ({@code true}) or not ({@code false}).
     * @throws UsernameNotFoundException If no user corresponds to the username extracted from the token or if there are other issues identified by an exception message.
     */
    public Boolean isAdmin(String token) {
        try{
            String username = jwtService.extractUsername(token);
            Optional<User> optionalUser = userRepository.findByEmail(username);
            if(optionalUser.isEmpty()) {
                throw new UsernameNotFoundException("User not found");
            }

            User user = optionalUser.get();
            return jwtService.isAdmin(token, user);

        }catch (Exception e){
            throw new UsernameNotFoundException("Something wrong: " + e.getMessage());
        }
    }

    /**
     * Determines if the user associated with the provided JWT token is recognized as a standard user in the system.
     * This method extracts the username from the JWT, finds the corresponding user in the database, and assesses their user role.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Extracting the username from the JWT token using the JWT service.</li>
     *     <li>Searching the user repository for a user with the extracted email.</li>
     *     <li>Verifying if the identified user matches the standard user criteria set within the JWT service.</li>
     * </ul>
     * </p>
     *
     * @param token The JWT token that is analyzed to identify the user.
     * @return A Boolean indicating whether the user has standard user privileges ({@code true}) or not ({@code false}).
     * @throws UsernameNotFoundException If no user corresponds to the username extracted from the token, or if other issues are identified through exception messages.
     */
    public Boolean isUser(String token) {
        try{
            String username = jwtService.extractUsername(token);
            Optional<User> optionalUser = userRepository.findByEmail(username);
            if(optionalUser.isEmpty()) {
                throw new UsernameNotFoundException("User not found");
            }

            User user = optionalUser.get();
            return jwtService.isUser(token, user);

        }catch (Exception e){
            throw new UsernameNotFoundException("Something wrong: " + e.getMessage());
        }
    }

    /**
     * Verifies the validity of a JWT token by ensuring it is correctly associated with a valid user and has not expired or been tampered with.
     * This method extracts the username encoded within the token, looks up the corresponding user in the database, and then checks if the token remains valid with respect to the found user.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Extracting the username from the JWT token using the JWT service.</li>
     *     <li>Finding the user in the database based on the extracted email.</li>
     *     <li>Validating the token against the user's details to ensure it has not expired or been compromised.</li>
     * </ul>
     * </p>
     *
     * @param token The JWT token to be validated.
     * @return A Boolean indicating the validity of the token: {@code true} if the token is valid, {@code false} otherwise.
     * @throws UsernameNotFoundException If no user corresponds to the username extracted from the token, indicating that the token is potentially invalid or forged.
     */
    public Boolean isValid(String token) {
        String username = jwtService.extractUsername(token);
        Optional<User> optionalUser = userRepository.findByEmail(username);
        if(optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        User user = optionalUser.get();

        return jwtService.isTokenValid(token, user);
    }
}
