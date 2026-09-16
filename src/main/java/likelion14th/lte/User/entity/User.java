package likelion14th.lte.User.entity;


import jakarta.persistence.*;
import likelion14th.lte.Entity.BaseEntity;
import likelion14th.lte.follow.entity.Follow;
import likelion14th.lte.login.domain.RefreshToken;
import likelion14th.lte.statistic.entity.Statistic;
import likelion14th.lte.youtube.domain.SavedSong;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class User extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(unique = true)
    private String providerId;

    @Column(length = 16, nullable = false,unique = true)
    private String userTag;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(columnDefinition = "TEXT")
    private String profileImage;

    @Column(columnDefinition = "TEXT")
    private String s3ImageKey;

    @OneToMany(mappedBy = "toUser",fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<likelion14th.lte.follow.entity.Follow> followers;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SavedSong> savedSongs;

    @OneToMany(mappedBy = "fromUser", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followings;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "statistic_id", unique = true)
    private Statistic statistic;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private RefreshToken refreshToken;

    @Builder(access = AccessLevel.PUBLIC)
    private User (String username, String userTag, String introduction, String providerId){
        this.username = username;
        this.userTag = userTag;
        this.providerId = providerId;
        this.introduction = introduction;
        this.followers = new ArrayList<>();
        this.followings = new ArrayList<>();
        this.statistic = Statistic.create();
        this.savedSongs = new ArrayList<>();
    }

    public void updateIntroduction(String introduction){
        this.introduction = introduction;
    }

    public void initializeStatisticIfAbsent() {
        if (statistic == null) {
            statistic = Statistic.create();
        }
    }
}