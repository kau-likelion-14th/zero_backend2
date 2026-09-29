package likelion14th.lte.User.service;

import likelion14th.lte.User.entity.User;
import likelion14th.lte.User.repository.UserRepository;
import likelion14th.lte.utils.Image.ImageUtil;
import likelion14th.lte.utils.S3.S3Dto;
import likelion14th.lte.utils.S3.S3Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3Utils s3Utils;

    @Mock
    private ImageUtil imageUtil;

    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    void deleteProfileImage_deletesS3ObjectAndClearsUserFields() {
        User user = userWithProfileImage();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(s3Utils).deleteFile("user/existing-image.png");

        userProfileService.deleteProfileImage(1L);

        verify(s3Utils).deleteFile("user/existing-image.png");
        assertThat(user.getS3ImageKey()).isNull();
        assertThat(user.getProfileImage()).isNull();
    }

    @Test
    void deleteProfileImage_withoutImageIsSafeAndDoesNotCallS3() {
        User user = User.builder()
                .username("tester")
                .userTag("TEST1234")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userProfileService.deleteProfileImage(1L);

        verifyNoInteractions(s3Utils);
        assertThat(user.getS3ImageKey()).isNull();
        assertThat(user.getProfileImage()).isNull();
    }

    @Test
    void putProfileImage_uploadsNewImageThenDeletesExistingS3Object() {
        User user = userWithProfileImage();
        MockMultipartFile file = new MockMultipartFile(
                "image", "profile.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );
        ImageUtil.ResizedImage resizedImage = new ImageUtil.ResizedImage(
                new byte[]{9, 8, 7}, "image/png", "png"
        );
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(imageUtil.resizeProfileToPngBytes(file, 256)).thenReturn(resizedImage);
        when(s3Utils.uploadBytes(eq(resizedImage.bytes()), eq("profile.png"), eq("image/png")))
                .thenReturn(new S3Dto(
                        "https://bucket.s3.ap-northeast-2.amazonaws.com/user/new-image.png",
                        "user/new-image.png"
                ));

        userProfileService.putProfileImage(1L, file);

        verify(imageUtil).validateImage(file);
        verify(s3Utils).deleteFile("user/existing-image.png");
        assertThat(user.getS3ImageKey()).isEqualTo("user/new-image.png");
        assertThat(user.getProfileImage())
                .isEqualTo("https://bucket.s3.ap-northeast-2.amazonaws.com/user/new-image.png");
    }

    private User userWithProfileImage() {
        return User.builder()
                .username("tester")
                .userTag("TEST1234")
                .profileImage("https://bucket.s3.ap-northeast-2.amazonaws.com/user/existing-image.png")
                .s3ImageKey("user/existing-image.png")
                .build();
    }
}
