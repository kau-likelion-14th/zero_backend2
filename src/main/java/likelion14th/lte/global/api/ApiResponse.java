package likelion14th.lte.global.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class ApiResponse<T> {

    private final Boolean isSuccess;
    private final String code;
    private final String message;
    private final T result;

    @JsonIgnore
    private final HttpStatus httpStatus;

    public static <T> ApiResponse<T> onSuccess(BaseCode code, T result) {
        return new ApiResponse<>(
                true,
                code.getReason().getCode(),
                code.getReason().getMessage(),
                result,
                code.getReason().getHttpStatus()
        );
    }

    public static ApiResponse<Void> onFailure(BaseCode code) {
        return new ApiResponse<>(
                false,
                code.getReason().getCode(),
                code.getReason().getMessage(),
                null,
                code.getReason().getHttpStatus()
        );
    }

    public static <T> ApiResponse<T> onFailure(BaseCode code, T data) {
        return new ApiResponse<>(
                false,
                code.getReason().getCode(),
                code.getReason().getMessage(),
                data,
                code.getReason().getHttpStatus()
        );
    }
}
