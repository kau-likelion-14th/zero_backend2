package likelion14th.lte.youtube.controller;

import likelion14th.lte.youtube.dto.request.SongSaveRequest;
import likelion14th.lte.youtube.service.YouTubeService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class YouTubeControllerTest {

    private final YouTubeService youTubeService = mock(YouTubeService.class);
    private final YouTubeController controller = new YouTubeController(youTubeService);

    @Test
    void save_uses_the_authenticated_jwt_subject() {
        SongSaveRequest request = new SongSaveRequest();
        ReflectionTestUtils.setField(request, "songId", "song-1");

        var result = controller.save(jwtFor(42L), request);

        verify(youTubeService).saveSong(42L, "song-1");
        assertThat(result.getCode()).isEqualTo("COMMON_200");
    }

    @Test
    void myList_uses_the_authenticated_jwt_subject() {
        var result = controller.myList(jwtFor(42L));

        verify(youTubeService).mySavedSongs(42L);
        assertThat(result.getCode()).isEqualTo("COMMON_200");
    }

    @Test
    void delete_uses_the_authenticated_jwt_subject() {
        var result = controller.delete(jwtFor(42L), "song-1");

        verify(youTubeService).deleteSavedSong(42L, "song-1");
        assertThat(result.getCode()).isEqualTo("COMMON_200");
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
