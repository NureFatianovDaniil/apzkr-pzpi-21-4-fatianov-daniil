package com.nure.apz.fatianov.daniil.AuthService.user;

import com.nure.apz.fatianov.daniil.AuthService.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final JwtService jwtService;

    /**
     * Retrieves all users from the database and converts them into a list of user model objects.
     * This method accesses the user repository to fetch all user entities and then maps each entity to a model
     * representation suitable for external interfaces or further processing.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Fetching all user entities from the repository.</li>
     *     <li>Converting each user entity into a user model object.</li>
     *     <li>Collecting all user models into a list and returning it.</li>
     * </ul>
     * </p>
     *
     * @return A list of {@link UserModel} objects representing all users in the system.
     */
    public List<UserModel> getAllUsers() {
        List<User> users = userRepository.findAll();

        List<UserModel> userModels = new ArrayList<>();
        for (User user : users) {
            userModels.add(UserModel.toModel(user));
        }
        return userModels;
    }

    /**
     * Retrieves a user from the database by their unique identifier and converts the user entity into a user model object.
     * If no user is found with the given ID, the method returns null.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Querying the user repository for a user by ID.</li>
     *     <li>Converting the user entity to a model representation if the user is found.</li>
     *     <li>Returning null if no user is found.</li>
     * </ul>
     * </p>
     *
     * @param id The unique identifier of the user to be retrieved.
     * @return A {@link UserModel} representing the user if found; otherwise, null.
     */
    public UserModel getById(Integer id) {
        User user = userRepository.findById(id).orElse(null);
        return UserModel.toModel(user);
    }

    /**
     * Retrieves the user ID associated with a given token after validating the token's authenticity and the user's existence.
     * This method extracts the username from the token, searches for the corresponding user by email, and validates the token.
     * If the user exists and the token is valid, it returns the user's ID. If not, it throws exceptions based on the failure condition.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Extracting the username from the token using the JWT service.</li>
     *     <li>Finding the user by their email address in the user repository.</li>
     *     <li>Validating the token for authenticity against the found user.</li>
     *     <li>Returning the user's ID if the token is valid and the user exists.</li>
     * </ul>
     * </p>
     *
     * @param token The JWT token that contains the username encoded.
     * @return The ID of the user if the user exists and the token is valid.
     * @throws UsernameNotFoundException If no user corresponds to the username extracted from the token.
     * @throws IllegalStateException If the token is found to be invalid during validation.
     */
    public Integer getUserId(String token) {
        String username = jwtService.extractUsername(token);
        Optional<User> optionalUser = userRepository.findByEmail(username);

        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        User user = optionalUser.get();

        if (!jwtService.isTokenValid(token, user)) {
            throw new IllegalStateException("Invalid token");
        }

        return user.getId();
    }

    /**
     * Retrieves the email address of a user by their unique identifier.
     * This method queries the user repository to find a user by their ID. If the user exists, their email is returned.
     * If no user is found with the specified ID, the method throws a UsernameNotFoundException.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Querying the user repository for a user by their ID.</li>
     *     <li>Checking if the user exists and if so, returning their email address.</li>
     *     <li>Throwing an exception if no user is found to indicate the user does not exist.</li>
     * </ul>
     * </p>
     *
     * @param id The unique identifier of the user whose email address is being queried.
     * @return The email address of the user if they exist.
     * @throws UsernameNotFoundException If no user is found with the provided ID.
     */
    public String getUserEmail(Integer id) {
        Optional<User> result = userRepository.findById(id);
        if (result.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        System.out.println(result.get().getEmail());
        return result.get().getEmail();
    }
}
