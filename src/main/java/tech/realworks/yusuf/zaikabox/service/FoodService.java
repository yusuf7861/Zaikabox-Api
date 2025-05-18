package tech.realworks.yusuf.zaikabox.service;

import org.springframework.web.multipart.MultipartFile;
import tech.realworks.yusuf.zaikabox.io.FoodRequest;
import tech.realworks.yusuf.zaikabox.io.FoodResponse;

import java.util.List;

public interface FoodService {
    String uploadFile(MultipartFile file);

    FoodResponse addFood(FoodRequest foodRequest, MultipartFile file);

    List<FoodResponse> readFoods();

    FoodResponse readFood(String id);

    boolean deleteFile(String fileName);

    void deleteFood(String id);
}
