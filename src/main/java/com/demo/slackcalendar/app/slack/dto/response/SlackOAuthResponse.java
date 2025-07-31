package com.demo.slackcalendar.app.slack.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackOAuthResponse {

    // 상태

    @JsonProperty("ok")
    private Boolean ok;
    // 액세스 토큰

    @JsonProperty("access_token")
    private String accessToken;
    // 토큰 타입

    @JsonProperty("token_type")
    private String tokenType;
    // 허용 영역
    @JsonProperty("scope")
    private String scope;

    @JsonProperty("bot_user_id")
    private String botUserId;

    // 앱 ID
    @JsonProperty("app_id")
    private String appId;

    // 워크 스페이스 정보
    private Group team;

    // 엔터프라이즈 정보
    private Group enterprise;

    // 사용자 계정 정보
    @JsonProperty("authed_user")
    private SlackOAuthUser authedUser;

    @Getter
    @Setter
    public static class SlackOAuthUser {
        String id;
        String scope;
        @JsonProperty("access_token")
        String accessToken;
        @JsonProperty("token_type")
        String tokenType;
    }

    @Getter
    @Setter
    public static class Group {
        String name;
        String id;
    }

}
