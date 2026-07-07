package likelion14th.lte.follow.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import likelion14th.lte.User.entity.User;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowUserResponse {
    private Long userId;           // 유저 ID
    private String userName;       // 표시 이름 (닉네임#태그 형태)
    private String profileImageUrl; // 프로필 이미지 URL
    private String introduction;   // 자기소개

    /**
     * User 엔티티를 FollowUserResponse DTO로 변환하는 정적 팩토리 메서드입니다.
     * userName은 "닉네임#태그" 형태로 조합하여 반환합니다.
     */
    public static FollowUserResponse from(User user){
        return new FollowUserResponse(
                user.getId(),                                    // 유저 ID
                user.getUsername()+"#"+user.getUserTag(),       // 표시용 이름: "닉네임#태그" 한 문자열로 합칩니다.
                user.getProfileImage(),                          // 프로필 이미지 URL
                user.getIntroduction()                           // 자기소개
        );
    }
}
