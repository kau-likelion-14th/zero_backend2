package likelion14th.lte.login.dto.response;


import com.fasterxml.jackson.annotation.JsonIgnore;
import likelion14th.lte.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private Long id;
    private String username;
    private String userTag;
    private String introduction;
    private String profileImage;
    private String accessToken;

    @JsonIgnore
    private String refreshToken;

    public static AuthResponse from (User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .userTag(user.getUserTag())
                .introduction(user.getIntroduction())
                .profileImage(user.getProfileImage())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
