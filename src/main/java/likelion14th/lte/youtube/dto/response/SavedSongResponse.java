package likelion14th.lte.youtube.dto.response;

import likelion14th.lte.youtube.domain.SavedSong;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class SavedSongResponse {

    private Long id;
    private String songId;
    private String title;
    private String artist;
    private String imageUrl;
    private Long durationMs;
    private LocalDate savedAt;

    public static SavedSongResponse from(SavedSong song) {
        return SavedSongResponse.builder()
                .id(song.getId())
                .songId(song.getSongId())
                .title(song.getTitle())
                .artist(song.getArtist())
                .imageUrl(song.getImageUrl())
                .durationMs(song.getDurationMs())
                .savedAt(song.getSavedAt())
                .build();
    }
}