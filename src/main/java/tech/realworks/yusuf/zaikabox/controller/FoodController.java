package tech.realworks.yusuf.zaikabox.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tech.realworks.yusuf.zaikabox.io.FoodRequest;
import tech.realworks.yusuf.zaikabox.io.FoodResponse;
import tech.realworks.yusuf.zaikabox.service.FoodService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/foods"})
@CrossOrigin("*")
@Tag(name = "Food Management", description = "APIs for managing food items")
public class FoodController {

    private final FoodService foodService;

    @Operation(summary = "Add a new food item", description = "Creates a new food item with an uploaded image. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Food item created successfully",
                    content = @Content(schema = @Schema(implementation = FoodResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid food data provided"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public FoodResponse addFood(@Parameter(description = "Food item data as JSON string")  @RequestPart("food") String foodString, @Parameter(description = "Image file for the food item")   @RequestPart("file") MultipartFile file) {
        ObjectMapper objectMapper = new ObjectMapper();

        FoodRequest foodRequest;
        try {
            foodRequest = objectMapper.readValue(foodString, FoodRequest.class);
        } catch (JsonProcessingException var6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid food request");
        }

        return this.foodService.addFood(foodRequest, file);
    }

    @Operation(summary = "Get all food items", description = "Retrieves all available food items")
    @ApiResponse(responseCode = "200", description = "List of all food items retrieved successfully",
            content = @Content(schema = @Schema(implementation = FoodResponse.class)))
    @GetMapping
    public List<FoodResponse> readFoods() {
        return foodService.readFoods();
    }

    @Operation(summary = "Get a food item by ID", description = "Retrieves a specific food item by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Food item found and returned",
                    content = @Content(schema = @Schema(implementation = FoodResponse.class))),
            @ApiResponse(responseCode = "404", description = "Food item not found")
    })
    @GetMapping({"/{id}"})
    public ResponseEntity<FoodResponse> readFood(@Parameter(description = "ID of the food item to retrieve") @PathVariable("id") String id) {
        FoodResponse foodResponse = foodService.readFood(id);
        return ResponseEntity.ok(foodResponse);
    }

    @Operation(summary = "Delete a food item", description = "Deletes a food item by its ID. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Food item successfully deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Food item not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping({"/{id}"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFood(@Parameter(description = "ID of the food item to delete") @PathVariable("id") String id) {
        foodService.deleteFood(id);
    }
}