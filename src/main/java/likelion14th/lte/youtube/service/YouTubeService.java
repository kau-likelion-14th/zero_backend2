package likelion14th.lte.youtube.service;


import likelion14th.lte.User.entity.User;
import likelion14th.lte.User.repository.UserRepository;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.youtube.client.YouTubeClient;
import likelion14th.lte.youtube.domain.SavedSong;
import likelion14th.lte.youtube.dto.response.SavedSongResponse;
import likelion14th.lte.youtube.dto.response.YouTubeSongItemResponse;
import likelion14th.lte.youtube.repository.SavedSongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class YouTubeService {

    private static final String DUMMY_LOGIN_ID = "dummy";
    private static final String DUMMY_USERNAME = "dummy";
    private static final String DUMMY_USER_TAG = "dummy";

    private final YouTubeClient youTubeClient;
    private final SavedSongRepository savedSongRepository;
    private final UserRepository userRepository;

    private User getDummyUser() {
        return userRepository.findByUsername(DUMMY_USERNAME)
                .orElseGet(()-> userRepository.save(
                        User.builder()
                                .username(DUMMY_USERNAME)
                                .userTag(DUMMY_USER_TAG)
                                .introduction("더미 데이터")
                                .build()
                ));
    }

    @Transactional(readOnly = true)
    public List<YouTubeSongItemResponse> searchSongs(String query, int limit){
        JsonNode root = youTubeClient.searchVideoRaw(query, limit);
        JsonNode items = root.path("items");
        List<YouTubeSongItemResponse> result = new ArrayList<>();

        if (!items.isArray()) {
            return result;
        }
        for (JsonNode item : items) {
            String videoId = item.path("id").path("videoId").asText(null);
            if(videoId == null){
                continue;
            }

            JsonNode snippet = item.path("snippet");

            result.add(YouTubeSongItemResponse.builder()
                    .songId(videoId)
                    .title(snippet.path("title").asText(""))
                    .artist(snippet.path("channelTitle").asText(""))
                    .imageUrl(extractThumbnail(snippet))
                    .build()
            );
        }
        return result;
    }

    public SavedSongResponse saveSong(String songId){
        if (songId == null || songId.isBlank()){
            throw new GeneralException(ErrorCode.BAD_REQUEST);
        }
        //TODO: 로그인 기능 구현 후 수정예정
        User user = getDummyUser();

        if (savedSongRepository.existsByUserAndSongId(user, songId)){
            throw new GeneralException(ErrorCode.SONG_ALREADY_SAVED);
        }

        JsonNode item = getFirstVideo(songId);
        JsonNode snippet = item.get("snippet");
        String duration = item.path("contentDetails").path("duration").asText(null);
        SavedSong savedSong = SavedSong.builder()
                .user(user)
                .songId(songId)
                .title(snippet.path("title").asText(""))
                .artist(snippet.path("channelTitle").asText(""))
                .imageUrl(extractThumbnail(snippet))
                .durationMs(parseDurationMs(duration))
                .build();

        return SavedSongResponse.from(savedSongRepository.save(savedSong));

    }

    private JsonNode getFirstVideo(String songId){
        JsonNode root = youTubeClient.getVideoRaw(songId);
        JsonNode items = root.path("items");

        if(!items.isArray() || items.size() == 0){
            throw new GeneralException(ErrorCode.SONG_NOT_FOUND);
        }

        return items.get(0);
    }

    public List<SavedSongResponse> mySavedSongs(){
        User user = getDummyUser();

        return savedSongRepository.findAllByUserOrderBySavedAtDesc(user)
                .stream()
                .map(SavedSongResponse::from)
                .toList();
    }

    public void deleteSavedSong(String songId){
        User user = getDummyUser();

        SavedSong savedSong= savedSongRepository.findByUserAndSongId(user, songId)
                .orElseThrow(()-> new GeneralException(ErrorCode.SONG_NOT_FOUND));

        savedSongRepository.delete(savedSong);
    }

    private String extractThumbnail(JsonNode snippet){
        String imageUrl = snippet.path("thumbnails").path("high").path("url").asText(null);

        if (imageUrl == null || imageUrl.isBlank()){
            imageUrl = snippet.path("thumbnails").path("medium").path("url").asText(null);
        }
        if (imageUrl == null || imageUrl.isBlank()){
            imageUrl = snippet.path("thumbnails").path("default").path("url").asText(null);
        }

        return imageUrl;
    }

    private Long parseDurationMs(String duration){
        if (duration == null || duration.isBlank()){
            return null;
        }
        try {
            return Duration.parse(duration).toMillis();
        } catch (DateTimeParseException e){
            return null;
        }
    }
}
