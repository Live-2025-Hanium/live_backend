package com.example.live_backend.domain.example.entity;

import com.example.live_backend.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Deprecated
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "socialProvider", nullable = false)
    @Enumerated(EnumType.STRING)
    private SocialProvider socialProvider;

    @Column(name = "socialId", nullable = false)
    private String socialId;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "gender", nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.MEMBER;

    @Column(name = "profileImage")
    private String profileImage;

    @Column(name = "cloverBalance", nullable = false)
    private int cloverBalance = 0;

    @Column(name = "deletedAt")
    private LocalDateTime deletedAt= null;


    public enum SocialProvider {
        KAKAO, GOOGLE
    }

    public enum Gender {
        FEMALE, MALE, OTHER
    }

    public enum Role {
        ADMIN, MEMBER
    }

    @Builder
    public User(String email, SocialProvider socialProvider, String socialId, String nickname, Gender gender, LocalDate birthdate, Role role, String profileImage, int cloverBalance,  LocalDateTime deletedAt) {
        this.email = email;
        this.socialProvider = socialProvider;
        this.socialId = socialId;
        this.nickname = nickname;
        this.gender = gender;
        this.birthdate = birthdate;
        this.role = role;
        this.profileImage = profileImage;
        this.cloverBalance = cloverBalance;
        this.deletedAt = deletedAt;
    }

}
