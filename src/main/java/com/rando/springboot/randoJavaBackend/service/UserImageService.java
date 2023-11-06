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
import java.util.Optional;
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

    public UserImage storeImage(MultipartFile file,User user) {
        log.info(file.getOriginalFilename());
        String fileName = "prefix-"+ "-" +currentTimeMillis() + file.getOriginalFilename();

        try {

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


            return imageEntity;
        } catch (IOException e) {
            throw new ResourceNotFoundException("Failed to store file " + fileName);
        }
    }

    public UserImage updateUserImage(MultipartFile file,User user,Long userImageId) {
        log.info(file.getOriginalFilename());
        String fileName = "prefix-"+ "-" +currentTimeMillis() + file.getOriginalFilename();

        try {

            String filePath = saveTempFile(file);

            final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(region).build();
            String key ="images/" + fileName; // 這裡加上了 "images/" 前綴
            s3.putObject(bucketName, key, new File(filePath));
            new File(filePath).delete();

            // If storing the file metadata/path in a database is necessary
            Optional<UserImage> OptionalImage = userImageRepository.findByUserAndId(user,userImageId);
            if (OptionalImage.isPresent()){
                UserImage imageEntity = OptionalImage.get();
                imageEntity.setImage(s3Service.getPresignedUrl("images/" + fileName));
                imageEntity.setUser(user);
                saveUserImage(imageEntity);  // Assuming you have a repository interface for ImageEntity
                return imageEntity;
            }
            throw new ResourceNotFoundException("UserImage not found: " + fileName);
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
        UserImage newUserImage = storeImage(image,user);
        saveUserImage(newUserImage);
    }

    public void updateUserImage(User user, Long id, MultipartFile newImage) {
        UserImage existingImage = userImageRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        // 這裡，你可以添加對新的圖片進行處理並更新的邏輯
        // 例如：儲存新的圖片，並更新 existingImage 的相關屬性
        UserImage newUserImage = storeImage(newImage,user);
        existingImage.setImage(newUserImage.getImage());

//        return saveUserImage(existingImage);
    }

    public void deleteUserImage(User user, Long id) {
        UserImage image = userImageRepository.findByUserAndId(user, id)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
        userImageRepository.delete(image);
        UserImage firsUserImage = userImageRepository.findFirstByUserOrderByUpdateAtAsc(user);
        if (firsUserImage != null) {
            user.setImage(firsUserImage.getImage());
            userRepository.save(user);
        }
    }

    public void saveUserImage(UserImage userImage) {
        UserImage savedImage = userImageRepository.save(userImage);
        User user = savedImage.getUser();
        UserImage firstUserImage = userImageRepository.findFirstByUserOrderByUpdateAtAsc(user);
        user.setImage(firstUserImage.getImage());  // Assuming the user entity has a setImage method
        userRepository.save(user);
        // save the user as well if needed
//        return savedImage;
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

