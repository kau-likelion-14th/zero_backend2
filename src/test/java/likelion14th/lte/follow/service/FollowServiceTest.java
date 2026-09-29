package likelion14th.lte.follow.service;

import likelion14th.lte.User.entity.User;
import likelion14th.lte.User.repository.UserRepository;
import likelion14th.lte.follow.dto.FollowUserResponse;
import likelion14th.lte.follow.repository.FollowRepository;
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
}
