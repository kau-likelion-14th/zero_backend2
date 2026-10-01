package likelion14th.lte.utils.S3;

import likelion14th.lte.global.config.AmazonConfig;
import likelion14th.lte.utils.exception.UtilException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static likelion14th.lte.utils.exception.UtilException.Reason.*;

import java.util.UUID;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class S3Utils {
    private final S3Client s3Client;
    private final AmazonConfig config;

    private String generateFileKey(String fileName) {
        String extension = extractExtension(fileName);
        String location = config.getLocationPath();
        String normalizedLocation = location == null || location.isBlank()
                ? ""
                : location.endsWith("/") ? location : location + "/";
        return normalizedLocation + UUID.randomUUID() + extension;
    }

    private String extractExtension(String fileName) {
        if (fileName == null) {
            return ".bin";
        }

        int extensionStart = fileName.lastIndexOf('.');
        if (extensionStart < 0 || extensionStart == fileName.length() - 1) {
            return ".bin";
        }
        return fileName.substring(extensionStart).toLowerCase();
    }

    private String getUrl(String key) {
        return "https://" + config.getBucket() + ".s3." + config.getRegion()
                + ".amazonaws.com/" + key;
    }

    public S3Dto uploadBytes(byte[] bytes, String fileName, String contentType) {
        if (bytes == null || bytes.length == 0) {
            throw new UtilException(FILE_EMPTY);
        }
        if (fileName == null || fileName.isBlank()) {
            throw new UtilException(FILE_EMPTY);
        }

        String key = generateFileKey(fileName);
        String resolvedContentType = contentType != null && !contentType.isBlank()
                ? contentType
                : "application/octet-stream";
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(config.getBucket())
                .key(key)
                .contentType(resolvedContentType)
                .build();

        try {
            s3Client.putObject(req, RequestBody.fromBytes(bytes));
            return new S3Dto(getUrl(key), key);
        } catch (SdkException e) {
            log.error("S3 upload failed for key={}", key, e);
            throw new UtilException(S3_UPLOAD_FAILED, e);
        }
    }

    public void deleteFile(String key) {
        if (key == null || key.isBlank()) {
            throw new UtilException(S3_DELETE_FAILED);
        }

        try {
            DeleteObjectRequest req = DeleteObjectRequest.builder()
                    .bucket(config.getBucket())
                    .key(key)
                    .build();
            s3Client.deleteObject(req);
            log.info("Deleted S3 object key={}", key);
        } catch (SdkException e) {
            log.error("S3 deletion failed for key={}", key, e);
            throw new UtilException(S3_DELETE_FAILED, e);
        }
    }
}
