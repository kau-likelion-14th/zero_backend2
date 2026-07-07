package likelion14th.lte.follow.repository;

import likelion14th.lte.follow.entity.Follow;
import likelion14th.lte.User.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFromUserAndToUser(User fromUser, User toUser);

    Optional<Follow> findByFromUserAndToUser(User fromUser, User toUser);
}
