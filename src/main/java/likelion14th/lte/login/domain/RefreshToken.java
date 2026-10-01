package likelion14th.lte.login.domain;

import jakarta.persistence.*;
import likelion14th.lte.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="refresh_token")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length=1000)
    private String refreshToken;

    @Column(nullable = false)
    private Long refreshTokenExpiration;

    @Builder
    public RefreshToken(User user, String refreshToken, Long refreshTokenExpiration) {
        this.user = user;
        this.refreshToken = refreshToken;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
    public void updateToken(String refreshToken, Long refreshTokenExpiration) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }
}
