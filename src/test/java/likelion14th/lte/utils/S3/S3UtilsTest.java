package likelion14th.lte.utils.S3;

import likelion14th.lte.global.config.AmazonConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class S3UtilsTest {

    @Test
    void uploadBytes_usesObjectKeyInsteadOfPublicUrl() {
        S3Client s3Client = mock(S3Client.class);
        AmazonConfig amazonConfig = new AmazonConfig(
                "test-bucket", "access-key", "secret-key", "ap-northeast-2", "user/"
        );
        S3Utils s3Utils = new S3Utils(s3Client, amazonConfig);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        S3Dto result = s3Utils.uploadBytes(new byte[]{1, 2, 3}, "profile.png", "image/png");

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        PutObjectRequest request = requestCaptor.getValue();

        assertThat(request.bucket()).isEqualTo("test-bucket");
        assertThat(request.key()).startsWith("user/").endsWith(".png");
        assertThat(request.key()).doesNotStartWith("https://");
        assertThat(result.getKey()).isEqualTo(request.key());
        assertThat(result.getUrl()).endsWith(request.key());
    }
}
