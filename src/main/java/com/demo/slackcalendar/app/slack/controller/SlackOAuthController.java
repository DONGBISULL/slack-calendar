package com.demo.slackcalendar.app.slack.controller;

import com.demo.slackcalendar.app.google.dto.GoogleOAuthProperties;
import com.demo.slackcalendar.app.google.service.GoogleOAuthService;
import com.demo.slackcalendar.app.slack.dto.response.SlackOAuthResponse;
import com.demo.slackcalendar.app.slack.service.SlackOAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Controller
@RequestMapping("/slack")
@RequiredArgsConstructor
@Slf4j
public class SlackOAuthController {

    private final SlackOAuthService service;
    private final GoogleOAuthProperties properties;
    private final Map<String, Instant> stateStore;
    private final GoogleOAuthService googleOAuthService;

    @GetMapping("/auth")
    protected String auth(@RequestParam("code") String code,
                          @RequestParam("state") String state) throws ExecutionException, InterruptedException, TimeoutException {
        log.info(" code {} state : {} ", code, state);
        Instant createdAt = stateStore.get(state);
        if (createdAt == null || Duration.between(createdAt, Instant.now()).toMinutes() > 5) {
            stateStore.remove(state);
            throw new IllegalStateException("잘못된 state " + state);
        }
//        if (!state.equalsIgnoreCase(state)) {
//            throw new IllegalStateException("위조된 status : slack status is " + slackStatus);
//        }

        // 슬랙 OAuth 처리 및 저장
        SlackOAuthResponse slackOAuthResponse = service.processOAuth(code);

        log.info("slack OAuth Response : {}", slackOAuthResponse);

        String teamId = slackOAuthResponse.getTeam().getId();
        String userId = slackOAuthResponse.getAuthedUser().getId();

        return redirectGoogleOAuth(teamId, userId);
    }

    private String redirectGoogleOAuth(String teamId, String userId) {
        String state = googleOAuthService.createState(teamId, userId);
        String url = properties.buildAuthorizeUrl(state);
        log.info(" url  {} ", url);
        return "redirect:" + url;
    }

    @GetMapping("/login")
    public String initiateGoogleLogin() {
        String s = redirectGoogleOAuth("1", "1");
        log.info("initiate google login : {}", s);
        return "redirect:" + s;
    }

}
