package likelion14th.lte.User.entity;


import jakarta.persistence.*;
import likelion14th.lte.Entity.BaseEntity;
import likelion14th.lte.follow.entity.Follow;
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

    @OneToMany(mappedBy = "fromUser", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followings;


    @Builder(access = AccessLevel.PUBLIC)
    private User (String username, String userTag, String introduction){
        this.username = username;
        this.userTag = userTag;
        this.introduction = introduction;
        this.followers = new ArrayList<>();
        this.followings = new ArrayList<>();
    }

    public void updateIntroduction(String introduction){
        this.introduction = introduction;
    }
}
