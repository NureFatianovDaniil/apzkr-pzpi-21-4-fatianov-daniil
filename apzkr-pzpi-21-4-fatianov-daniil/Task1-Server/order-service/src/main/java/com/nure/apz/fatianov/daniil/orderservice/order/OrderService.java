package com.nure.apz.fatianov.daniil.orderservice.order;

import com.nure.apz.fatianov.daniil.orderservice.request.IsSuitableRequestEntity;
import com.nure.apz.fatianov.daniil.orderservice.request.OrderAddRequestBody;
import com.nure.apz.fatianov.daniil.orderservice.request.OrderChangeRequestBody;
import com.nure.apz.fatianov.daniil.orderservice.request.OrderProcessRequestBody;
import com.nure.apz.fatianov.daniil.orderservice.response.OrderAdminGetResponseEntity;
import com.nure.apz.fatianov.daniil.orderservice.response.OrderUserGetResponse;
import com.nure.apz.fatianov.daniil.orderservice.response.OrderVehicleGetResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplateBuilder restTemplateBuilder;

    private static final String userServiceUrl = "http://localhost:8082/user-service";
    private static final String vehicleStationServiceUrl = "http://localhost:8084/vehicle-station-service";

    /**
     * Generates a simple numeric password.
     * This method uses a {@link Random} object to generate a 6-digit password,
     * where each digit is randomly chosen from 0 to 9.
     *
     * @return A {@link String} representing the generated password, which is 6 digits long.
     */
    public String generatePassword() {
        Random random = new Random();

        StringBuilder passwordBuilder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int digit = random.nextInt(10);
            passwordBuilder.append(digit);
        }

        return passwordBuilder.toString();
    }

    /**
     * Generates a unique numeric identifier.
     * This method utilizes a {@link Random} object to construct a 16-digit number,
     * randomly selecting each digit from 0 to 9. The resulting number can be used as
     * a unique identifier or key.
     *
     * @return A {@link String} that represents the generated numeric identifier, 16 digits in length.
     */
    public String generateNumber() {
        Random random = new Random();

        StringBuilder passwordBuilder = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int digit = random.nextInt(10);
            passwordBuilder.append(digit);
        }

        return passwordBuilder.toString();
    }

    /**
     * Adds a new order to the system using various details provided in the request body.
     * This method generates a unique order number similar to the system used by a postal service.
     * It verifies the authentication header, retrieves user ID based on the token, fetches the arrival station ID,
     * and constructs a new order object to save it into the repository.
     *
     * <p>Important steps include:
     * <ul>
     *     <li>Generating a unique order number not currently used for active orders.</li>
     *     <li>Validating the authentication header and extracting the user ID from it.</li>
     *     <li>Setting the order's arrival station based on the station number provided.</li>
     *     <li>Creating a unique receipt code for the order.</li>
     * </ul>
     * </p>
     *
     * @param requestBody The {@link OrderAddRequestBody} containing details like items, station number, etc.
     * @param authHeader The authorization header used to validate the request and fetch the user ID.
     * @throws IllegalStateException if the authentication header is invalid, or if fetching the user ID or station ID fails.
     * @throws RuntimeException if any external API call fails.
     */
    public void addOrder(OrderAddRequestBody requestBody, String authHeader) {
        //Todo Зробити генерацію номеру за допомогою шифрування
        // обробника,отримувача, точок відправлення та отримання та країни,
        // на прикладі номеру замовлень нової пошти

        if (!isValid(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }

        String randomNumber;

        while (true) {
            randomNumber = generateNumber();
            Optional<Order> optionalOrder =
                    orderRepository.findByNumberAndStatusNotLike(
                            randomNumber,
                            Status.RECEIVED
                    );

            if (optionalOrder.isEmpty()) {
                break;
            }
        }

        Integer userId;

        String token = authHeader.substring(7);
        String urlToUserService = userServiceUrl.concat("/user/get-userId?token=" + token);

        try {
            RestTemplate restTemplate = restTemplateBuilder.build();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Integer> responseEntity = restTemplate.exchange(urlToUserService, HttpMethod.GET, entity, Integer.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                userId = responseEntity.getBody();
            } else {
                throw new IllegalStateException("Something went wrong");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Order newOrder = new Order();
        newOrder.setUserId(userId);
        newOrder.setNumber(randomNumber);
        newOrder.setItems(requestBody.getItems());

        String urlToStationService = vehicleStationServiceUrl.concat("/station/get-id?number=" + requestBody.getArrivalStationNumber());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Integer> responseEntity = restTemplate.exchange(urlToStationService, HttpMethod.GET, entity, Integer.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                newOrder.setArrivalStationId(responseEntity.getBody());
            } else {
                throw new IllegalStateException("Station with number: "
                        + requestBody.getArrivalStationNumber() + " can not be found");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        newOrder.setStatus(Status.CREATED);
        newOrder.setReceiptCode(generatePassword());

        Instant creationDateUtc = requestBody.getCreationDate().toInstant();

        newOrder.setCreationDate(creationDateUtc);
        newOrder.setVehicleId(null);
        newOrder.setDepartureStationId(null);

        orderRepository.save(newOrder);
    }

    /**
     * Retrieves all orders from the system and enriches them with additional details such as user email, vehicle number, and station numbers.
     * This method checks if the requester is an admin based on the provided authentication header.
     * It then retrieves all orders from the repository and supplements each order with associated details fetched via external API calls.
     *
     * <p>Steps include:
     * <ul>
     *     <li>Verifying the admin status of the requester.</li>
     *     <li>Fetching all orders from the database.</li>
     *     <li>Enriching each order with user email, vehicle number, and station numbers (both departure and arrival).</li>
     * </ul>
     * </p>
     *
     * @param authHeader The authorization header to validate admin access.
     * @return A list of {@link OrderAdminGetResponseEntity} containing detailed information about each order.
     * @throws IllegalStateException If the authentication header is invalid, or if required data cannot be found for an order.
     * @throws RuntimeException If an error occurs during external API calls.
     */
    public List<OrderAdminGetResponseEntity> getAll(String authHeader) {

        if (!isAdmin(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }

        List<Order> orders = orderRepository.findAll();
        List<OrderAdminGetResponseEntity> responseEntities = new ArrayList<>();
        for (Order order : orders) {
            OrderAdminGetResponseEntity orderAdminGetResponse = new OrderAdminGetResponseEntity();
            orderAdminGetResponse.setId(order.getId());
            orderAdminGetResponse.setNumber(order.getNumber());
            orderAdminGetResponse.setReceiptCode(order.getReceiptCode());
            orderAdminGetResponse.setStatus(order.getStatus());
            orderAdminGetResponse.setCreationDate(order.getCreationDate().atZone(ZoneOffset.UTC));
            orderAdminGetResponse.setItems(order.getItems());

            String token = authHeader.substring(7);

            HttpHeaders headers = new HttpHeaders();

            headers.set("Authorization", "Bearer " + token);
            HttpEntity<Void> userHttpEntity = new HttpEntity<>(headers);
            String urlToUserService = userServiceUrl.concat("/user/get-user-email?id=" + order.getUserId());

            try {
                RestTemplate restTemplate = restTemplateBuilder.build();
                ResponseEntity<String> responseEntity = restTemplate.exchange(urlToUserService, HttpMethod.GET, userHttpEntity, String.class);

                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    orderAdminGetResponse.setUserEmail(responseEntity.getBody());
                } else {
                    throw new IllegalStateException("User with id "
                            + order.getUserId() + " can not be found");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (order.getVehicleId() != null) {
                HttpEntity<Void> vehicleHttpEntity = new HttpEntity<>(headers);
                String urlToVehicleService = vehicleStationServiceUrl.concat("/vehicle/get-number?id=" + order.getVehicleId());
                try {
                    RestTemplate restTemplate = restTemplateBuilder.build();
                    ResponseEntity<String> responseEntity = restTemplate.exchange(
                            urlToVehicleService,
                            HttpMethod.GET,
                            vehicleHttpEntity,
                            String.class);

                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        orderAdminGetResponse.setVehicleNumber(responseEntity.getBody());
                    } else {
                        throw new IllegalStateException("Vehicle with id "
                                + order.getVehicleId() + " can not be found");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                orderAdminGetResponse.setVehicleNumber(null);
            }

            String urlToStationService = vehicleStationServiceUrl.concat("/station/get-number?id=");

            if (order.getDepartureStationId() != null) {
                HttpEntity<Void> DepartureStationHttpEntity = new HttpEntity<>(headers);
                try {
                    RestTemplate restTemplate = restTemplateBuilder.build();
                    ResponseEntity<String> responseEntity = restTemplate.exchange(
                            urlToStationService.concat(String.valueOf(order.getDepartureStationId())),
                            HttpMethod.GET,
                            DepartureStationHttpEntity,
                            String.class);

                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        orderAdminGetResponse.setDepartureStationNumber(responseEntity.getBody());
                    } else {
                        throw new IllegalStateException("Station with id "
                                + order.getDepartureStationId() + " can not be found");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                orderAdminGetResponse.setDepartureStationNumber(null);
            }

            HttpEntity<Void> ArrivalStationHttpEntity = new HttpEntity<>(headers);
            try {
                RestTemplate restTemplate = restTemplateBuilder.build();
                ResponseEntity<String> responseEntity = restTemplate.exchange(
                        urlToStationService.concat(String.valueOf(order.getArrivalStationId())),
                        HttpMethod.GET,
                        ArrivalStationHttpEntity,
                        String.class);

                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    orderAdminGetResponse.setArrivalStationNumber(responseEntity.getBody());
                } else {
                    throw new IllegalStateException("Station with id "
                            + order.getDepartureStationId() + " can not be found");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            responseEntities.add(orderAdminGetResponse);
        }
        return responseEntities;
    }

    /**
     * Modifies an existing order in the system based on the provided request body. The order can only be modified if it has not been finalized.
     * This method validates user permissions using the provided authorization header and checks the order's current status
     * to ensure it's eligible for modification.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Verifying user permissions using the authorization header.</li>
     *     <li>Fetching the order from the repository and checking its current status.</li>
     *     <li>Updating the arrival station by making an external API call to resolve the station ID from the station number.</li>
     *     <li>Updating the order items and saving changes to the database.</li>
     * </ul>
     * </p>
     *
     * @param requestBody The {@link OrderChangeRequestBody} containing the new details for the order.
     * @param authHeader The authorization header to validate user access.
     * @throws IllegalStateException If the auth header is invalid, the order does not exist, or the order's status is not eligible for changes.
     * @throws RuntimeException If an error occurs during an external API call.
     */
    public void changeOrder(
            OrderChangeRequestBody requestBody,
            String authHeader) {

        if (!isUser(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }

        Optional<Order> optionalOrder = orderRepository.findById(requestBody.getId());
        if (optionalOrder.isEmpty()) {
            throw new IllegalStateException("Order with id " + requestBody.getId() + " does not exist");
        }

        Order newOrder = optionalOrder.get();

        if (newOrder.getStatus() == Status.RECEIVED ||
                newOrder.getStatus() == Status.DENIED ||
                newOrder.getStatus() == Status.SENT) {
            throw new IllegalStateException("Order with id " +
                    requestBody.getId() + " already has" + newOrder.getStatus());
        }

        String token = authHeader.substring(7);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        String urlToStationService = vehicleStationServiceUrl.concat("/station/get-id?number=" + requestBody.getArrivalStationNumber());

        HttpEntity<String> arrivalStationHttpEntity = new HttpEntity<>(headers);
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Integer> responseEntity = restTemplate.exchange(
                    urlToStationService,
                    HttpMethod.GET,
                    arrivalStationHttpEntity,
                    Integer.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                newOrder.setArrivalStationId(responseEntity.getBody());
            } else {
                throw new IllegalStateException("Station with number: "
                        + requestBody.getArrivalStationNumber() + " can not be found");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        newOrder.setItems(requestBody.getItems());

        orderRepository.save(newOrder);
    }

    /**
     * Processes an order by updating its details and status in the system. The operation includes validating
     * the administrative privileges of the requester, checking the current status of the order to ensure it
     * is eligible for processing, and updating various components of the order such as its departure station,
     * vehicle assignment, and item weights.
     *
     * <p>Steps include:
     * <ul>
     *     <li>Verifying administrative access using the provided authorization header.</li>
     *     <li>Validating the existence and status of the order.</li>
     *     <li>Fetching and setting the departure station based on the provided station number.</li>
     *     <li>Assigning a vehicle to the order and validating the vehicle's suitability for the order's requirements.</li>
     *     <li>Setting the order's status to processed and updating the order details in the repository.</li>
     * </ul>
     * </p>
     *
     * @param requestBody The {@link OrderProcessRequestBody} containing new details for the order including
     *                    departure station and vehicle number.
     * @param authHeader  The authorization header to validate administrative access.
     * @throws IllegalStateException If the authentication is invalid, the order does not exist, the order's status
     *                               is not eligible for changes, required entities like stations or vehicles cannot be found,
     *                               or if the vehicle is not suitable for the order's requirements.
     * @throws RuntimeException If any external API calls fail.
     */
    public void processOrder(
            OrderProcessRequestBody requestBody,
            String authHeader) {

        if (!isAdmin(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }

        Optional<Order> optionalOrder = orderRepository.findById(requestBody.getId());
        if (optionalOrder.isEmpty()) {
            throw new IllegalStateException("Order with id " + requestBody.getId() + " does not exist");
        }

        Order newOrder = optionalOrder.get();
        newOrder.setItems(requestBody.getItems());

        if (newOrder.getStatus() == Status.RECEIVED ||
                newOrder.getStatus() == Status.DENIED ||
                newOrder.getStatus() == Status.SENT) {
            throw new IllegalStateException("Order with id " +
                    requestBody.getId() + " already has" + newOrder.getStatus());
        }

        String token = authHeader.substring(7);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        String urlToStationService = vehicleStationServiceUrl.concat("/station/get-id?number=" + requestBody.getDepartureStationNumber());

        Integer departureStationId;

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Integer> responseEntity = restTemplate.exchange(
                    urlToStationService,
                    HttpMethod.GET,
                    entity,
                    Integer.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                departureStationId = responseEntity.getBody();
                newOrder.setDepartureStationId(departureStationId);
            } else {
                throw new IllegalStateException("Station with number: "
                        + requestBody.getDepartureStationNumber() + " can not be found");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (!requestBody.getVehicleNumber().equals(" ")) {
            String urlToVehicleService = vehicleStationServiceUrl.concat("/vehicle/get-id?number=" + requestBody.getVehicleNumber());

            HttpEntity<Void> vehicleNumberHttpEntity = new HttpEntity<>(headers);
            try {
                RestTemplate restTemplate = restTemplateBuilder.build();
                ResponseEntity<Integer> responseEntity = restTemplate.exchange(
                        urlToVehicleService,
                        HttpMethod.GET,
                        vehicleNumberHttpEntity,
                        Integer.class);

                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    String urlToVechieServiceIsSuitable = vehicleStationServiceUrl
                            .concat("/vehicle/is-suitable");

                    IsSuitableRequestEntity isSuitableRequestEntity = new IsSuitableRequestEntity();
                    isSuitableRequestEntity.setArrivalStationId(newOrder.getArrivalStationId());
                    isSuitableRequestEntity.setDepartureStationId(departureStationId);

                    double weight = 0;
                    for (Item item : newOrder.getItems()) {
                        if (item.getWeight() == 0) {
                            throw new IllegalStateException("Weight can't be 0");
                        }

                        weight += item.getWeight();
                    }

                    isSuitableRequestEntity.setWeight(weight);

                    HttpEntity<IsSuitableRequestEntity> vehicleIsSuitableHttpEntity = new HttpEntity<>(isSuitableRequestEntity, headers);
                    try {
                        RestTemplate restTemplateIsSuitable = restTemplateBuilder.build();
                        ResponseEntity<Boolean> responseEntityIsSuitable = restTemplate.exchange(
                                urlToVechieServiceIsSuitable,
                                HttpMethod.GET,
                                vehicleIsSuitableHttpEntity,
                                Boolean.class);

                        if (responseEntityIsSuitable.getStatusCode().is2xxSuccessful()) {
                            if (!responseEntityIsSuitable.getBody()) {
                                throw new IllegalStateException("Vehicle with number: "
                                        + requestBody.getVehicleNumber()
                                        + "cant take this order");
                            }
                        }

                    } catch (Exception e) {
                        System.out.println("Cannot change vehicle status to ready");
                    }

                    newOrder.setVehicleId(responseEntity.getBody());

                    String urlToVehicleServiceGetDroneReady = vehicleStationServiceUrl.concat("/vehicle/get-vehicle-ready?number=" + requestBody.getVehicleNumber());

                    HttpEntity<Void> vehicleReadyHttpEntity = new HttpEntity<>(headers);
                    try {
                        RestTemplate restTemplateVehicleReady = restTemplateBuilder.build();
                        ResponseEntity<String> responseEntityVehicleReady = restTemplate.exchange(
                                urlToVehicleServiceGetDroneReady,
                                HttpMethod.PUT,
                                vehicleReadyHttpEntity,
                                String.class);

                    } catch (Exception e) {
                        System.out.println("Cannot change vehicle status to ready");
                    }

                } else {
                    throw new IllegalStateException("Vehicle with number: "
                            + requestBody.getVehicleNumber() + " can not be found");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        newOrder.setStatus(Status.PROCESSED);

        orderRepository.save(newOrder);
    }

    /**
     * Updates the status of an existing order in the system. This method checks if the order exists
     * and updates its status accordingly.
     *
     * <p>Procedure includes:
     * <ul>
     *     <li>Searching for the order by its identifier in the database.</li>
     *     <li>Updating the order's status if it exists.</li>
     *     <li>Saving the updated order back to the repository.</li>
     * </ul>
     * </p>
     *
     * @param id The unique identifier of the order to be updated.
     * @param status The new status to be set for the order.
     * @throws IllegalStateException If no order with the provided identifier exists.
     */
    public void updateOrderStatus(String id, Status status) {
        Optional<Order> optionalOrder = orderRepository.findById(id);
        if (optionalOrder.isEmpty()) {
            throw new IllegalStateException("Order with id: " + id + " does not exist");
        }

        Order order = optionalOrder.get();
        order.setStatus(status);
        orderRepository.save(order);
    }

    /**
     * Retrieves detailed information about an order associated with a specific vehicle, based on the vehicle's ID.
     * This method ensures that the requester has administrative privileges and that the order is in a 'PROCESSED' state.
     * It also enriches the order details with the station number of the order's arrival station.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Validating admin access using the authorization header.</li>
     *     <li>Finding the order associated with the given vehicle ID and checking its status.</li>
     *     <li>Fetching the arrival station number from an external service and adding it to the response.</li>
     * </ul>
     * </p>
     *
     * @param id The ID of the vehicle associated with the order to be retrieved.
     * @param authHeader The authorization header used to verify administrative access.
     * @return An {@link OrderVehicleGetResponse} containing the order's details, or {@code null} if no such order exists.
     * @throws IllegalStateException If the authorization header is invalid or if the specified station cannot be found.
     * @throws RuntimeException If there is an error during the external API call to fetch station information.
     */
    public OrderVehicleGetResponse getOrderForVehicle(Integer id, String authHeader) {
        if (!isAdmin(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }

        Optional<Order> optionalOrder = orderRepository.findByVehicleIdAndStatusLike(id, Status.PROCESSED);
        if (optionalOrder.isEmpty()) {
            return null;
        }

        Order order = optionalOrder.get();
        OrderVehicleGetResponse orderVehicleGetResponse = new OrderVehicleGetResponse();
        orderVehicleGetResponse.setNumber(order.getNumber());
        orderVehicleGetResponse.setItems(order.getItems());

        String token = authHeader.substring(7);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        String urlToStationService = vehicleStationServiceUrl.concat("/station/get-number?id=" + order.getArrivalStationId());

        HttpEntity<Integer> arrivalStationHttpEntity = new HttpEntity<>(headers);
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    urlToStationService,
                    HttpMethod.GET,
                    arrivalStationHttpEntity,
                    String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                orderVehicleGetResponse.setArrivalStationNumber(responseEntity.getBody());
            } else {
                throw new IllegalStateException("Station with id "
                        + order.getDepartureStationId() + " can not be found");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return orderVehicleGetResponse;
    }

    /**
     * Retrieves all orders associated with the user who is authenticated via the provided authorization header.
     * This method first extracts the user ID by calling an external user service and then fetches all orders linked to this user.
     * Each order is further enriched with details such as the station numbers associated with the order's departure and arrival stations.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Extracting the user ID from the authorization token provided in the header.</li>
     *     <li>Fetching all orders for the identified user from the order repository.</li>
     *     <li>Enriching each order with additional details such as station numbers for arrival and departure stations.</li>
     * </ul>
     * </p>
     *
     * @param authHeader The authorization header containing the Bearer token used for user authentication.
     * @return A list of {@link OrderUserGetResponse} instances containing detailed information about each order.
     * @throws IllegalStateException If there is an issue with the authorization token or if no orders exist for the user.
     * @throws RuntimeException If there is a failure in any external API call required to fetch additional order details.
     */
    public List<OrderUserGetResponse> getOrdersForUser(String authHeader) {
        List<OrderUserGetResponse> orderUserGetResponses = new ArrayList<>();

        String token = authHeader.substring(7);

        String urlToUserService = userServiceUrl.concat("/user/get-userId?token=" + token);

        Integer userId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Integer> responseEntity = restTemplate.exchange(
                    urlToUserService,
                    HttpMethod.GET,
                    entity,
                    Integer.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                userId = responseEntity.getBody();

            } else {
                throw new IllegalStateException("Something went wrong");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Optional<List<Order>> optionalOrder = orderRepository.findAllByUserId(userId);
        if (optionalOrder.isEmpty()) {
            throw new IllegalStateException("Orders for user: " + token + " does not exist");
        }

        List<Order> orders = optionalOrder.get();
        for (Order order : orders) {
            OrderUserGetResponse orderUserGetResponse = new OrderUserGetResponse();
            orderUserGetResponse.setId(order.getId());
            orderUserGetResponse.setNumber(order.getNumber());
            orderUserGetResponse.setItems(order.getItems());
            orderUserGetResponse.setReceiptCode(order.getReceiptCode());
            orderUserGetResponse.setCreationDate(order.getCreationDate().atZone(ZoneOffset.UTC));
            orderUserGetResponse.setStatus(order.getStatus());

            String urlToStationService = vehicleStationServiceUrl.concat("/station/get-number?id=");

            HttpEntity<Integer> arrivalStationHttpEntity = new HttpEntity<>(order.getArrivalStationId(), headers);
            try {
                RestTemplate restTemplate = restTemplateBuilder.build();
                ResponseEntity<String> responseEntity = restTemplate.exchange(
                        urlToStationService.concat(String.valueOf(order.getArrivalStationId())),
                        HttpMethod.GET,
                        arrivalStationHttpEntity,
                        String.class);

                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    orderUserGetResponse.setArrivalStationNumber(responseEntity.getBody());
                } else {
                    throw new IllegalStateException("Station with id "
                            + order.getDepartureStationId() + " can not be found");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (order.getDepartureStationId() != null) {
                HttpEntity<Void> DepartureStationHttpEntity = new HttpEntity<>(headers);
                try {
                    RestTemplate restTemplate = restTemplateBuilder.build();
                    ResponseEntity<String> responseEntity = restTemplate.exchange(
                            urlToStationService.concat(String.valueOf(order.getDepartureStationId())),
                            HttpMethod.GET,
                            DepartureStationHttpEntity,
                            String.class);

                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        orderUserGetResponse.setDepartureStationNumber(responseEntity.getBody());
                    } else {
                        throw new IllegalStateException("Station with id "
                                + order.getDepartureStationId() + " can not be found");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                orderUserGetResponse.setDepartureStationNumber(null);
            }

            orderUserGetResponses.add(orderUserGetResponse);
        }

        return orderUserGetResponses;

    }

    /**
     * Determines if the bearer of the token in the authorization header has administrative privileges.
     *
     * @param authHeader The authorization header containing the bearer token.
     * @return A Boolean value indicating whether the token bearer is an admin. Returns {@code false} if the header is null or empty.
     * @throws IllegalStateException If the user service indicates something went wrong during the admin status check.
     * @throws RuntimeException If there's an issue with the network connection or server during the API call.
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
     * Checks if the bearer of the token in the authorization header is a regular user.
     *
     * @param authHeader The authorization header containing the bearer token.
     * @return A Boolean value indicating whether the token bearer is a regular user. Returns {@code false} if the header is null or empty.
     * @throws IllegalStateException If the user service indicates something went wrong during the user status check.
     * @throws RuntimeException If there's an issue with the network connection or server during the API call.
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
     * Validates if the bearer of the token in the authorization header has a valid session.
     *
     * @param authHeader The authorization header containing the bearer token.
     * @return A Boolean value indicating the validity of the session. Returns {@code false} if the header is null or empty.
     * @throws IllegalStateException If the user service indicates something went wrong during the validity check.
     * @throws RuntimeException If there's an issue with the network connection or server during the API call.
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
