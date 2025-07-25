package com.demo.slackcalendar.app.slack.service;

import com.demo.slackcalendar.commons.service.VaultPathCacheService;
import com.demo.slackcalendar.commons.service.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@Slf4j
public class SlackEventService {

    private VaultService service;

    private WebClient webClient;

    private VaultPathCacheService pathService;

    @Value("${slack.bot-token}")
    private String botToken;

    public SlackEventService(VaultService service, WebClient.Builder webClientBuilder, VaultPathCacheService pathService) {
        this.service = service;
        this.webClient = webClientBuilder
                .baseUrl("https://slack.com/api/")
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.pathService = pathService;
    }

    public void invite(Map<String, Object> eventInfo) {
        String channel = (String) eventInfo.get("channel");
        Map<String, String> block = Map.of(
                "text", "추가되었다는 알림 처리",
                "channel", channel
        );

        webClient.post()
                .uri("chat.postMessage")
                .header("Authorization", "Bearer " + botToken)
                .bodyValue(block)
                .retrieve()
                .bodyToMono(String.class)
                // 에러 발생 시 대체할 수 있는 새로운 데이터 스트림
                .onErrorResume(e -> {
                    log.error(e.getMessage());
                    return Mono.just("Slack 앱 에러");
                }).subscribe()
        ;
    }

}
