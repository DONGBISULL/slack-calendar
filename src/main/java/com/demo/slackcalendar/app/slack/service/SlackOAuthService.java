package com.demo.slackcalendar.app.slack.service;

import com.demo.slackcalendar.app.slack.dto.response.SlackOAuthResponse;
import com.demo.slackcalendar.app.slack.entity.SlackUser;
import com.demo.slackcalendar.app.slack.repository.SlackUserRepository;
import com.demo.slackcalendar.commons.service.VaultPathCacheService;
import com.demo.slackcalendar.commons.service.VaultService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class SlackOAuthService {

    @Value("${slack.oauth.client-secret}")
    private String clientSecret;

    @Value("${slack.oauth.client-id}")
    private String clientId;

    private final VaultService service;

    private WebClient webClient;

    private SlackUserRepository repository;

    private VaultPathCacheService pathService;

    public SlackOAuthService(VaultService service, VaultPathCacheService pathService, WebClient.Builder webClientBuilder, SlackUserRepository repository) {
        this.service = service;
        this.webClient = webClientBuilder
                .baseUrl("https://slack.com/api")
                .build();
        this.repository = repository;
        this.pathService = pathService;
    }

    public SlackOAuthResponse processOAuth(String code) throws ExecutionException, InterruptedException, TimeoutException {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        Mono<SlackOAuthResponse> authResponse = webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/oauth.v2.access").build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                .bodyToMono(SlackOAuthResponse.class);

        try {
            CompletableFuture<SlackOAuthResponse> future = authResponse.toFuture();
            SlackOAuthResponse slackOAuthResponse = future.get(10, TimeUnit.SECONDS);

            if (slackOAuthResponse != null && slackOAuthResponse.getOk()) {
                saveSlackInfo(slackOAuthResponse);
            } else {
                throw new RuntimeException("슬랙 인증 실패");
            }
            return slackOAuthResponse;
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            throw new RuntimeException("작업이 중단되었습니다");
        } catch (ExecutionException e) {
            log.error(e.getMessage());
            throw new RuntimeException("작업 처리 중 오류가 발생했습니다");
        } catch (TimeoutException e) {
            log.error("작업 제한 시간({} 초) 초과", 10, e);
            throw new RuntimeException("작업 처리 시간이 초과되었습니다");
        }
    }

    @Transactional
    public void saveSlackInfo(SlackOAuthResponse target) {
        SlackOAuthResponse.Group team = target.getTeam();
        SlackOAuthResponse.SlackOAuthUser authedUser = target.getAuthedUser();

        String id = team.getId();
        String userId = authedUser.getId();
        Optional<SlackUser> user = repository.findByTeamIdAndUserId(id, userId);

        if (!user.isPresent()) {
            SlackUser saved = SlackUser.builder()
                    .teamId(id)
                    .userId(userId)
                    .build();
            repository.save(saved);
        }

        saveSlackVaultInfo(target, authedUser);
    }

    /**
     * vault 에 슬랙 정보 저장
     */
    public void saveSlackVaultInfo(SlackOAuthResponse response, SlackOAuthResponse.SlackOAuthUser authedUser) {
        try {
            String vaultPath = pathService.getVaultPath("slack-path");
            log.info("Vault base path: {}", vaultPath);

            String teamId = response.getTeam().getId();
            String authedUserId = authedUser.getId();

            // Bot Token 저장 (팀 레벨) - 최상위 access_token 사용
            String botTokenPath = vaultPath + "/" + teamId;
            Map<String, Object> botData = Map.of(
                    "team_id", teamId,
                    "team_name", response.getTeam().getName() != null ? response.getTeam().getName() : "",
                    "bot_access_token", response.getAccessToken() != null ? response.getAccessToken() : "", // Bot Token
                    "bot_user_id", response.getBotUserId() != null ? response.getBotUserId() : "",
                    "app_id", response.getAppId() != null ? response.getAppId() : "",
                    "timestamp", System.currentTimeMillis()
            );

            log.info("Bot token 저장 경로: {}", botTokenPath);
            service.writeSecret(botTokenPath, Map.of("data", botData));
            log.info("Bot token 저장 완료");

            // User Token 저장 (사용자 레벨)
            String userTokenPath = vaultPath + "/" + teamId + "/users/" + authedUserId;
            Map<String, Object> userData = Map.of(
                    "user_id", authedUserId,
                    "user_access_token", authedUser.getAccessToken() != null ? authedUser.getAccessToken() : "", // User Token
                    "token_type", authedUser.getTokenType() != null ? authedUser.getTokenType() : "",
                    "scope", authedUser.getScope() != null ? authedUser.getScope() : "",
                    "timestamp", System.currentTimeMillis()
            );

            log.info("User token 저장 경로: {}", userTokenPath);
            service.writeSecret(userTokenPath, Map.of("data", userData));
            log.info("User token 저장 완료");

        } catch (Exception e) {
            log.error("Vault 저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Vault에 Slack 정보 저장 실패", e);
        }
    }

}
