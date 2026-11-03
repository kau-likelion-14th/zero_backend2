package likelion14th.lte.follow.service;

import likelion14th.lte.user.entity.User;
import likelion14th.lte.user.repository.UserRepository;
import likelion14th.lte.follow.dto.FollowUserResponse;
import likelion14th.lte.follow.repository.FollowRepository;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FollowService followService;

    @Test
    void getCanFollowUsers_returnsPagedFriendPageForAuthenticatedUser() {
        User loggedInUser = User.builder()
                .username("me")
                .userTag("ME000001")
                .build();
        User candidate = User.builder()
                .username("friend")
                .userTag("FRIEND01")
                .introduction("안녕하세요")
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findById(1L)).thenReturn(Optional.of(loggedInUser));
        when(userRepository.findCanFollowUsers(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(candidate), pageable, 1));

        Page<FollowUserResponse> result = followService.getCanFollowUsers(1L, pageable);

        verify(userRepository).findCanFollowUsers(1L, pageable);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getUserName()).isEqualTo("friend#FRIEND01");
    }

    @Test
    void searchUsers_trimsNicknameAndMapsPagedUsers() {
        Pageable pageable = PageRequest.of(1, 10);
        User me = User.builder().username("me").userTag("ME").build();
        User candidate = User.builder().username("전성환").userTag("1234").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));
        when(userRepository.searchCanFollowUsers(1L, "전성", pageable))
                .thenReturn(new PageImpl<>(List.of(candidate), pageable, 11));

        Page<FollowUserResponse> result = followService.searchUsers(1L, "  전성  ", pageable);

        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent().getFirst().getUserName()).isEqualTo("전성환#1234");
    }

    @Test
    void searchUsers_escapesWildcardsBeforeDatabaseSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().build()));
        when(userRepository.searchCanFollowUsers(1L, "!%!!!_", pageable)).thenReturn(Page.empty(pageable));

        assertThat(followService.searchUsers(1L, "%!_", pageable)).isEmpty();
        verify(userRepository).searchCanFollowUsers(1L, "!%!!!_", pageable);
    }

    @Test
    void searchUsers_rejectsBlankNickname() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().build()));
        assertThatThrownBy(() -> followService.searchUsers(1L, "   ", PageRequest.of(0, 10)))
                .isInstanceOfSatisfying(GeneralException.class,
                        error -> assertThat(error.getCode()).isEqualTo(ErrorCode.BAD_REQUEST));
        verify(userRepository, never()).searchCanFollowUsers(anyLong(), anyString(), any());
    }

    @Test
    void searchUsers_rejectsUnknownAuthenticatedUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> followService.searchUsers(1L, "친구", PageRequest.of(0, 10)))
                .isInstanceOfSatisfying(GeneralException.class,
                        error -> assertThat(error.getCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
    }
}
