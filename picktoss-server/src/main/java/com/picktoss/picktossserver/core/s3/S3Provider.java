package com.picktoss.picktossserver.core.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.picktoss.picktossserver.core.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.AMAZON_SERVICE_EXCEPTION;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.FILE_UPLOAD_ERROR;

@Component
@RequiredArgsConstructor
public class S3Provider {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile multipartFile, String s3Key){

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3.putObject(bucket, s3Key, inputStream, metadata);
        } catch (IOException e){
            throw new CustomException(FILE_UPLOAD_ERROR, e.getMessage());
        }
        return s3Key;
    }

    public void uploadFeedbackImage(List<MultipartFile> multipartFiles, List<String> s3Keys) {
        ObjectMetadata metadata = new ObjectMetadata();

        for (int i = 0; i < multipartFiles.size(); i++) {
            String s3Key = s3Keys.get(i);
            try (InputStream inputStream = multipartFiles.get(i).getInputStream()) {
                byte[] bytes = IOUtils.toByteArray(inputStream);

                metadata.setContentLength(bytes.length);
                metadata.setContentType(multipartFiles.get(i).getContentType());

                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

                amazonS3.putObject(bucket, s3Key, byteArrayInputStream, metadata);
            } catch (IOException e){
                throw new CustomException(FILE_UPLOAD_ERROR, e.getMessage());
            }
        }
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

    public void deleteFile(String s3Key) {
        try {
            amazonS3.deleteObject(bucket, s3Key);
        } catch (AmazonServiceException e) {
            throw new CustomException(AMAZON_SERVICE_EXCEPTION, e.getErrorMessage());
        }
    }

    private String decodeContentToString(byte[] contentBytes) {
        return new String(contentBytes, StandardCharsets.UTF_8);
    }
}
