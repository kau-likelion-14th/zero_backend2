package likelion14th.lte.follow.service;

import likelion14th.lte.User.entity.User;
import likelion14th.lte.User.repository.UserRepository;
import likelion14th.lte.follow.dto.FollowUserResponse;
import likelion14th.lte.follow.entity.Follow;
import likelion14th.lte.follow.repository.FollowRepository;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public FollowUserResponse followUser(Long fromUserId, Long toUserId) {
        User fromUser = getUser(fromUserId);
        User toUser = getTargetUser(toUserId);

        if (fromUser.getId().equals(toUser.getId())) {
            throw new GeneralException(ErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }

        if (followRepository.existsByFromUserAndToUser(fromUser, toUser)) {
            throw new GeneralException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        Follow follow = Follow.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .build();

        followRepository.save(follow);
        return FollowUserResponse.from(toUser);
    }

    @Transactional
    public void unfollowUser(Long fromUserId, Long toUserId) {
        User fromUser = getUser(fromUserId);
        User toUser = getTargetUser(toUserId);

        if (fromUser.getId().equals(toUser.getId())) {
            throw new GeneralException(ErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }

        Follow follow = followRepository.findByFromUserAndToUser(fromUser, toUser)
                .orElseThrow(() -> new GeneralException(ErrorCode.FOLLOW_NOT_FOUND));

        followRepository.delete(follow);
    }

    @Transactional(readOnly = true)
    public List<FollowUserResponse> getFollowers(Long userId) {
        User user = getUser(userId);
        return followRepository.findByToUser(user).stream()
                .map(follow -> FollowUserResponse.from(follow.getFromUser()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FollowUserResponse> getFollowings(Long userId) {
        User user = getUser(userId);
        return followRepository.findByFromUser(user).stream()
                .map(follow -> FollowUserResponse.from(follow.getToUser()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<FollowUserResponse> getCanFollowUsers(Long userId, Pageable pageable) {
        getUser(userId);
        return userRepository.findCanFollowUsers(userId, pageable)
                .map(FollowUserResponse::from);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));
    }

    private User getTargetUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.FOLLOW_TARGET_NOT_FOUND));
    }
}
