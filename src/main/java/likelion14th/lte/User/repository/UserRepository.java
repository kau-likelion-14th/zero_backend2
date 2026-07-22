package likelion14th.lte.User.repository;

import likelion14th.lte.User.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);
    Page<User> findByUsernameContainingIgnoreCase(String nickName, Pageable pageable);
    Optional<User> findByUserTag(String UserTag);

    @Query("SELECT u FROM User u " +
            "WHERE u.id != :userId " +
            "AND NOT EXISTS (SELECT f FROM Follow f WHERE f.fromUser.id = :userId AND f.toUser.id = u.id)")
    Page<User> findCanFollowUsers(@Param("userId") Long userId, Pageable pageable);
}