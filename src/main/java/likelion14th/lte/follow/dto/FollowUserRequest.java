package likelion14th.lte.follow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowUserRequest {
    @NotNull(message = "팔로우할 사용자 id는 필수입니다.")
    private Long toUserId;
}
