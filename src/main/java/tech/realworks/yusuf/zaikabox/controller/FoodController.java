package tech.realworks.yusuf.zaikabox.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class FoodController {

    private final FoodService foodService;

    @PostMapping
    public FoodResponse addFood(@RequestPart("food") String foodString, @RequestPart("file") MultipartFile file) {
        ObjectMapper objectMapper = new ObjectMapper();

        FoodRequest foodRequest = null;
        try {
            foodRequest = objectMapper.readValue(foodString, FoodRequest.class);
        } catch (JsonProcessingException var6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid food request");
        }

        return this.foodService.addFood(foodRequest, file);
    }

    @GetMapping
    public List<FoodResponse> readFoods() {
        return foodService.readFoods();
    }

    @GetMapping({"/{id}"})
    public ResponseEntity<FoodResponse> readFood(@PathVariable("id") String id) {
        FoodResponse foodResponse = foodService.readFood(id);
        return ResponseEntity.ok(foodResponse);
    }

    @DeleteMapping({"/{id}"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFood(@PathVariable("id") String id) {
        foodService.deleteFood(id);
    }
}