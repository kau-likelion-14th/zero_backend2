package likelion14th.lte.follow.service;

import likelion14th.lte.follow.dto.FollowUserResponse;
import likelion14th.lte.follow.dto.UserNameDto;
import likelion14th.lte.follow.entity.Follow;
import likelion14th.lte.follow.repository.FollowRepository;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.User.entity.User;
import likelion14th.lte.User.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;


    private static UserNameDto nameParsing(String name){
        if(!name.contains("#")){
            return new UserNameDto(name,null);
        }
        String[] parts = name.split("#",2);
        if(parts[1].length()!=8){
            throw new GeneralException(ErrorCode.INVALID_HANDLE_FORMAT);
        }
        return new UserNameDto(parts[0],parts[1]);
    }

    @Transactional
    public FollowUserResponse followUser(Long fromUserId, Long toUserId){
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new GeneralException(ErrorCode.FOLLOW_TARGET_NOT_FOUND));

        if(fromUser.getId().equals(toUser.getId())){
            throw new GeneralException((ErrorCode.FOLLOW_SELF_NOT_ALLOWED));
        }
        if(followRepository.existsByFromUserAndToUser(fromUser,toUser)){
            throw new GeneralException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        Follow follow = Follow.builder()
                .toUser(toUser)
                .fromUser(fromUser)
                .build();

        followRepository.save(follow);

        return FollowUserResponse.from(follow.getToUser());
    }

    @Transactional(readOnly = true)
    public Page<FollowUserResponse> searchCanFollowers(Long userId, String targetName, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        UserNameDto nameDto = nameParsing(targetName);
        Page<User> users;

        if(nameDto.userTag() != null && !nameDto.userTag().isEmpty()){
            User target = userRepository.findByUserTag(nameDto.userTag()).orElse(null);
            if(target == null){
                return Page.empty(pageable);
            }
            users = new PageImpl<>(List.of(target),pageable,1);
        } else {
            users = userRepository.findByUsernameContainingIgnoreCase(nameDto.userName(), pageable);
        }
        List<User> canFollowUsers = users.getContent()
                .stream()
                .filter(target -> !target.getId().equals(userId))
                .filter(target -> !followRepository.existsByFromUserAndToUser(user, target))
                .toList();

        return new PageImpl<>(canFollowUsers, pageable, canFollowUsers.size())
                .map(FollowUserResponse::from);
    }

}
