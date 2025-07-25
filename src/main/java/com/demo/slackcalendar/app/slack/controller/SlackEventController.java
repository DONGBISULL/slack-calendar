package com.demo.slackcalendar.app.slack.controller;

import com.demo.slackcalendar.app.slack.service.SlackEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/slack")
@RequiredArgsConstructor
@Slf4j
public class SlackEventController {

    private final SlackEventService service;

    @PostMapping("/events")
    public ResponseEntity<?> receiveSlackEvent(@RequestBody Map<String, Object> payload) {
        String type = (String) payload.get("type");
        if (type == null) {
            throw new IllegalArgumentException("슬랙 메시지 타입 오류");
        }

        if ("url_verification".equals(type)) {
            String challenge = (String) payload.get("challenge");
            return ResponseEntity.ok(challenge);
        }

        if ("event_callback".equals(type)) {
            Map<String, Object> eventInfo = (Map<String, Object>) payload.get("event");
            log.info(" {} ", eventInfo);
            String eventType = (String) eventInfo.get("type");
            switch (eventType) {
                case "member_joined_channel":
                    service.invite(eventInfo);
                    break;
            }
        }

        return ResponseEntity.ok(payload);
    }

    /* application/x-www-form-urlencoded;charset=UTF-8 ? */
    @PostMapping("/command")
    public ResponseEntity<?> receiveSlackCommand(@RequestBody MultiValueMap<String, Object> payload) {
        log.info(" {} ", payload);
        List<Object> command = payload.get("command");

        if (command == null || command.size() == 0 || StringUtils.hasText(command.get(0).toString())) {
            throw new IllegalArgumentException("잘못된 명령어 입력");
        }

        String commandTarget = command.get(0).toString();
        if ("/calendar-connect".equals(commandTarget)) {

        }

        return ResponseEntity.ok(payload);
    }

}