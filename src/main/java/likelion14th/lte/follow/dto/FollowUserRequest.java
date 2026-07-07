package likelion14th.lte.follow.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowUserRequest {
    /** 팔로우·언팔로우 대상 유저의 ID입니다. */
    private Long toUserId;
}
