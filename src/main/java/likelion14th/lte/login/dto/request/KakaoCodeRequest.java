package likelion14th.lte.login.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor

public class KakaoCodeRequest {
    @NotBlank(message= "인가 코드는 필수입니다.")
    private String code;
}
