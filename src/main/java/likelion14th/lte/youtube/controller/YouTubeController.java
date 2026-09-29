package likelion14th.lte.youtube.controller;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.SuccessCode;
import likelion14th.lte.youtube.dto.request.SongSaveRequest;
import likelion14th.lte.youtube.dto.response.SavedSongResponse;
import likelion14th.lte.youtube.dto.response.YouTubeSongItemResponse;
import likelion14th.lte.youtube.service.YouTubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/youtube")
public class YouTubeController {
    private final YouTubeService youTubeService;

    @GetMapping("/search")
    @Operation(summary = "유튜브 음악 검색")
    public ApiResponse<List<YouTubeSongItemResponse>> serach(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit){
        return ApiResponse.onSuccess(SuccessCode.OK, youTubeService.searchSongs(q, limit));
    }

    @PostMapping("/save")
    @Operation(summary = "곡 저장")
    public ApiResponse<SavedSongResponse> save(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SongSaveRequest req
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ApiResponse.onSuccess(SuccessCode.OK, youTubeService.saveSong(userId, req.getSongId()));
    }

    @GetMapping("/me")
    @Operation(summary ="내가 저장한 곡 조회")
    public ApiResponse<List<SavedSongResponse>> myList(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ApiResponse.onSuccess(SuccessCode.OK, youTubeService.mySavedSongs(userId));
    }

    @DeleteMapping("/save/{songId}")
    @Operation(summary = "저장된 곡 삭제")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String songId
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        youTubeService.deleteSavedSong(userId, songId);
        return ApiResponse.onSuccess(SuccessCode.OK, null);
    }
}
