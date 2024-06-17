package com.nure.apz.fatianov.daniil.vehiclestationservice.station;

import com.nure.apz.fatianov.daniil.vehiclestationservice.station.request.StationAddRequest;
import com.nure.apz.fatianov.daniil.vehiclestationservice.station.request.StationChangeRequest;
import com.nure.apz.fatianov.daniil.vehiclestationservice.station.response.StationGetAllResponseEntity;
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
@RequestMapping("/vehicle-station-service/station")
@RequiredArgsConstructor
public class StationController {
    private final StationService stationService;

    @Operation(summary = "Add a new station",
            description = "Adds a new station to the system.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Station added successfully"),
                    @ApiResponse(responseCode = "400", description = "Error occurred while adding station")
            })
    @PostMapping("/add")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> addStation(
            @RequestBody StationAddRequest requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            stationService.addStation(requestBody, authHeader);
            return ResponseEntity.ok().body("Station added successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Get all stations",
            description = "Retrieves all stations from the system.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "All stations retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Error occurred while retrieving stations")
            })
    @GetMapping("/get-all")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<StationGetAllResponseEntity>> getAllStations(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            return ResponseEntity.ok().body(stationService.getAllStations(authHeader));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ArrayList<>());
        }
    }

    @Operation(summary = "Get all stations",
            description = "Retrieves all stations from the system.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "All stations retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Error occurred while retrieving stations")
            })
    @GetMapping("/get-number")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> getStationNumber(
            @RequestParam Integer id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            return ResponseEntity.ok().body(stationService.getStationNumber(id, authHeader));
        } catch (Exception e) {
            return ResponseEntity.ok().body(null);
        }
    }

    @Operation(summary = "Get station ID by number",
            description = "Retrieves the station ID based on the station number provided.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Station ID retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Station ID could not be retrieved")
            })
    @GetMapping("/get-id")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Integer> getStationId(
            @RequestParam String number,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            return ResponseEntity.ok().body(stationService.getStationId(number, authHeader));
        } catch (Exception e) {
            return ResponseEntity.ok().body(null);
        }
    }

    @Operation(summary = "Change station details",
            description = "Updates the details of an existing station.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Station details updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Error occurred during station update")
            })
    @PutMapping("/change-station")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> changeStation(
            @RequestBody StationChangeRequest requestBody,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            stationService.changeStation(requestBody, authHeader);
            return ResponseEntity.ok().body("Station changed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Delete a station",
            description = "Deletes a station from the system based on the station number provided.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Station deleted successfully"),
                    @ApiResponse(responseCode = "400", description = "Error occurred during station deletion")
            })
    @DeleteMapping("/delete")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> deleteStation(
            @RequestParam String number,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        try {
            stationService.deleteStation(number, authHeader);
            return ResponseEntity.ok().body("Station deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
