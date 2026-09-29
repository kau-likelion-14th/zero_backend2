package likelion14th.lte.User.service;

import likelion14th.lte.User.dto.request.CreateTestUserRequest;
import likelion14th.lte.User.dto.responce.UserProfileResponse;
import likelion14th.lte.User.entity.User;
import likelion14th.lte.User.repository.UserRepository;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.utils.Image.ImageUtil;
import likelion14th.lte.utils.S3.S3Dto;
import likelion14th.lte.utils.S3.S3Utils;
import likelion14th.lte.utils.exception.UtilException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileService {

    private final UserRepository userRepository;
    private final S3Utils s3Utils;
    private final ImageUtil imageUtil;

    @Transactional
    public UserProfileResponse createTestUser(CreateTestUserRequest request) {

        User newUser = User.builder()
                .username(request.getUsername())
                .userTag(request.getUserTag())
                .introduction(request.getIntroduction())
                .build();

        User saveUser;

        try {
            saveUser = userRepository.save(newUser);
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.BAD_REQUEST);
        }

        return UserProfileResponse.from(saveUser);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new GeneralException(ErrorCode.USER_NOT_FOUND));

        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse putProfileImage(
            Long userId,
            MultipartFile profileImage) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new GeneralException(ErrorCode.USER_NOT_FOUND));

        try {
            imageUtil.validateImage(profileImage);

            ImageUtil.ResizedImage resizedImage =
                    imageUtil.resizeProfileToPngBytes(
                            profileImage,
                            256
                    );

            String originalFilename =
                    profileImage.getOriginalFilename();

            String baseName = originalFilename != null &&
                    originalFilename.contains(".")
                    ? originalFilename.substring(
                    0,
                    originalFilename.lastIndexOf('.'))
                    : "profile";

            S3Dto result =
                    s3Utils.uploadBytes(
                            resizedImage.bytes(),
                            baseName + ".png",
                            resizedImage.contentType()
                    );

            if (user.getS3ImageKey() != null) {
                s3Utils.deleteFile(user.getS3ImageKey());
            }

            user.updateProfileImage(
                    result.getUrl(),
                    result.getKey()
            );

            return UserProfileResponse.from(user);

        } catch (UtilException e) {
            throw GeneralException.of(
                    mapToErrorCode(e.getReason())
            );
        }
    }

    @Transactional
    public void deleteProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        String imageKey = user.getS3ImageKey();
        if (imageKey == null || imageKey.isBlank()) {
            user.removeProfileImage();
            return;
        }

        try {
            s3Utils.deleteFile(imageKey);
            user.removeProfileImage();
        } catch (UtilException e) {
            throw GeneralException.of(mapToErrorCode(e.getReason()));
        }
    }

    private ErrorCode mapToErrorCode(UtilException.Reason reason) {

        return switch (reason) {
            case FILE_EMPTY -> ErrorCode.IMAGE_FILE_EMPTY;
            case FILE_TOO_LARGE -> ErrorCode.IMAGE_TOO_LARGE;
            case TYPE_NOT_ALLOWED -> ErrorCode.IMAGE_TYPE_NOT_ALLOWED;

            case IMAGE_PROCESS_FAILED -> ErrorCode.IMAGE_PROCESS_FAILED;

            case S3_UPLOAD_FAILED -> ErrorCode.S3_UPLOAD_FAILED;
            case S3_DELETE_FAILED -> ErrorCode.S3_DELETE_FAILED;
        };
    }
}
