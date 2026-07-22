package likelion14th.lte.follow.dto;

import likelion14th.lte.User.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowUserResponse {
    private Long userId;
    private String userName;
    private String profileImageUrl;
    private String introduction;

    public static FollowUserResponse from(User user) {
        return new FollowUserResponse(
                user.getId(),
                user.getUsername() + "#" + user.getUserTag(),
                user.getProfileImage(),
                user.getIntroduction()
        );
    }
}
