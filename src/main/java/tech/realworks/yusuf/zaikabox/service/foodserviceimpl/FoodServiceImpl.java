package tech.realworks.yusuf.zaikabox.service.foodserviceimpl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.realworks.yusuf.zaikabox.entity.FoodEntity;
import tech.realworks.yusuf.zaikabox.io.FoodRequest;
import tech.realworks.yusuf.zaikabox.io.FoodResponse;
import tech.realworks.yusuf.zaikabox.repository.FoodRepository;
import tech.realworks.yusuf.zaikabox.service.FoodService;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoodServiceImpl implements FoodService {

    private final BlobContainerClient containerClient;
    private final FoodRepository foodRepository;
    private final BlobContainerClient blobContainerClient;

    @Override
    public String uploadFile(MultipartFile file) {
        String var10000 = String.valueOf(UUID.randomUUID());
        String filename = var10000 + "-" + file.getOriginalFilename();
        BlobClient blobClient = this.containerClient.getBlobClient(filename);

        try (InputStream inputStream = file.getInputStream()) {
            blobClient.upload(inputStream, file.getSize(), true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }

        return blobClient.getBlobUrl();
    }


    @Override
    public FoodResponse addFood(FoodRequest foodRequest, MultipartFile file) {
        FoodEntity newFoodEntity = this.convertToEntity(foodRequest);
        String imageUrl = this.uploadFile(file);
        newFoodEntity.setImageUrl(imageUrl);
        newFoodEntity = foodRepository.save(newFoodEntity);
        return this.convertToResponse(newFoodEntity);
    }

    @Override
    public List<FoodResponse> readFoods() {
        List<FoodEntity> databaseEntities = foodRepository.findAll();
        return databaseEntities.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public FoodResponse readFood(String id) {
        FoodEntity foodEntity = foodRepository.findById(id).orElseThrow(() -> new RuntimeException("Food with id " + id + " is not found"));
        return convertToResponse(foodEntity);
    }

    @Override
    public boolean deleteFile(String fileName) {
        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(fileName);

            if(blobClient.exists()) {
                blobClient.delete();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void deleteFood(String id) {
       FoodResponse response = readFood(id);
        String fileName = response.getImageUrl().substring(response.getImageUrl().lastIndexOf("/") + 1);
        boolean isFileDeleted = deleteFile(fileName);
        if (isFileDeleted) {
            foodRepository.deleteById(response.getId());
        }

    }

    private FoodEntity convertToEntity(FoodRequest foodRequest) {
        return FoodEntity.builder().name(foodRequest.getName()).description(foodRequest.getDescription()).price(foodRequest.getPrice()).category(foodRequest.getCategory()).build();
    }

    private FoodResponse convertToResponse(FoodEntity foodEntity) {
        return FoodResponse.builder().id(foodEntity.getId()).name(foodEntity.getName()).description(foodEntity.getDescription()).price(foodEntity.getPrice()).category(foodEntity.getCategory()).imageUrl(foodEntity.getImageUrl()).build();
    }
}
