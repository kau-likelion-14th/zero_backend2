package likelion14th.lte.user.controller;

import likelion14th.lte.user.dto.request.UserIntroRequest;
import likelion14th.lte.user.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserProfileControllerTest {

    private final UserProfileService userProfileService = mock(UserProfileService.class);
    private final UserProfileController controller = new UserProfileController(userProfileService);

    @Test
    void getOtherUserProfile_usesToUserIdInsteadOfJwtSubject() {
        var response = controller.getOtherUserProfile(jwtFor(1L), 2L);

        verify(userProfileService).getOtherUserProfile(2L);
        assertThat(response.getCode()).isEqualTo("USER_2007");
    }

    @Test
    void updateIntroduction_usesJwtSubjectAndIntroduceField() {
        UserIntroRequest request = new UserIntroRequest();
        ReflectionTestUtils.setField(request, "introduce", "새 소개");

        var response = controller.updateIntroduction(jwtFor(1L), request);

        verify(userProfileService).updateIntroduction(1L, "새 소개");
        assertThat(response.getCode()).isEqualTo("USER_2006");
    }

    @Test
    void deleteProfileImage_returnsTheUpdatedProfileResponse() {
        var response = controller.deleteUserProfileImage(jwtFor(1L));

        verify(userProfileService).deleteProfileImage(1L);
        assertThat(response.getCode()).isEqualTo("PROFILE_2002");
    }

    private Jwt jwtFor(Long userId) {
        Instant now = Instant.now();
        return new Jwt(
                "access-token",
                now,
                now.plusSeconds(60),
                Map.of("alg", "HS256"),
                Map.of("sub", userId.toString(), "type", "ACCESS")
        );
    }
}
