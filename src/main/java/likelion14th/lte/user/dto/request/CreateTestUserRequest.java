package likelion14th.lte.user.dto.request;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class CreateTestUserRequest {
    private String username;
    private String userTag;
    private String introduction;

}
