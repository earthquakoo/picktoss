package com.picktoss.picktossserver.core.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Component
@RequiredArgsConstructor
public class S3Provider {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile multipartFile){

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(multipartFile.getContentType());

        String s3Key = generateS3key();

        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3.putObject(bucket, s3Key, inputStream, metadata);
        } catch (IOException e){
            throw new CustomException(FILE_UPLOAD_ERROR, e.getMessage());
        }
        return s3Key;
    }

    public String findFile(String s3Key) {
        try {
            S3Object s3Object = amazonS3.getObject(bucket, s3Key);
            S3ObjectInputStream file = s3Object.getObjectContent();
            byte[] contentBytes = file.readAllBytes();
            return decodeContentToString(contentBytes);
        } catch (AmazonServiceException e) {
            throw new CustomException(AMAZON_SERVICE_EXCEPTION, e.getErrorMessage());
        } catch (IOException e) {
            throw new CustomException(FILE_UPLOAD_ERROR, e.getMessage());
        }
    }

    public String findImageUrl(String s3Key) {
        try {
            URL url = amazonS3.getUrl(bucket, s3Key);
            return ""+url;
        } catch (AmazonServiceException e) {
            throw new CustomException(AMAZON_SERVICE_EXCEPTION, e.getErrorMessage());
        }
    }

    public void deleteFile(String s3Key) {
        try {
            amazonS3.deleteObject(bucket, s3Key);
        } catch (AmazonServiceException e) {
            throw new CustomException(AMAZON_SERVICE_EXCEPTION, e.getErrorMessage());
        }
    }

    private String generateS3key() {
        return UUID.randomUUID().toString();
    }

    private String decodeContentToString(byte[] contentBytes) {
        return new String(contentBytes, StandardCharsets.UTF_8);
    }
}
