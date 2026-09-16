package likelion14th.lte.login.service;



import likelion14th.lte.User.entity.User;
import likelion14th.lte.User.repository.UserRepository;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.login.client.KakaoClient;
import likelion14th.lte.login.domain.RefreshToken;
import likelion14th.lte.login.dto.response.AuthResponse;
import likelion14th.lte.login.jwt.JwtProvider;
import likelion14th.lte.login.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final KakaoClient kakaoClient;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    public AuthResponse handleKakaoCode(String code) {
        String kakaoAccessToken = kakaoClient.getAccessToken(code);
        JsonNode kakaoUserInfo = kakaoClient.getUserInfo(kakaoAccessToken);

        JsonNode providerIdNode = kakaoUserInfo.path("id");
        String providerId = providerIdNode.isMissingNode() || providerIdNode.isNull()
                ? null
                : providerIdNode.asText();
        if (providerId == null || providerId.isBlank()) {
            throw new GeneralException(ErrorCode.KAKAO_API_FAILED);
        }
        JsonNode nicknameNode = kakaoUserInfo.path("kakao_account")
                .path("profile")
                .path("nickname");
        String username = nicknameNode.isMissingNode() || nicknameNode.isNull() || nicknameNode.asText().isBlank()
                ? "카카오 유저"
                : nicknameNode.asText();

        User user = userRepository.findByProviderId(providerId)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .providerId(providerId)
                                .username(username)
                                .userTag(createUniqueUserTag())
                                .build()
                ));

        return issueToken(user);
    }

    private AuthResponse issueToken(User user) {
        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        Long refreshTokenExpiration = jwtProvider.getRefreshTokenExpiration();

        saveOrUpdateRefreshToken(user, refreshToken,refreshTokenExpiration);
        return AuthResponse.from(user, accessToken, refreshToken);
    }

    public void saveOrUpdateRefreshToken(User user, String token, Long expiration) {
        refreshTokenRepository.findByUser(user).ifPresentOrElse(
                exiting -> exiting.updateToken(token, expiration),
                () -> refreshTokenRepository.save(
                        RefreshToken.builder()
                                .user(user)
                                .refreshToken(token)
                                .refreshTokenExpiration(expiration)
                                .build()
                )
        );
    }


    @Transactional(readOnly = true)
    public String reissueAccessToken(String refreshToken) {
        Long userId;
        try {
            userId = jwtProvider.validateRefreshToken(refreshToken);
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.TOKEN_INVALID);
        }


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        RefreshToken savedToken = refreshTokenRepository.findByUser(user)
                .orElseThrow(()-> new GeneralException(ErrorCode.WRONG_REFRESH_TOKEN));

        if(!savedToken.getRefreshToken().equals(refreshToken)){
            throw new GeneralException(ErrorCode.TOKEN_INVALID);
        }

        return jwtProvider.createAccessToken(user.getId());
    }

    public void logout(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new GeneralException((ErrorCode.USER_NOT_FOUND)));

        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
    }

    private String createUniqueUserTag(){
        String userTag;
            userTag = "KAKAO" + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0,8)
                    .toUpperCase();
        return userTag;
    }

    public void withdraw(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        userRepository.delete(user);
    }


}
