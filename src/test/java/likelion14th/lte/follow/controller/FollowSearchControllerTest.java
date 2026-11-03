package likelion14th.lte.follow.controller;

import likelion14th.lte.follow.dto.FollowUserResponse;
import likelion14th.lte.follow.service.FollowService;
import likelion14th.lte.todo.service.TodoCalendarService;
import likelion14th.lte.todo.service.TodoService;
import likelion14th.lte.user.entity.User;
import likelion14th.lte.user.service.UserProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FollowSearchControllerTest {
    private FollowService service;
    private MockMvc mvc;
    private UserProfileService profiles;

    @BeforeEach
    void setUp() {
        service = mock(FollowService.class);
        profiles = mock(UserProfileService.class);
        // 실제 상세 컨트롤러도 함께 등록하여 /search가 /{followId}로 매칭되는 회귀를 검증합니다.
        mvc = MockMvcBuilders.standaloneSetup(new FollowController(service),
                        new FollowDetailController(profiles, mock(TodoCalendarService.class), mock(TodoService.class)))
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType() == Jwt.class;
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container,
                                                  NativeWebRequest request, WebDataBinderFactory factory) {
                        Instant now = Instant.now();
                        return new Jwt("test-token", now, now.plusSeconds(60),
                                Map.of("alg", "HS256"), Map.of("sub", "1", "type", "ACCESS"));
                    }
                }, new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void searchRouteReturnsExpectedPageInsteadOfTreatingSearchAsFollowId() throws Exception {
        User user = User.builder().username("전성환").userTag("1234").introduction("반갑습니다").build();
        ReflectionTestUtils.setField(user, "id", 42L);
        var pageable = PageRequest.of(0, 10);
        when(service.searchUsers(1L, "전성", pageable))
                .thenReturn(new PageImpl<>(List.of(FollowUserResponse.from(user)), pageable, 1));

        mvc.perform(get("/api/follow/search").param("nickname", "전성").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.content[0].userId").value(42))
                .andExpect(jsonPath("$.result.content[0].userName").value("전성환#1234"))
                .andExpect(jsonPath("$.result.totalPages").value(1))
                .andExpect(jsonPath("$.result.number").value(0));
        verifyNoInteractions(profiles);
    }

    @Test
    void searchRequiresNicknameParameter() throws Exception {
        mvc.perform(get("/api/follow/search")).andExpect(status().isBadRequest());
        verifyNoInteractions(service, profiles);
    }

    @Test
    void searchDefaultsToFirstPageOfTenUsers() throws Exception {
        var pageable = PageRequest.of(0, 10);
        when(service.searchUsers(1L, "없는이름", pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        mvc.perform(get("/api/follow/search").param("nickname", "없는이름"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content").isEmpty())
                .andExpect(jsonPath("$.result.totalPages").value(0));
        verify(service).searchUsers(1L, "없는이름", pageable);
    }
}
