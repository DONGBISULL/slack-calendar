package com.demo.slackcalendar.app.slack.controller;

import com.demo.slackcalendar.app.slack.dto.SlackOAuthProperties;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/slack")
@RequiredArgsConstructor
@Slf4j
public class SlackRouterController {

    private final Map<String, Instant> stateStore;

    private final SlackOAuthProperties properties;

    @GetMapping("/install")
    protected String index(Model model) {
        String state = UUID.randomUUID().toString();
        log.info(" >>>>> state : {}", state);
        stateStore.put(state, Instant.now());
        String installUrl = properties.buildAuthorizeUrl(state);
        model.addAttribute("installUrl", installUrl);
        return "/slack/install";
    }

    @GetMapping("/success")
    protected String index() {
        return "/slack/success";
    }


}
