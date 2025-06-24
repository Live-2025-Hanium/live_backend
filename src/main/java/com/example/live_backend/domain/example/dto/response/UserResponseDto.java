package com.example.live_backend.domain.example.dto.response;

import com.example.live_backend.domain.example.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private User.Gender gender;
    private LocalDate birthdate;
    private User.Role role;
    private String profileImage;
    private int cloverBalance;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.gender = user.getGender();
        this.birthdate = user.getBirthdate();
        this.role = user.getRole();
        this.profileImage = user.getProfileImage();
        this.cloverBalance = user.getCloverBalance();
    }
}
