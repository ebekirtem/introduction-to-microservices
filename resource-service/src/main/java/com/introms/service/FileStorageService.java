package com.introms.service;

import com.introms.config.S3ConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final S3Client s3;
    private final S3ConfigProperties s3ConfigProperties;

    @PostConstruct
    public void ensureBucketExists() {
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(s3ConfigProperties.getBucketName()).build());
        } catch (S3Exception e) {
            s3.createBucket(CreateBucketRequest.builder().bucket(s3ConfigProperties.getBucketName()).build());
        }
    }

    public String upload(String key, byte[] data, String contentType) {
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(s3ConfigProperties.getBucketName())
                .key(key)
                .contentType(contentType)
                .contentLength((long) data.length)
                .build();

        s3.putObject(req, RequestBody.fromBytes(data));
        return key;
    }

    /** ✅ Download (GetObject) */
    public byte[] download(String key) {
        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(s3ConfigProperties.getBucketName())
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> bytes = s3.getObjectAsBytes(req);
        return bytes.asByteArray();
    }

    /** ✅ Delete (DeleteObject) */
    public void delete(String key) {
        DeleteObjectRequest req = DeleteObjectRequest.builder()
                .bucket(s3ConfigProperties.getBucketName())
                .key(key)
                .build();

        s3.deleteObject(req);
    }

    public boolean exists(String key) {
        try {
            s3.headObject(HeadObjectRequest.builder().bucket(s3ConfigProperties.getBucketName()).key(key).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) return false;
            throw e;
        }
    }
}
