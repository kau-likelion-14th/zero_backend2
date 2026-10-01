package likelion14th.lte.youtube.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor

public class SongSaveRequest {
    @NotBlank(message = "songId는 필수입니다.")
    private String songId;
}
