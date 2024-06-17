package com.nure.apz.fatianov.daniil.vehiclestationservice.station;

import com.nure.apz.fatianov.daniil.vehiclestationservice.station.request.StationAddRequest;
import com.nure.apz.fatianov.daniil.vehiclestationservice.station.request.StationChangeRequest;
import com.nure.apz.fatianov.daniil.vehiclestationservice.station.response.StationGetAllResponseEntity;
import com.nure.apz.fatianov.daniil.vehiclestationservice.station.response.objects.VehicleEntityForStation;
import com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.Vehicle;
import com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StationService {

    private static final String userServiceUrl = "http://localhost:8082/user-service";

    private final VehicleRepository vehicleRepository;

    private final StationRepository stationRepository;
    private final RestTemplateBuilder restTemplateBuilder;

    /**
     * Adds a new station to the system based on the provided request details.
     * This method checks if the user has administrative privileges before proceeding to create a new station.
     * It assigns a unique sequential number to the station, sets its properties from the request, and saves it to the database.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Verifying administrative privileges using the authorization header.</li>
     *     <li>Generating a unique station number and setting station properties from the request body.</li>
     *     <li>Saving the new station to the repository and flushing the session to ensure it's stored immediately.</li>
     * </ul>
     * </p>
     *
     * @param requestBody The {@link StationAddRequest} containing details about the new station such as description, latitude, longitude, altitude, and type.
     * @param authHeader The authorization header used to verify administrative access.
     * @throws IllegalStateException If the authorization header is invalid, indicating the user does not have administrative privileges.
     */
    public void addStation(StationAddRequest requestBody, String authHeader) {
        if (!isAdmin(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }

        Station station = new Station();
        station.setNumber("ST" + stationRepository.getNextSequenceValue());
        station.setDescription(requestBody.getDescription());
        station.setLatitude(requestBody.getLatitude());
        station.setLongitude(requestBody.getLongitude());
        station.setAltitude(requestBody.getAltitude());
        station.setType(requestBody.getType());
        stationRepository.saveAndFlush(station);
    }

    /**
     * Retrieves all stations in the system along with associated details and vehicles stationed at each.
     * This method first verifies if the user making the request has administrative privileges.
     * It then fetches all stations from the repository and constructs a response list that includes detailed information
     * about each station and the vehicles associated with it.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Checking for administrative privileges using the provided authorization header.</li>
     *     <li>Fetching all stations from the repository.</li>
     *     <li>Compiling detailed information about each station, including associated vehicles, into a response entity list.</li>
     * </ul>
     * </p>
     *
     * @param authHeader The authorization header used to verify administrative access.
     * @return A list of {@link StationGetAllResponseEntity} containing detailed information about each station.
     * @throws IllegalStateException If the authorization header is invalid, indicating that the user does not have the necessary privileges.
     */
    public List<StationGetAllResponseEntity> getAllStations(String authHeader) {
        if (!isAdmin(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }

        List<Station> stations = stationRepository.findAll();
        List<StationGetAllResponseEntity> stationGetAllResponseEntities = new ArrayList<>();
        for (Station station : stations) {
            StationGetAllResponseEntity stationGetAllResponseEntity = new StationGetAllResponseEntity();
            stationGetAllResponseEntity.setId(station.getId());
            stationGetAllResponseEntity.setNumber(station.getNumber());
            stationGetAllResponseEntity.setDescription(station.getDescription());
            stationGetAllResponseEntity.setLatitude(station.getLatitude());
            stationGetAllResponseEntity.setLongitude(station.getLongitude());
            stationGetAllResponseEntity.setAltitude(station.getAltitude());
            stationGetAllResponseEntity.setType(station.getType());

            List<VehicleEntityForStation> vehicleEntities = new ArrayList<>();

            for (Vehicle vehicle : station.getVehicles()) {
                VehicleEntityForStation vehicleEntityForStation = new VehicleEntityForStation();
                vehicleEntityForStation.setNumber(vehicle.getNumber());
                vehicleEntityForStation.setStatus(vehicle.getStatus());
                vehicleEntities.add(vehicleEntityForStation);
            }

            stationGetAllResponseEntity.setVehicles(vehicleEntities);

            stationGetAllResponseEntities.add(stationGetAllResponseEntity);
        }
        return stationGetAllResponseEntities;
    }

    /**
     * Retrieves the number of a station based on its unique identifier.
     * This method first checks the validity of the authorization header to ensure the requester has the appropriate access.
     * If valid, it then queries the database for the station by its ID and returns the station number if found.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Validating the authorization header to ensure it is still active and valid.</li>
     *     <li>Checking if the provided station ID is non-null and exists in the database.</li>
     *     <li>Retrieving and returning the station number if the station exists.</li>
     * </ul>
     * </p>
     *
     * @param id The unique identifier of the station.
     * @param authHeader The authorization header used to validate access permissions.
     * @return The number of the station if it exists; otherwise, returns {@code null} if the ID is null.
     * @throws IllegalStateException If the authorization header is invalid or if no station with the provided ID exists.
     */
    public String getStationNumber(Integer id, String authHeader) {
        if (!isValid(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }

        if (id == null) {
            return null;
        }

        Optional<Station> optionalStation = stationRepository.findById(id);
        if (optionalStation.isEmpty()) {
            throw new IllegalStateException("Station with id: " + id + " does not exist");
        }
        return optionalStation.get().getNumber();
    }

    /**
     * Updates the details of an existing station based on the provided request data.
     * This method first verifies that the user has administrative privileges by checking the authorization header.
     * It then locates the station by its unique number and updates its details such as description, geographic coordinates,
     * altitude, and type, as provided in the request.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Verifying administrative privileges using the provided authorization header.</li>
     *     <li>Finding the station by its number and checking if it exists in the repository.</li>
     *     <li>Updating the station's properties according to the request data and saving the changes.</li>
     * </ul>
     * </p>
     *
     * @param requestBody The {@link StationChangeRequest} containing the new station details such as number, description, latitude, longitude, altitude, and type.
     * @param authHeader The authorization header used to verify administrative access.
     * @throws IllegalStateException If the authorization header is invalid or if no station with the provided number exists.
     */
    public void changeStation(StationChangeRequest requestBody, String authHeader) {
        if (!isAdmin(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }

        Optional<Station> optionalStation = stationRepository.findByNumber(requestBody.getNumber());
        if (optionalStation.isEmpty()) {
            throw new IllegalStateException("Station with number: "
                    + requestBody.getNumber() + " does not exist");
        }

        Station station = optionalStation.get();
        station.setDescription(requestBody.getDescription());
        station.setLatitude(requestBody.getLatitude());
        station.setLongitude(requestBody.getLongitude());
        station.setAltitude(requestBody.getAltitude());
        station.setType(requestBody.getType());
        stationRepository.saveAndFlush(station);
    }

    /**
     * Deletes a station from the system based on its unique number.
     * This method first verifies administrative privileges using the authorization header.
     * It then checks for the existence of the station by its number and ensures that no vehicles are currently associated with it before proceeding with deletion.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Verifying administrative privileges using the provided authorization header.</li>
     *     <li>Locating the station by its number in the repository and ensuring it exists.</li>
     *     <li>Checking that no vehicles are associated with the station, which is a prerequisite for deletion.</li>
     *     <li>Deleting the station from the repository if all conditions are met.</li>
     * </ul>
     * </p>
     *
     * @param number The unique number of the station to be deleted.
     * @param authHeader The authorization header used to verify administrative access.
     * @throws IllegalStateException If the authorization header is invalid, the station does not exist, or if the station has vehicles associated with it.
     */
    public void deleteStation(String number, String authHeader) {
        if (!isAdmin(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }

        Optional<Station> optionalStation = stationRepository.findByNumber(number);
        if (optionalStation.isEmpty()) {
            throw new IllegalStateException("Station with number: " + number + " does not exist");
        }

        Station station = optionalStation.get();
        if (!station.getVehicles().isEmpty()) {
            throw new IllegalStateException("Station with number: " + number + " has vehicles");
        }
        stationRepository.delete(station);
    }

    /**
     * Retrieves the unique identifier (ID) of a station given its number, after confirming the validity of the authorization header.
     * This method checks if the token within the authorization header is valid. If valid, it then searches for the station
     * by its number in the repository. If the station is found, its ID is returned.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Validating the authorization header to ensure it is still active and valid.</li>
     *     <li>Searching for the station by its number in the repository.</li>
     *     <li>Returning the station ID if found.</li>
     * </ul>
     * </p>
     *
     * @param number The number of the station for which the ID is sought.
     * @param authHeader The authorization header containing the bearer token.
     * @return The unique identifier of the station if found; otherwise, an exception is thrown.
     * @throws IllegalStateException If the authorization header is invalid or if no station with the provided number exists.
     */
    public Integer getStationId(String number, String authHeader) {
        if (!isValid(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }
        Optional<Station> optionalStation = stationRepository.findByNumber(number);
        if (optionalStation.isEmpty()) {
            throw new IllegalStateException("Vehicle with number: " + number + " does not exist");
        }

        return optionalStation.get().getId();
    }

    /**
     * Checks if the bearer of the token included in the authorization header has administrative privileges.
     * This method sends a request to a user service endpoint to validate the administrative status of the token bearer.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Validating the non-emptiness of the authorization header.</li>
     *     <li>Extracting the token from the header.</li>
     *     <li>Sending a request to the user service to verify if the token bearer is an administrator.</li>
     * </ul>
     * </p>
     *
     * @param authHeader The authorization header containing the bearer token.
     * @return A Boolean indicating whether the user is an administrator ({@code true}) or not ({@code false}).
     * @throws IllegalStateException If the user service responds with an error or if the response is not successful, suggesting that the token might not be valid or the user service is unreachable.
     * @throws RuntimeException If there is an issue with the network connection or server during the API call.
     */
    public Boolean isAdmin(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return false;
        }

        String token = authHeader.substring(7);
        String urlToUserService = userServiceUrl.concat("/auth/is-admin?token=" + token);

        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Boolean> responseEntity = restTemplate.exchange(urlToUserService, HttpMethod.GET, null, Boolean.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return responseEntity.getBody();
            } else {
                throw new IllegalStateException("Something went wrong");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if the bearer of the token provided in the authorization header is a registered user.
     * This method communicates with a user service endpoint to validate the user status of the token bearer.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Validating the non-emptiness of the authorization header.</li>
     *     <li>Extracting the token from the header and querying the user service to verify the user's status.</li>
     *     <li>Assessing the response to determine whether the bearer is recognized as a user.</li>
     * </ul>
     * </p>
     *
     * @param authHeader The authorization header containing the bearer token.
     * @return A Boolean indicating whether the bearer is recognized as a registered user ({@code true}) or not ({@code false}).
     * @throws IllegalStateException If the user service indicates that the token is invalid or if there is an error in the response, suggesting potential issues with the token or service accessibility.
     * @throws RuntimeException If there are network or server issues that prevent communication with the user service.
     */
    private Boolean isUser(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return false;
        }

        String token = authHeader.substring(7);
        String urlToUserService = userServiceUrl.concat("/auth/is-user?token=" + token);

        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Boolean> responseEntity = restTemplate.exchange(urlToUserService, HttpMethod.GET, null, Boolean.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return responseEntity.getBody();
            } else {
                throw new IllegalStateException("Something went wrong");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if the token provided in the authorization header is valid and active in the system.
     * This method communicates with a user service endpoint to validate the token's status.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Ensuring the authorization header is not empty.</li>
     *     <li>Extracting the token from the header.</li>
     *     <li>Sending a request to the user service to verify the token's validity.</li>
     * </ul>
     * </p>
     *
     * @param authHeader The authorization header containing the bearer token.
     * @return A Boolean indicating whether the token is valid ({@code true}) or not ({@code false}).
     * @throws IllegalStateException If the user service indicates the token is not valid, or if there is a problem with the response.
     * @throws RuntimeException If there is an error during the network connection or server interaction during the API call.
     */
    private Boolean isValid(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return false;
        }
        String token = authHeader.substring(7);
        String urlToUserService = userServiceUrl.concat("/auth/is-valid?token=" + token);

        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Boolean> responseEntity = restTemplate.exchange(urlToUserService, HttpMethod.GET, null, Boolean.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return responseEntity.getBody();
            } else {
                throw new IllegalStateException("Something went wrong");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
