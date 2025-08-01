package com.demo.slackcalendar.app.google.service;

import com.demo.slackcalendar.app.google.dto.GoogleOAuthProperties;
import com.demo.slackcalendar.app.google.dto.response.GoogleOAuthResponse;
import com.demo.slackcalendar.app.slack.entity.SlackUser;
import com.demo.slackcalendar.app.slack.repository.SlackUserRepository;
import com.demo.slackcalendar.commons.exception.GoogleOAuthException;
import com.demo.slackcalendar.commons.service.VaultPathCacheService;
import com.demo.slackcalendar.commons.service.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class GoogleOAuthService {

    private WebClient webClient;

    private Map<String, Map<String, Object>> googleStateStore;

    private SlackUserRepository repository;

    private VaultService service;
    private VaultPathCacheService pathService;

    @Value("${google.oauth.client-id}")
    private String clientId;
    @Value("${google.oauth.client-secret}")
    private String clientSecret;
    @Value("${google.oauth.redirect-uri}")
    private String redirectUri;

    private GoogleOAuthProperties properties;

    public GoogleOAuthService(WebClient.Builder builder,
                              Map<String, Map<String, Object>> googleStateStore,
                              SlackUserRepository repository,
                              VaultService service,
                              VaultPathCacheService pathService, GoogleOAuthProperties properties) {
        this.webClient = builder
                .baseUrl("https://oauth2.googleapis.com")
                .build();
        this.googleStateStore = googleStateStore;
        this.repository = repository;
        this.service = service;
        this.pathService = pathService;
        this.properties = properties;
    }

    public Map<String, Object> processOAuth(String code, String state) throws GoogleOAuthException {
        Map<String, Object> authInfo = googleStateStore.get(state);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);

        Mono<GoogleOAuthResponse> response = webClient.post()
                .uri(uriBuilder ->
                        uriBuilder.path("/token").build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                .bodyToMono(GoogleOAuthResponse.class);

        CompletableFuture<GoogleOAuthResponse> future = response.toFuture();
        try {
            GoogleOAuthResponse result = future.get(10, TimeUnit.SECONDS);
            saveGoogleInfo(result, authInfo);
            Map<String, Object> val = new HashMap<>();
            val.putAll(authInfo);
            val.put("ok", true);
            return val;
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            throw new GoogleOAuthException("인증 실패");
        } catch (ExecutionException e) {
            log.error(e.getMessage());
            throw new GoogleOAuthException("작업이 중단되었습니다");
        } catch (TimeoutException e) {
            log.error("작업 제한 시간({} 초) 초과", 10, e);
            throw new GoogleOAuthException("작업 처리 시간이 초과되었습니다");
        }
    }

    public void saveGoogleInfo(GoogleOAuthResponse response, Map<String, Object> authInfo) throws GoogleOAuthException {
        String teamId = authInfo.get("teamId").toString();
        String userId = authInfo.get("userId").toString();
        Optional<SlackUser> user = repository.findByTeamIdAndUserId(teamId, userId);

        if (user.isPresent()) {
            String vaultPath = pathService.getVaultPath("google-data-path");
            String path = vaultPath + "/" + userId;
            Map<String, Object> googleData = buildRequestData(response);
            log.info(" vaultPath {} ", vaultPath);
            service.writeSecret(path, Map.of("data", googleData));
        } else {
            throw new GoogleOAuthException("존재하지 않는 사용자입니다");
        }

        log.info(" response {} ", response.toString());
    }

    private Map<String, Object> buildRequestData(GoogleOAuthResponse response) {
        Map<String, Object> googleData = Map.of(
                "accessToken", response.getAccessToken(),
                "refreshToken", response.getRefreshToken(),
                "expiresIn", response.getExpiresIn(),
                "tokenType", response.getTokenType(),
                "scope", response.getScope()
        );
        return googleData;
    }

    public String createState(String teamId, String userId) {
        String stateToken = UUID.randomUUID().toString();

        Map<String, Object> stateData = Map.of(
                "teamId", teamId,
                "userId", userId,
                "createdAt", System.currentTimeMillis()
        );

        googleStateStore.put(stateToken, stateData);
        log.debug("State token 생성: {}", stateToken);

        return stateToken;
    }

    // 5분 지난 항목 삭제
    @Scheduled(fixedRate = 300000)
    private void cleanupExpiredStates() {
        long now = System.currentTimeMillis();
        googleStateStore.entrySet().removeIf(entry -> {
            long createdAt = (long) entry.getValue().get("createdAt");
            return now - createdAt > 300000;
        });
    }

    /**
     * 구글 볼트에서 구글 Auth 가 기존의 요소와 다른 경우 스케줄러로 권한 확인 프로세스
     */
    @Scheduled(fixedDelay = 6000 * 10)
    public void googleScopeCheck() {
        String vaultPath = pathService.getVaultPath("google-data-path");

        log.info(" vaultPath {}", vaultPath);

//        Map<String, Map<String, Object>> result = service.listSecrets(vaultPath);
//        log.info(" result {} ", result);

        List<String> scopes = properties.getScopes();
        log.info(" scopes {}", scopes);
    }

}
