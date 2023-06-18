package com.zerototen.savegame.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sun.istack.NotNull;
import com.zerototen.savegame.domain.Member;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MemberDto implements Serializable {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeleteDto {
        private String email;
        private String password;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter
    @Builder
    public static class SaveDto {

        @ApiModelProperty(hidden = true)
        private Long id;
        private String nickname;
        private String email;
        private String password;
        private String userImage;
        @ApiModelProperty(hidden = true)
        private String accessToken;
        @ApiModelProperty(hidden = true)
        private String refreshToken;

        public static SaveDto response(Member member, String accessToken, String refreshToken) {
            return SaveDto.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Getter
    @Builder
    public static class LoginDto {

        private String email;
        private String password;
        @ApiModelProperty(hidden = true)
        private String accessToken;
        @ApiModelProperty(hidden = true)
        private String refreshToken;

        public static LoginDto response(String accessToken, String refreshToken) {
            return LoginDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        }
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class socialLoginResponse {

        private final String status;
        private final String name;
        private final String email;
        private final String img;
        private final String atk;
        private final String rtk;

        public static socialLoginResponse response(String name, String email, String img,
            String atk, String rtk, String status) {
            return socialLoginResponse.builder()
                .status(status)
                .name(name)
                .email(email)
                .img(img)
                .atk(atk)
                .rtk(rtk)
                .build();
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class DetailDto {

        private Long id;
        private String nickname;
        private String email;
        private String imageUrl;

        public static DetailDto response(@NotNull Member member) {
            return DetailDto.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .imageUrl(member.getImageUrl())
                .build();
        }
    }

}