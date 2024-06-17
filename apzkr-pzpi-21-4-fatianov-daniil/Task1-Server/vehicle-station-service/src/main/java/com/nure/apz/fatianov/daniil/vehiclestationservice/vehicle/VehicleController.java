package com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle;

import com.nure.apz.fatianov.daniil.vehiclestationservice.station.object.Point;
import com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.request.IsSuitableRequest;
import com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.request.VehicleAddRequest;
import com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.request.VehicleChangeRequest;
import com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.response.VehicleGetAllResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/vehicle-station-service/vehicle")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @Operation(summary = "Add a new vehicle",
            description = "Adds a new vehicle to the system database.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Vehicle added successfully"),
                    @ApiResponse(responseCode = "400", description = "Error occurred during adding vehicle")
            })
    @PostMapping("/add")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> addVehicle(
            @RequestBody VehicleAddRequest requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            vehicleService.addVehicle(requestBody, authHeader);
            return ResponseEntity.ok().body("Vehicle added successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Get all vehicles",
            description = "Retrieves all vehicles from the system.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of vehicles retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Error occurred during fetching vehicles")
            })
    @GetMapping("/get-all")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<VehicleGetAllResponseEntity>> getAllVehicles(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            return ResponseEntity.ok().body(vehicleService.getAllVehicles(authHeader));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }
    }

    @Operation(summary = "Get vehicle number by ID",
            description = "Fetches the vehicle number based on the vehicle ID provided.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Vehicle number retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Vehicle number could not be retrieved")
            })
    @GetMapping("/get-number")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> getVehicleNumber(
            @RequestParam Integer id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            return ResponseEntity.ok().body(vehicleService.getVehicleNumber(id, authHeader));
        } catch (Exception e) {
            return ResponseEntity.ok().body(null);
        }
    }

    @Operation(summary = "Get vehicle ID by number",
            description = "Retrieves the ID of a vehicle based on the vehicle number provided.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Vehicle ID retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Vehicle not found or error occurred")
            })
    @GetMapping("/get-id")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Integer> getVehicleId(
            @RequestParam String number,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            return ResponseEntity.ok().body(vehicleService.getVehicleId(number, authHeader));
        } catch (Exception e) {
            return ResponseEntity.ok().body(null);
        }
    }

    @Operation(summary = "Change vehicle details",
            description = "Updates the details of an existing vehicle.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Vehicle changed successfully"),
                    @ApiResponse(responseCode = "400", description = "Error occurred during updating vehicle")
            })
    @PutMapping("/change")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> changeVehicle(
            @RequestBody VehicleChangeRequest requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            vehicleService.changeVehicle(requestBody, authHeader);
            return ResponseEntity.ok().body("Vehicle changed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Set vehicle ready for dispatch",
            description = "Changes the status of a vehicle to 'Ready for dispatch' based on the vehicle number provided.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Vehicle status updated to ready successfully"),
                    @ApiResponse(responseCode = "400", description = "Error occurred or vehicle not found")
            })
    @PutMapping("/get-vehicle-ready")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> getVehicleReady(
            @RequestParam String number,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            vehicleService.getVehicleReady(number, authHeader);
            return ResponseEntity.ok().body("Vehicle added successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Delete a vehicle",
            description = "Deletes a vehicle from the system based on the vehicle number provided.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Vehicle deleted successfully"),
                    @ApiResponse(responseCode = "400", description = "Error occurred during deleting vehicle")
            })
    @DeleteMapping("/delete")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> deleteVehicle(
            @RequestParam String number,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            vehicleService.deleteVehicle(number, authHeader);
            return ResponseEntity.ok().body("Vehicle deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/send")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<Point>> sendVehicle(
            @RequestParam String number,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            return ResponseEntity.ok().body(vehicleService.sendVehicle(number, authHeader));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }
    }


    @GetMapping("/is-suitable")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Boolean> isSuitable(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody IsSuitableRequest request
    ) {
        try {
            return ResponseEntity.ok().body(vehicleService.isSuitable(request, authHeader));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok().body(false);
        }
    }

    @Operation(summary = "Ping test",
            description = "Simple ping operation to test the availability and responsiveness of the service.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Service is available and responsive")
            })
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
