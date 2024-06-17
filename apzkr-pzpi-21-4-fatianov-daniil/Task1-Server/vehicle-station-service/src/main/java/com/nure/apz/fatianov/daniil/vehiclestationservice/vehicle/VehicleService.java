package com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nure.apz.fatianov.daniil.vehiclestationservice.station.Station;
import com.nure.apz.fatianov.daniil.vehiclestationservice.station.StationRepository;
import com.nure.apz.fatianov.daniil.vehiclestationservice.station.object.Point;
import com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.request.IsSuitableRequest;
import com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.request.VehicleAddRequest;
import com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.request.VehicleChangeRequest;
import com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.response.VehicleGetAllResponseEntity;
import com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.response.objects.Order;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private static final Double BUFFER_RADIUS = 0.009;// буферний радіус 1км
    private static final int MARGIN_DISTANCE = 10;// дистанція відхилення від
    private static final double EPS = 0.00018; // радіус для 20 метрів
    private static final int MIN_POINTS = 2; // мінімальна кількість точок у кластері
    private static final double MIN_NEIGHBOUR_DISTANCE = 0.5;// мінімальна дистанція до сусідньої точки в км

    private static final String orderServiceUrl = "http://localhost:8083/order-service";// посилання на сервіс заказів
    private static final String userServiceUrl = "http://localhost:8082/user-service";// посилання на сервіс юзерів


    private final VehicleRepository vehicleRepository;

    private final StationRepository stationRepository;

    private final RestTemplateBuilder restTemplateBuilder;

    /**
     * Adds a new vehicle to the system based on the specifications provided in the request body.
     * This method verifies that the requester has administrative privileges and checks if the specified station exists in the database.
     * It then creates a new vehicle, assigns it a unique sequential number, and sets its attributes such as lifting capacity,
     * flight distance, and availability status.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Verifying administrative access using the authorization header.</li>
     *     <li>Checking the existence of the station where the vehicle will be located.</li>
     *     <li>Creating and saving a new vehicle with the provided specifications.</li>
     * </ul>
     * </p>
     *
     * @param requestBody The {@link VehicleAddRequest} containing details about the new vehicle, including station number,
     *                    lifting capacity, and flight distance.
     * @param authHeader The authorization header used to verify administrative access.
     * @throws IllegalStateException If the authorization header is invalid, or if the specified station does not exist.
     */
    public void addVehicle(VehicleAddRequest requestBody, String authHeader) {
        if (!isAdmin(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }

        Optional<Station> optionalStation = stationRepository.findByNumber(requestBody.getStationNumber());

        if (optionalStation.isEmpty()) {
            throw new IllegalStateException("Station with number: "
                    + requestBody.getStationNumber() + "does not exist");
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setNumber("VEH" + vehicleRepository.getNextSequenceValue());
        vehicle.setLiftingCapacity(requestBody.getLiftingCapacity());
        vehicle.setFlightDistance(requestBody.getFlightDistance());
        vehicle.setStatus(Status.AVAILABLE);
        vehicle.setStation(optionalStation.get());

        vehicleRepository.saveAndFlush(vehicle);
    }

    /**
     * Retrieves all vehicles from the system with detailed information about each one.
     * This method checks if the requester has administrative privileges. It then fetches all vehicles from the repository
     * and enriches each vehicle entity with additional data, such as associated orders.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Verifying administrative access using the authorization header.</li>
     *     <li>Fetching all vehicles from the vehicle repository.</li>
     *     <li>Enriching each vehicle with its associated order, if available.</li>
     * </ul>
     * </p>
     *
     * @param authHeader The authorization header used to verify administrative access.
     * @return A list of {@link VehicleGetAllResponseEntity} containing detailed information about each vehicle.
     * @throws IllegalStateException If the authorization header is invalid.
     * @throws RuntimeException If there is an error during an external API call to fetch order information for the vehicles.
     */
    public List<VehicleGetAllResponseEntity> getAllVehicles(String authHeader) {
        if (!isAdmin(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }

        List<Vehicle> vehicles = vehicleRepository.findAll();
        List<VehicleGetAllResponseEntity> vehicleGetAllResponseEntities = new ArrayList<>();
        for (Vehicle vehicle : vehicles) {
            VehicleGetAllResponseEntity vehicleGetAllResponseEntity = new VehicleGetAllResponseEntity();
            vehicleGetAllResponseEntity.setId(vehicle.getId());
            vehicleGetAllResponseEntity.setNumber(vehicle.getNumber());
            vehicleGetAllResponseEntity.setLiftingCapacity(vehicle.getLiftingCapacity());
            vehicleGetAllResponseEntity.setFlightDistance(vehicle.getFlightDistance());
            vehicleGetAllResponseEntity.setStatus(vehicle.getStatus());
            vehicleGetAllResponseEntity.setStationNumber(vehicle.getStation().getNumber());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<Void> orderHttpEntity = new HttpEntity<>(headers);
            String urlToOrderService = orderServiceUrl.concat("/get-order-for-vehicle?droneId=" + vehicle.getId());
            try {
                RestTemplate restTemplate = restTemplateBuilder.build();
                ResponseEntity<Order> responseEntity = restTemplate.exchange(
                        urlToOrderService,
                        HttpMethod.GET,
                        orderHttpEntity,
                        Order.class
                );

                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    vehicleGetAllResponseEntity.setOrder(responseEntity.getBody());
                } else {
                    vehicleGetAllResponseEntity.setOrder(null);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            vehicleGetAllResponseEntities.add(vehicleGetAllResponseEntity);
        }
        return vehicleGetAllResponseEntities;
    }

    /**
     * Retrieves the vehicle number based on a given vehicle ID.
     * This method validates that the requester has administrative privileges before attempting to fetch the vehicle number.
     * It checks if the specified vehicle ID exists in the repository.
     *
     * <p>Procedure includes:
     * <ul>
     *     <li>Validating that the requester is an administrator using the provided authorization header.</li>
     *     <li>Checking if the vehicle with the specified ID exists in the vehicle repository.</li>
     *     <li>Returning the vehicle number if the vehicle exists.</li>
     * </ul>
     * </p>
     *
     * @param id The ID of the vehicle for which the number is being requested.
     * @param authHeader The authorization header used to verify administrative access.
     * @return The vehicle number as a {@link String} or {@code null} if the vehicle ID is null.
     * @throws IllegalStateException If the authorization header is invalid, or if no vehicle with the provided ID exists.
     */
    public String getVehicleNumber(Integer id, String authHeader) {
        if (!isAdmin(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }

        if (id == null) {
            return null;
        }

        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
        if (optionalVehicle.isEmpty()) {
            throw new IllegalStateException("Vehicle with id: " + id + " does not exist");
        }
        return optionalVehicle.get().getNumber();
    }

    /**
     * Updates the details of an existing vehicle in the database.
     * This method first checks if the requester has administrative rights based on the provided authorization header.
     * It then validates the existence of the vehicle specified by the ID in the request body.
     * If the vehicle exists, it updates its number, lifting capacity, and flight distance as provided in the request body.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Verifying administrative privileges using the authorization header.</li>
     *     <li>Fetching the vehicle from the repository using the ID provided in the request.</li>
     *     <li>Updating the vehicle's details in the repository and saving the changes.</li>
     * </ul>
     * </p>
     *
     * @param requestBody The {@link VehicleChangeRequest} containing the new details for the vehicle.
     * @param authHeader The authorization header used to verify administrative access.
     * @throws IllegalStateException If the authorization header is invalid or if no vehicle with the provided ID exists.
     */
    public void changeVehicle(VehicleChangeRequest requestBody, String authHeader) {
        if (!isAdmin(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }

        Optional<Vehicle> optionalVehicle = vehicleRepository.findById(requestBody.getId());

        if (optionalVehicle.isEmpty()) {
            throw new IllegalStateException("Vehicle with id: " + requestBody.getId() + " does not exist");
        }

        Vehicle vehicle = optionalVehicle.get();
        vehicle.setNumber(requestBody.getNumber());
        vehicle.setLiftingCapacity(requestBody.getLiftingCapacity());
        vehicle.setFlightDistance(requestBody.getFlightDistance());

        vehicleRepository.saveAndFlush(vehicle);
    }

    /**
     * Sets the status of a specific vehicle to 'READY', indicating it is prepared for operations.
     * This method checks if the requester has administrative privileges using the provided authorization header.
     * It then looks up the vehicle by its unique number and updates its status in the database.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Verifying administrative privileges using the authorization header.</li>
     *     <li>Searching for the vehicle by its number in the repository.</li>
     *     <li>Updating the vehicle's status to 'READY' and saving the changes to the database.</li>
     * </ul>
     * </p>
     *
     * @param number The number of the vehicle to be updated.
     * @param authHeader The authorization header used to verify administrative access.
     * @throws IllegalStateException If the authorization header is invalid, or if no vehicle with the provided number exists.
     */
    public void getVehicleReady(String number, String authHeader) {
        if (!isAdmin(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }

        Optional<Vehicle> optionalVehicle = vehicleRepository.findByNumber(number);
        if (optionalVehicle.isEmpty()) {
            throw new IllegalStateException("Vehicle with number: " + number + " does not exist");
        }

        Vehicle vehicle = optionalVehicle.get();
        vehicle.setStatus(Status.READY);
        vehicleRepository.saveAndFlush(vehicle);
    }

    /**
     * Deletes a vehicle from the system based on the specified vehicle number.
     * This method first verifies that the requester has administrative privileges.
     * It then checks if the vehicle exists and whether it is currently assigned to any orders.
     * If the vehicle is not assigned to any orders, it proceeds to delete the vehicle from the repository.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Verifying administrative privileges using the authorization header.</li>
     *     <li>Finding the vehicle by its number and checking if it is linked to any active orders.</li>
     *     <li>Deleting the vehicle if no active orders are found.</li>
     * </ul>
     * </p>
     *
     * @param number The unique number of the vehicle to be deleted.
     * @param authHeader The authorization header used to verify administrative access.
     * @throws IllegalStateException If the authorization header is invalid, if the vehicle does not exist, or if the vehicle is linked to an order.
     * @throws RuntimeException If there is an error during the external API call to check for associated orders.
     */
    public void deleteVehicle(String number, String authHeader) {
        if (!isAdmin(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }

        Optional<Vehicle> optionalVehicle = vehicleRepository.findByNumber(number);
        if (optionalVehicle.isEmpty()) {
            throw new IllegalStateException("Vehicle with number: " + number + " does not exist");
        }

        Vehicle vehicle = optionalVehicle.get();

        String urlToOrderService = orderServiceUrl.concat("/get-orders-for-vehicle?id=" + vehicle.getId());
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Order> responseEntity = restTemplate.exchange(
                    urlToOrderService,
                    HttpMethod.GET,
                    null,
                    Order.class
            );

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                Order order = responseEntity.getBody();
                if (order != null) {
                    throw new IllegalStateException("Vehicle with number: " + number + " has order");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        vehicleRepository.delete(vehicle);
    }

    /**
     * Retrieves the unique identifier (ID) of a vehicle based on its number.
     * This method first verifies that the requester has administrative privileges using the provided authorization header.
     * It then checks if a vehicle with the specified number exists in the vehicle repository.
     * If the vehicle exists, the method returns its ID.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Verifying administrative privileges using the authorization header.</li>
     *     <li>Searching for the vehicle by its number in the repository.</li>
     *     <li>Returning the vehicle's ID if found.</li>
     * </ul>
     * </p>
     *
     * @param number The number of the vehicle for which the ID is requested.
     * @param authHeader The authorization header used to verify administrative access.
     * @return The ID of the vehicle as an {@link Integer}.
     * @throws IllegalStateException If the authorization header is invalid, or if no vehicle with the provided number exists.
     */
    public Integer getVehicleId(String number, String authHeader) {
        if (!isAdmin(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }
        Optional<Vehicle> optionalVehicle = vehicleRepository.findByNumber(number);
        if (optionalVehicle.isEmpty()) {
            throw new IllegalStateException("Vehicle with number: " + number + " does not exist");
        }

        return optionalVehicle.get().getId();
    }

    /**
     * Determines if a specified vehicle is suitable for a transport request based on its weight capacity and flight distance.
     * This method verifies that the requester has administrative privileges and checks the vehicle's capacity against the request's weight,
     * as well as the flight distance against the computed distance between departure and arrival stations.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Verifying administrative privileges using the authorization header.</li>
     *     <li>Validating the existence of the vehicle and the specified stations.</li>
     *     <li>Checking if the vehicle's lifting capacity and flight distance meet the requirements of the transport request.</li>
     * </ul>
     * </p>
     *
     * @param request The {@link IsSuitableRequest} containing the vehicle number, weight of the cargo, and IDs of the departure and arrival stations.
     * @param authHeader The authorization header used to verify administrative access.
     * @return A Boolean indicating whether the vehicle is suitable for the request. Returns {@code true} if suitable, otherwise throws an exception.
     * @throws IllegalStateException If the authorization header is invalid, if the vehicle or stations do not exist, or if the vehicle cannot handle the weight or distance required.
     */
    public Boolean isSuitable(IsSuitableRequest request, String authHeader) {
        if (!isAdmin(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }

        Optional<Vehicle> optionalVehicle = vehicleRepository.findByNumber(request.getVehicleNumber());

        if (optionalVehicle.isEmpty()) {
            throw new IllegalStateException("Vehicle with number: " + request.getVehicleNumber() + " does not exist");
        }

        Vehicle vehicle = optionalVehicle.get();

        if (request.getWeight() > vehicle.getLiftingCapacity()) {
            throw new IllegalStateException("Vehicle with number: "
                    + request.getVehicleNumber() + " is over capacity");
        }

        Optional<Station> optionalArrivalStation = stationRepository.findById(request.getArrivalStationId());
        if (optionalArrivalStation.isEmpty()) {
            throw new IllegalStateException("Arrival station with id: " + request.getArrivalStationId() + " does not exist");
        }

        Optional<Station> optionalDepartureStation = stationRepository.findById(request.getDepartureStationId());
        if (optionalDepartureStation.isEmpty()) {
            throw new IllegalStateException("Arrival station with id: " + request.getArrivalStationId() + " does not exist");
        }

        Station arrivalStation = optionalArrivalStation.get();
        Station departureStation = optionalDepartureStation.get();

        double distance = distance(
                departureStation.getLatitude(),
                departureStation.getLongitude(),
                arrivalStation.getLatitude(),
                arrivalStation.getLongitude()
        );

        if (distance > vehicle.getFlightDistance() - MARGIN_DISTANCE) {
            throw new IllegalStateException("Vehicle with id: "
                    + request.getVehicleNumber()
                    + " and flight distance: " + vehicle.getFlightDistance()
                    + " can't reach distance: " + distance);
        }

        return true;
    }

    /**
     * Calculates the optimal route for a vehicle to travel from its current station to a destination station.
     * This method ensures the user has administrative privileges, fetches the order associated with the vehicle,
     * and verifies the details of the order and the existence of the arrival station. It then calculates the route using
     * geographic data and pathfinding algorithms.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Verifying administrative privileges using the provided authorization header.</li>
     *     <li>Fetching the order associated with the vehicle and validating the order details.</li>
     *     <li>Fetching geographic coordinates for the departure and arrival stations.</li>
     *     <li>Calculating the shortest path between the two stations using loaded geographic data.</li>
     * </ul>
     * </p>
     *
     * @param number The number of the vehicle for which the route needs to be determined.
     * @param authHeader The authorization header used to verify administrative access.
     * @return A list of {@link Point} objects representing the calculated route.
     * @throws IllegalStateException If the authorization header is invalid, the vehicle or station does not exist, or the order is incorrect.
     * @throws RuntimeException If there is an error in fetching data or calculating the path due to external API or internal logic failure.
     */
    public List<Point> sendVehicle(String number, String authHeader) {
        if (!isAdmin(authHeader)) {
            throw new IllegalStateException("Invalid auth header");
        }

        Optional<Vehicle> optionalVehicle = vehicleRepository.findByNumber(number);
        if (optionalVehicle.isEmpty()) {
            throw new IllegalStateException("Vehicle with number: " + number + " does not exist");
        }

        Vehicle vehicle = optionalVehicle.get();
        String token = authHeader.substring(7);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> orderHttpEntity = new HttpEntity<>(headers);

        String urlToOrderService = orderServiceUrl.concat("/get-order-for-vehicle?droneId=" + vehicle.getId());
        Order order = new Order();
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            ResponseEntity<Order> responseEntity = restTemplate.exchange(
                    urlToOrderService,
                    HttpMethod.GET,
                    orderHttpEntity,
                    Order.class
            );

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                order = responseEntity.getBody();
                if (!Order.orderHasDetails(order)) {
                    throw new IllegalStateException("Cannot send drone: " + number + "because wrong order");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Station deptStation = vehicle.getStation();

        Optional<Station> optionalStation = stationRepository.findByNumber(order.getArrivalStationNumber());
        if (optionalStation.isEmpty()) {
            throw new IllegalStateException("Station with number: " + order.getArrivalStationNumber() + " does not exist");
        }

        Station arvlStation = optionalStation.get();

        Double startLat, startLon, endLat, endLon;
        startLat = deptStation.getLatitude();
        startLon = deptStation.getLongitude();
        endLat = arvlStation.getLatitude();
        endLon = arvlStation.getLongitude();

        double minLat = Math.min(startLat, endLat) - BUFFER_RADIUS;
        double maxLat = Math.max(startLat, endLat) + BUFFER_RADIUS;
        double minLon = Math.min(startLon, endLon) - BUFFER_RADIUS;
        double maxLon = Math.max(startLon, endLon) + BUFFER_RADIUS;

        Set<Point> crossings;
        try {
            crossings = loadCrossingsFromOpenStreetMap(minLat, minLon, maxLat, maxLon);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Point start = new Point(startLat, startLon);
        Point end = new Point(endLat, endLon);

        //TODO додати запит на конкретний дрон для завантаження шляху
        return findShortestPath(start, end, crossings);
    }

    /**
     * Clusters a set of geographical coordinates using the DBSCAN clustering algorithm.
     * This method converts each coordinate into a double point (longitude, latitude) and applies DBSCAN to find clusters.
     * It computes the mean latitude and longitude for the points in each cluster to create new, representative points.
     * Points that are not part of any clusters are included individually.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Transforming coordinates into double points for clustering.</li>
     *     <li>Applying the DBSCAN clustering algorithm to the points.</li>
     *     <li>Calculating the centroid for each cluster to represent the cluster by a single point.</li>
     *     <li>Incorporating any unclustered points directly into the result.</li>
     * </ul>
     * </p>
     *
     * @param coordinates A set of {@link Coordinate} objects representing geographical locations.
     * @return A set of {@link Point} objects representing the clustered and/or individual points.
     * @see DBSCANClusterer The clustering algorithm used to group points.
     */
    private Set<Point> clusterPoints(Set<Coordinate> coordinates) {
        List<DoublePoint> doublePoints = new ArrayList<>();
        for (Coordinate coordinate : coordinates) {
            doublePoints.add(new DoublePoint(new double[]{coordinate.y, coordinate.x}));
        }

        DBSCANClusterer<DoublePoint> clusterer = new DBSCANClusterer<>(EPS, MIN_POINTS);
        List<Cluster<DoublePoint>> clusters = clusterer.cluster(doublePoints);

        Set<Point> clusteredPoints = new HashSet<>();
        Set<DoublePoint> clusteredDoublePoints = new HashSet<>();

        for (Cluster<DoublePoint> cluster : clusters) {
            double sumLat = 0, sumLon = 0;
            for (DoublePoint doublePoint : cluster.getPoints()) {
                double[] values = doublePoint.getPoint();
                sumLat += values[0];
                sumLon += values[1];
                clusteredDoublePoints.add(doublePoint);
            }
            double meanLat = sumLat / cluster.getPoints().size();
            double meanLon = sumLon / cluster.getPoints().size();
            clusteredPoints.add(new Point(meanLat, meanLon));
        }

        for (DoublePoint doublePoint : doublePoints) {
            if (!clusteredDoublePoints.contains(doublePoint)) {
                double[] values = doublePoint.getPoint();
                clusteredPoints.add(new Point(values[0], values[1]));
            }
        }

        return clusteredPoints;
    }

    /**
     * Retrieves and processes crossing and turning circle data from OpenStreetMap (OSM) within specified geographic boundaries.
     * This method sends a query to the OSM Overpass API to fetch nodes tagged as "crossing" or "turning_circle" within the given boundaries.
     * It then clusters the returned points to minimize data and simplify further processing.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Constructing and sending a query to the OSM Overpass API with specified geographic boundaries.</li>
     *     <li>Parsing the JSON response to extract latitude and longitude coordinates of relevant geographic points.</li>
     *     <li>Clustering the geographic points to reduce data complexity and improve manageability.</li>
     * </ul>
     * </p>
     *
     * @param minLat The minimum latitude of the bounding box.
     * @param minLon The minimum longitude of the bounding box.
     * @param maxLat The maximum latitude of the bounding box.
     * @param maxLon The maximum longitude of the bounding box.
     * @return A set of {@link Point} objects representing clustered geographic points of crossings and turning circles.
     * @throws Exception If there is an error in fetching or processing the data from the API.
     */
    public Set<Point> loadCrossingsFromOpenStreetMap(double minLat, double minLon, double maxLat,  double maxLon) throws Exception {
        String url = "https://overpass-api.de/api/interpreter";
        String data = "[out:json];" +
                "(node[\"highway\"=\"crossing\"](" + minLat + "," + minLon  + "," + maxLat + "," + maxLon + ");" +
                "node[\"highway\"=\"turning_circle\"](" + minLat + "," + minLon  + "," + maxLat + "," + maxLon + "););" +
                "out;";
        String fullUrl = url + "?data=" + data;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);

        Set<Coordinate> crossings = new HashSet<>();

        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);
            JsonNode elements = root.path("elements");

            for (JsonNode element : elements) {
                double lat = element.get("lat").asDouble();
                double lon = element.get("lon").asDouble();
                crossings.add(new Coordinate(lon, lat));
            }
        } else {
            throw new RuntimeException("Failed to fetch crossings data");
        }

        Set<Point> clusteredCrossings = clusterPoints(crossings);

        return clusteredCrossings;
    }

    /**
     * Calculates the shortest path between two points using a set of possible crossing points.
     * This method utilizes the A* search algorithm, which is enhanced with a priority queue to efficiently find the path
     * with the lowest estimated cost to the destination. The method operates on a set of geographical points representing crossings,
     * including the start and end points as part of the pathfinding process.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Initializing the priority queue with the start point based on estimated distance to the end point.</li>
     *     <li>Continuously exploring the nearest unvisited point until the end point is reached or no paths are left to explore.</li>
     *     <li>Calculating tentative distances to neighboring points and updating the path if a shorter path is found.</li>
     *     <li>Reconstructing the path from end to start once the destination is reached.</li>
     * </ul>
     * </p>
     *
     * @param start The starting point of the path.
     * @param end The destination point of the path.
     * @param crossings A set of points representing possible waypoints or crossings on the path.
     * @return A list of {@link Point} objects representing the shortest path from start to end.
     *         Returns an empty list if no path is found.
     */
    private List<Point> findShortestPath(Point start, Point end, Set<Point> crossings) {
        PriorityQueue<Point> openSet = new PriorityQueue<>((a, b) -> {
            double distA = distance(a, end);
            double distB = distance(b, end);
            return Double.compare(distA, distB);
        });
        openSet.offer(start);
        Set<Point> closedSet = new HashSet<>();
        Map<Point, Point> cameFrom = new HashMap<>();
        Map<Point, Double> gScore = new HashMap<>();

        gScore.put(start, 0.0);

        crossings.add(end);
        while (!openSet.isEmpty()) {
            Point current = openSet.poll();
            if (current.equals(end)) {
                return reconstructPath(cameFrom, current);
            }
            closedSet.add(current);
            for (Point neighbor : getNeighbors(current, crossings)) {
                if (closedSet.contains(neighbor)) {
                    continue;
                }
                double tentativeScore = gScore.getOrDefault(current, Double.MAX_VALUE) + distance(current, neighbor);
                if (!gScore.containsKey(neighbor) || tentativeScore < gScore.get(neighbor)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeScore);
                    openSet.offer(neighbor);
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Identifies and returns the neighboring points of a given point from a set of crossings, based on a specified minimum distance.
     * This method calculates the Euclidean distance between the given point and each point in the set of crossings.
     * It includes a point as a neighbor if it is within the minimum neighbor distance and is not the point itself.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Iterating through a set of geographical points (crossings).</li>
     *     <li>Calculating the distance between the given point and each crossing.</li>
     *     <li>Selecting crossings that are within a defined proximity threshold as neighbors.</li>
     * </ul>
     * </p>
     *
     * @param point The point for which neighbors are to be identified.
     * @param crossings A set of points representing potential neighbors.
     * @return A list of {@link Point} objects that are within the minimum neighbor distance from the specified point.
     */
    private List<Point> getNeighbors(Point point, Set<Point> crossings) {
        List<Point> neighbors = new ArrayList<>();
        for (Point crossing : crossings) {
            double dist = distance(point, crossing);
            if (!crossing.equals(point) && dist < MIN_NEIGHBOUR_DISTANCE) {
                neighbors.add(crossing);
            }
        }
        return neighbors;
    }

    /**
     * Reconstructs a path by tracing back from an endpoint to a start point using a map of point connections.
     * This method starts from a specified endpoint and traces back through the map of 'cameFrom' points until
     * it reaches the starting point of the path. It effectively builds the path from end to start, then reverses
     * it to present the path from start to end.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Starting from the endpoint and iterating backward through the map until the start point is reached.</li>
     *     <li>Accumulating points into a list which initially constructs the path in reverse order.</li>
     *     <li>Reversing the accumulated list to present the path in correct start-to-end order.</li>
     * </ul>
     * </p>
     *
     * @param cameFrom A map where each key is a point and its value is the point from which it came in the path.
     * @param current The endpoint of the path from which to start reconstructing the path backwards.
     * @return A list of {@link Point} objects representing the path from start to end.
     */
    private List<Point> reconstructPath(Map<Point, Point> cameFrom, Point current) {
        List<Point> path = new ArrayList<>();
        path.add(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(current);
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Calculates the geodesic distance between two points specified by latitude and longitude using the Haversine formula.
     * This method converts latitude and longitude from degrees to radians and then computes the distance based on the curvature of the Earth.
     *
     * <p>The Haversine formula is particularly useful in navigation for calculating the shortest distance between two points on the surface of a sphere.</p>
     *
     * @param p1 The first point with geographic coordinates (latitude, longitude).
     * @param p2 The second point with geographic coordinates (latitude, longitude).
     * @return The distance between the two points in kilometers.
     */
    private double distance(Point p1, Point p2) {
        double lat1 = Math.toRadians(p1.getLatitude());
        double lon1 = Math.toRadians(p1.getLongitude());
        double lat2 = Math.toRadians(p2.getLatitude());
        double lon2 = Math.toRadians(p2.getLongitude());

        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double r = 6371; // Радіус Землі в кілометрах
        return c * r;
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;

        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double r = 6371;

        return c * r;
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
     * Verifies if the bearer of the token in the authorization header is recognized as a user in the system.
     * This method queries a user service endpoint to validate the user status of the token bearer.
     *
     * <p>Key operations include:
     * <ul>
     *     <li>Checking if the authorization header is non-empty and valid.</li>
     *     <li>Extracting the token from the header and sending it to the user service for verification.</li>
     *     <li>Interpreting the response to determine if the bearer is a registered user.</li>
     * </ul>
     * </p>
     *
     * @param authHeader The authorization header containing the bearer token.
     * @return A Boolean indicating whether the bearer is a registered user ({@code true}) or not ({@code false}).
     * @throws IllegalStateException If there is an error response from the user service, indicating that the verification failed.
     * @throws RuntimeException If there are issues with network connectivity or server responses during the API call.
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
