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
    public ApiResponse<SavedSongResponse> save(@Valid @RequestBody SongSaveRequest req){
        return ApiResponse.onSuccess(SuccessCode.OK, youTubeService.saveSong(req.getSongId()));
    }

    @GetMapping("/me")
    @Operation(summary ="내가 저장한 곡 조회")
    public ApiResponse<List<SavedSongResponse>> myList(){
        return ApiResponse.onSuccess(SuccessCode.OK, youTubeService.mySavedSongs());
    }

    @DeleteMapping("/save/{songId}")
    @Operation(summary = "저장된 곡 삭제")
    public ApiResponse<Void> delete(@PathVariable String songId){
        youTubeService.deleteSavedSong(songId);
        return ApiResponse.onSuccess(SuccessCode.OK, null);
    }
}
