package com.rando.springboot.randoJavaBackend.service;

import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.time.Duration;

public class GeneratePresignedUrlAndUploadObject {
    public static String generatePresignedUrl(S3Presigner presigner, String bucketName, String objectKey) {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        // 建立預簽名請求
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))  // 簽名有效時間
                .getObjectRequest(getObjectRequest)
                .build();

        // 產生預簽名的URL
        PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(presignRequest);
        URL url = presignedGetObjectRequest.url();

        return url.toString();
    }



}
