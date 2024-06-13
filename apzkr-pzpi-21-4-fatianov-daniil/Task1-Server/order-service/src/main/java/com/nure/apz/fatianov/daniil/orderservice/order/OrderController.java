package com.nure.apz.fatianov.daniil.orderservice.order;

import com.nure.apz.fatianov.daniil.orderservice.model.OrderModel;
import com.nure.apz.fatianov.daniil.orderservice.request.OrderAddRequestBody;
import com.nure.apz.fatianov.daniil.orderservice.request.OrderChangeRequestBody;
import com.nure.apz.fatianov.daniil.orderservice.request.OrderProcessRequestBody;
import com.nure.apz.fatianov.daniil.orderservice.request.OrderSendRequestBody;
import com.nure.apz.fatianov.daniil.orderservice.response.OrderAdminGetResponseEntity;
import com.nure.apz.fatianov.daniil.orderservice.response.OrderUserGetResponse;
import com.nure.apz.fatianov.daniil.orderservice.response.OrderVehicleGetResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/order-service")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Add an order",
            description = "Adds a new order to the system. Requires Bearer Authentication for access.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order added successfully"),
                    @ApiResponse(responseCode = "500", description = "Failed to add order due to an internal server error")
            })
    @PostMapping("/add")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> addOrder(
            @RequestBody OrderAddRequestBody requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            orderService.addOrder(requestBody, authHeader);
            return ResponseEntity.ok().body("Order added successfully");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add order: " + e.getMessage());
        }
    }

    @Operation(summary = "Get all orders",
            description = "Retrieves all orders from the system. Requires Bearer Authentication for access.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "All orders retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Failed to retrieve orders due to internal server error")
            })
    @GetMapping("/get-all")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<OrderAdminGetResponseEntity>> getAll(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            return ResponseEntity.ok(orderService.getAll(authHeader));
        } catch (Exception e) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Change order details",
            description = "Changes details of an existing order. Requires Bearer Authentication for access.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order processed successfully"),
                    @ApiResponse(responseCode = "500", description = "Failed to change order due to internal server error")
            })
    @PutMapping("/change")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> changeOrder(
            @RequestBody OrderChangeRequestBody requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            orderService.changeOrder(requestBody, authHeader);
            return ResponseEntity.ok().body("Order processed successfully");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to change order: " + e.getMessage());
        }
    }

    @Operation(summary = "Process an order",
            description = "Processes an existing order. Requires Bearer Authentication for access.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order changed successfully"),
                    @ApiResponse(responseCode = "500", description = "Failed to change order due to internal server error")
            })
    @PutMapping("/process")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> processOrder(
            @RequestBody OrderProcessRequestBody requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            orderService.processOrder(requestBody, authHeader);
            return ResponseEntity.ok().body("Order changed successfully");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to change order: " + e.getMessage());
        }
    }

    @Operation(summary = "Update order status",
            description = "Updates the status of an existing order.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
                    @ApiResponse(responseCode = "500", description = "Failed to update order status due to internal server error")
            })
    @PutMapping("/update-status")
    public ResponseEntity<String> updateOrderStatus(
            @RequestParam String id,
            @RequestParam Status status
    ) {
        try {
            orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok().body("Order status updated successfully");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update order status: " + e.getMessage());
        }
    }

    @Operation(summary = "Get all order statuses",
            description = "Retrieves all possible statuses of orders from the system.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "All order statuses retrieved successfully")
            })
    @GetMapping("/statuses")
    public ResponseEntity<Status[]> getAllStatuses() {
        Status[] allStatuses = Status.values();
        return ResponseEntity.ok(allStatuses);
    }

    @Operation(summary = "Get order for a vehicle",
            description = "Retrieves the order assigned to a specific vehicle based on the drone ID. Requires Bearer Authentication for access.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order for the vehicle retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Failed to retrieve order due to internal server error")
            })
    @GetMapping("/get-order-for-vehicle")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<OrderVehicleGetResponse> getOrderForVehicle(
            @RequestParam Integer droneId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            return ResponseEntity.ok(orderService.getOrderForVehicle(droneId, authHeader));
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get orders for a user",
            description = "Retrieves all orders associated with a user based on their authentication token. Requires Bearer Authentication for access.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "All orders for the user retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Failed to retrieve orders due to internal server error")
            })
    @GetMapping("/get-orders-for-user")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<OrderUserGetResponse>> getOrderForUser(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
    ) {
        try {
            return ResponseEntity.ok(orderService.getOrdersForUser(token));
        } catch (Exception e) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Ping test",
            description = "Simple ping operation to test the availability and responsiveness of the service. Returns a static response to indicate the service is operational.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Service is available and responsive")
            })
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
