package likelion14th.lte.user.service;

import likelion14th.lte.user.entity.User;
import likelion14th.lte.user.repository.UserRepository;
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

        var response = userProfileService.deleteProfileImage(1L);

        verify(s3Utils).deleteFile("user/existing-image.png");
        assertThat(user.getS3ImageKey()).isNull();
        assertThat(user.getProfileImage()).isNull();
        assertThat(response.getProfileImageUrl()).isNull();
    }

    @Test
    void deleteProfileImage_withoutImageIsSafeAndDoesNotCallS3() {
        User user = User.builder()
                .username("tester")
                .userTag("TEST1234")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var response = userProfileService.deleteProfileImage(1L);

        verifyNoInteractions(s3Utils);
        assertThat(user.getS3ImageKey()).isNull();
        assertThat(user.getProfileImage()).isNull();
        assertThat(response.getProfileImageUrl()).isNull();
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

    @Test
    void updateIntroduction_updatesOnlyTheAuthenticatedUsersIntroduction() {
        User user = User.builder()
                .username("tester")
                .userTag("TEST1234")
                .introduction("before")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var response = userProfileService.updateIntroduction(1L, "after");

        assertThat(response.getIntroduction()).isEqualTo("after");
        assertThat(user.getIntroduction()).isEqualTo("after");
    }

    @Test
    void getOtherUserProfile_usesTheTargetUserId() {
        User user = User.builder()
                .username("friend")
                .userTag("FRIEND01")
                .introduction("hello")
                .build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        var response = userProfileService.getOtherUserProfile(2L);

        assertThat(response.getUsername()).isEqualTo("friend#FRIEND01");
        verify(userRepository).findById(2L);
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
