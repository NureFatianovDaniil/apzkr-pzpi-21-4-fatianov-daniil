package com.nure.apz.fatianov.daniil.AuthService.config;

import com.nure.apz.fatianov.daniil.AuthService.user.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String SECRET = "4786fe3770b8f0e45ac836f1b4f93bbcb53f412624ce9461d0de8238f2c57aa1";

    /**
     * Extracts the username from a JWT token by retrieving the subject claim.
     * This method is commonly used to identify the user associated with a JWT, as the subject claim typically contains the user's username or email.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Accessing the subject claim of the JWT, which is expected to hold the username.</li>
     * </ul>
     * </p>
     *
     * @param token The JWT token from which the username is to be extracted.
     * @return The username extracted from the JWT token's subject claim.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from a JWT token using a function provided as an argument.
     * This method is a generic utility that can be used to retrieve any type of claim from the JWT, such as the subject, expiration time, or custom claims.
     * It relies on a function that applies to the JWT's claims to extract the desired data.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Decoding the JWT to extract all its claims.</li>
     *     <li>Applying a function to these claims to retrieve a specific claim value.</li>
     * </ul>
     * </p>
     *
     * @param token The JWT token from which the claim is to be extracted.
     * @param claimsResolver A function that takes the JWT's claims and returns a value of type T extracted from the claims.
     * @param <T> The type of the claim value to be returned.
     * @return The claim value of type T extracted from the JWT, determined by the claimsResolver function.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final  Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a JWT token for the specified user details without additional claims.
     * This method is a convenience wrapper that calls an overloaded version of itself with an empty map for claims,
     * primarily focusing on the user's principal for token generation.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Generating a JWT token using default claims and the provided user details.</li>
     *     <li>Delegating to the overloaded {@code generateToken} method that handles the encoding and signing of the token.</li>
     * </ul>
     * </p>
     *
     * @param userDetails The user details object containing information about the user for whom the token is to be generated.
     * @return A JWT token encoded as a String, representing the authenticated user's session.
     */
    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a JWT token for the specified user details, incorporating additional claims provided in the map.
     * This method constructs a JWT by setting the necessary claims like subject, issuance date, and expiration.
     * It also incorporates any extra claims provided, allowing for flexibility in the information embedded within the token.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Setting standard claims such as the subject (username), issued at, and expiration time.</li>
     *     <li>Incorporating additional claims provided through the {@code extraClaims} map.</li>
     *     <li>Signing the JWT with a specified key and using the HS256 signature algorithm.</li>
     *     <li>Compacting the JWT into a URL-safe string format.</li>
     * </ul>
     * </p>
     *
     * @param extraClaims A map containing additional claims to be included in the JWT.
     * @param userDetails The user details object containing information about the user for whom the token is to be generated.
     * @return A JWT token as a String, encoded and signed, representing the authenticated user's session with potentially extended information.
     */
    public String generateToken(
            Map<String, Object>  extraClaims,
            UserDetails userDetails
    ){
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Determines if the user associated with the provided JWT token and user details holds administrative privileges.
     * This method checks if the username extracted from the token matches the username in the provided user details,
     * verifies that the user has the 'ADMIN' role among their authorities, and ensures that the token has not expired.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Extracting the username from the JWT token.</li>
     *     <li>Comparing the extracted username with the username in the user details.</li>
     *     <li>Checking the user's authorities for the 'ADMIN' role.</li>
     *     <li>Verifying that the token is still valid (i.e., has not expired).</li>
     * </ul>
     * </p>
     *
     * @param token The JWT token that represents the user's session.
     * @param userDetails The user details which include username and authorities.
     * @return true if the user is an admin and the token is valid; false otherwise.
     */
    public boolean isAdmin(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) &&
                userDetails.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals(Role.ADMIN.name())) &&
                !isTokenExpired(token);
    }

    /**
     * Determines if the user associated with the provided JWT token and user details holds standard user privileges.
     * This method checks if the username extracted from the token matches the username in the provided user details,
     * verifies that the user has the 'USER' role among their authorities, and ensures that the token has not expired.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Extracting the username from the JWT token.</li>
     *     <li>Comparing the extracted username with the username in the user details.</li>
     *     <li>Checking the user's authorities for the 'USER' role.</li>
     *     <li>Verifying that the token is still valid (i.e., has not expired).</li>
     * </ul>
     * </p>
     *
     * @param token The JWT token that represents the user's session.
     * @param userDetails The user details which include username and authorities.
     * @return true if the user is a standard user and the token is valid; false otherwise.
     */
    public boolean isUser(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) &&
                userDetails.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals(Role.USER.name())) &&
                !isTokenExpired(token);
    }

    /**
     * Checks if a JWT token is valid by verifying that the username encoded within the token matches the username
     * in the provided user details and that the token has not expired.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Extracting the username from the JWT token.</li>
     *     <li>Comparing the extracted username with the username provided in the user details.</li>
     *     <li>Checking if the token has expired.</li>
     * </ul>
     * </p>
     *
     * @param token The JWT token to be validated.
     * @param userDetails The user details containing the username against which the token's username is compared.
     * @return true if the token's username matches the user details' username and the token has not expired; false otherwise.
     */
    public  boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Determines if a JWT token has expired by comparing the token's expiration time with the current system time.
     * This method extracts the expiration date from the token and checks if this date has already passed.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Extracting the expiration date from the JWT token.</li>
     *     <li>Comparing the extracted date to the current system date to determine if the token has expired.</li>
     * </ul>
     * </p>
     *
     * @param token The JWT token whose expiration status is being checked.
     * @return true if the current date is after the token's expiration date, indicating that the token has expired; false otherwise.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from a JWT token by accessing its 'exp' claim.
     * This method uses the `extractClaim` function to specifically retrieve the expiration time of the token,
     * which is crucial for validating the token's active period.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Using the `extractClaim` method with a lambda that accesses the expiration claim from the JWT's claims.</li>
     * </ul>
     * </p>
     *
     * @param token The JWT token from which the expiration date is to be extracted.
     * @return A {@link Date} object representing the expiration time of the token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts all claims from a JWT token by parsing it with the specified signing key.
     * This method validates the token and retrieves its body, which contains all the claims.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Building a JWT parser with the signing key.</li>
     *     <li>Parsing the JWT token to validate it and extract the claims.</li>
     *     <li>Returning the claims contained within the token.</li>
     * </ul>
     * </p>
     *
     * @param token The JWT token from which all claims are to be extracted.
     * @return A {@link Claims} object containing all the claims present in the JWT token.
     */
    private Claims extractAllClaims(String token){

        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Retrieves the signing key used for validating and signing JWT tokens.
     * This method decodes the base64-encoded secret key and generates an HMAC-SHA key from the decoded bytes.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Decoding the base64-encoded secret key.</li>
     *     <li>Generating an HMAC-SHA key from the decoded byte array.</li>
     * </ul>
     * </p>
     *
     * @return A {@link Key} object used for signing and validating JWT tokens.
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
