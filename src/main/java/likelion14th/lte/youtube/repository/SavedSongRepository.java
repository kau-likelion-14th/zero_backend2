package likelion14th.lte.youtube.repository;

import likelion14th.lte.User.entity.User;
import likelion14th.lte.youtube.domain.SavedSong;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface SavedSongRepository extends JpaRepository<SavedSong, Long>{

    boolean existsByUserAndSongId(User user, String songId);
    Optional<SavedSong> findByUserAndSongId(User user, String songId);
    List<SavedSong> findAllByUserOrderBySavedAtDesc(User user);

}
