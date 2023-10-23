package com.rando.springboot.randoJavaBackend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.rando.springboot.randoJavaBackend.controller.UserController;
import com.rando.springboot.randoJavaBackend.dao.UserImageRepository;
import com.rando.springboot.randoJavaBackend.dao.UserRepository;
import com.rando.springboot.randoJavaBackend.dto.UserImageDTO;
import com.rando.springboot.randoJavaBackend.entity.ResourceNotFoundException;
import com.rando.springboot.randoJavaBackend.entity.User;
import com.rando.springboot.randoJavaBackend.entity.UserImage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import com.amazonaws.services.s3.model.S3Object;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.System.currentTimeMillis;

@Service
public class UserImageService {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);


    @Autowired
    private UserImageRepository userImageRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${aws.accessKeyId}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;

    private S3Client s3Client;

    @Autowired
    private S3Service s3Service; // The S3Service we defined previously

    @PostConstruct
    public void initializeS3Client() {
        s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

    }

    public String storeImage(MultipartFile file,User user) {
        log.info(file.getOriginalFilename());
        String fileName = "prefix-"+ "-" +currentTimeMillis() + file.getOriginalFilename();

        try {
//            log.info("Test01");
//            s3Client.putObject(PutObjectRequest.builder()
//                            .bucket(bucketName)
//                            .key(fileName)
//                            .build(),
//                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
//
//            log.info("Test02");

            String filePath = saveTempFile(file);

            final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(region).build();
            String key ="images/" + fileName; // 這裡加上了 "images/" 前綴
            s3.putObject(bucketName, key, new File(filePath));
            new File(filePath).delete();


            // If storing the file metadata/path in a database is necessary
            UserImage imageEntity = new UserImage();
            imageEntity.setImage(s3Service.getPresignedUrl("images/" + fileName));
            imageEntity.setUser(user);
            saveUserImage(imageEntity);  // Assuming you have a repository interface for ImageEntity


            return fileName;
        } catch (IOException e) {
            throw new ResourceNotFoundException("Failed to store file " + fileName);
        }
    }

    public String saveTempFile(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("prefix-", "-" + file.getOriginalFilename());
        file.transferTo(tempFile);
        return tempFile.getAbsolutePath();
    }

    private String generateFileName(String originalFileName) {
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return UUID.randomUUID().toString() + fileExtension;
    }

    public void createUserImage(User user, MultipartFile image) {
//        UserImage userImage = new UserImage();
//        // logic to save image...
//        userImage.setUser(user);
//        String imageUrl = storeImage(image,user);
//        userImage.setImage(imageUrl);
        storeImage(image,user);
//        return saveUserImage(userImage);
    }

    public void updateUserImage(User user, Long id, MultipartFile newImage) {
        UserImage existingImage = userImageRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        // 這裡，你可以添加對新的圖片進行處理並更新的邏輯
        // 例如：儲存新的圖片，並更新 existingImage 的相關屬性
        String imageUrl = storeImage(newImage,user);
        existingImage.setImage(imageUrl);

//        return saveUserImage(existingImage);
    }

    public void deleteUserImage(User user, Long id) {
        UserImage image = userImageRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
        deleteUserImage(image);
    }

    public void saveUserImage(UserImage userImage) {
        UserImage savedImage = userImageRepository.save(userImage);
        User user = savedImage.getUser();
        user.setImage(savedImage.getImage());  // Assuming the user entity has a setImage method
        userRepository.save(user);
        // save the user as well if needed
//        return savedImage;
    }

    public void deleteUserImage(UserImage userImage) {
        userImageRepository.delete(userImage);
        User user = userImage.getUser();
        UserImage latestImage = userImageRepository.findFirstByUserOrderByUpdateAtDesc(user);
        if (latestImage != null) {
            user.setImage(latestImage.getImage());
            // save the user as well if needed
        }
    }

    public List<UserImageDTO >convertToUserImageDTOs(User user){
        List<UserImageDTO> userImageDTOS = new ArrayList<>();
        List<UserImage> userImages = userImageRepository.findByUser(user);

        for (UserImage userImage: userImages){
            UserImageDTO dto = new UserImageDTO(userImage);

            BeanUtils.copyProperties(userImage, dto);
            dto.setImageUrl(s3Service.getPresignedUrl(userImage.getImage()));

            userImageDTOS.add(dto);
        }
        return userImageDTOS;
    }
}

