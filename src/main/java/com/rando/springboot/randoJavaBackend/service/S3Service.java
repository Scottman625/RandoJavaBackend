package com.rando.springboot.randoJavaBackend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.rando.springboot.randoJavaBackend.controller.UserController;
import com.rando.springboot.randoJavaBackend.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
public class S3Service {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Autowired
    private AmazonS3 amazonS3;


    @Value("${aws.accessKeyId}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final long URL_EXPIRATION_TIME = 3600; // 1 hour

    public String getPresignedUrl(String fileName) {
        String replaceFileName;
        if  (fileName.contains("https://rando-app-bucket.s3.ap-northeast-1.amazonaws.com/")){
            replaceFileName = fileName.replace("https://rando-app-bucket.s3.ap-northeast-1.amazonaws.com/", "");
        }else{
            replaceFileName = fileName.replace("https://rando-app-bucket.s3.amazonaws.com/", "");
        }
        String objectKey  = replaceFileName.split("\\?")[0];

        try {
            // Check Redis cache for both URL and timestamp
            String cachedUrl = redisTemplate.opsForValue().get(objectKey);
            String cachedTimestamp = redisTemplate.opsForValue().get(objectKey + ":timestamp");

            if (cachedUrl == null || cachedTimestamp == null || isUrlExpired(cachedTimestamp)) {
                // Generate new presigned URL if not in cache or if it is expired
                String presignedUrl = generatePresignedUrl(objectKey);
                String timestamp = String.valueOf(System.currentTimeMillis());

                // Cache the generated URL and its timestamp in Redis
                redisTemplate.opsForValue().set(objectKey, presignedUrl, URL_EXPIRATION_TIME, TimeUnit.SECONDS);
                redisTemplate.opsForValue().set(objectKey + ":timestamp", timestamp, URL_EXPIRATION_TIME, TimeUnit.SECONDS);

                return presignedUrl;
            }

            return cachedUrl;
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return fileName;
    }

    private boolean isUrlExpired(String cachedTimestamp) {
        long timestamp = Long.parseLong(cachedTimestamp);
        long currentTime = System.currentTimeMillis();
        long expirationTimeMillis = URL_EXPIRATION_TIME * 1000;

        // Check if the cached URL is within the safe window (e.g., 5 minutes before actual expiration)
        return (currentTime - timestamp) > (expirationTimeMillis - (5 * 60 * 1000));
    }

    public String generatePresignedUrl(String objectKey) {

//        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();


//        S3Client s3 = S3Client.builder()
//                .region(Region.AP_NORTHEAST_1)
//                .credentialsProvider(credentialsProvider)
//                .build();

        StsClient sts = StsClient.builder()
                .region(Region.AP_NORTHEAST_1)
                .credentialsProvider(credentialsProvider)
                .build();


        String roleARN = "arn:aws:iam::854688567695:role/S3Access";
        String roleSessionName = "testSession";  // 你可以選擇一個有意義的名稱

        AssumeRoleRequest roleRequest = AssumeRoleRequest.builder()
                .roleArn(roleARN)
                .roleSessionName(roleSessionName)
                .build();

        AssumeRoleResponse roleResponse = sts.assumeRole(roleRequest);

        String tempAccessKeyId = roleResponse.credentials().accessKeyId();
        String tempSecretAccessKey = roleResponse.credentials().secretAccessKey();
        String tempSessionToken = roleResponse.credentials().sessionToken();

        S3Presigner presigner = S3Presigner.builder()
                .region(Region.AP_NORTHEAST_1)  // 請根據您的實際情況替換 region
                .credentialsProvider(StaticCredentialsProvider.create(AwsSessionCredentials.create(
                        tempAccessKeyId, tempSecretAccessKey, tempSessionToken)))
                .build();

        String url = GeneratePresignedUrlAndUploadObject.generatePresignedUrl(presigner,bucketName,objectKey);
        log.info(url);
        return url;

    }

    public void regeneratePresignedUrl(List<User> users){
        for(User user: users){
            String fileName = user.getImage();
            fileName = fileName.replace("https://rando-app-bucket.s3.amazonaws.com/", "");
            String objectKey  = fileName.split("\\?")[0];
            String presignedUrl = generatePresignedUrl(objectKey);

            // Cache the generated URL in Redis
            redisTemplate.opsForValue().set(objectKey, presignedUrl, URL_EXPIRATION_TIME, TimeUnit.SECONDS);

        }
    }
}

