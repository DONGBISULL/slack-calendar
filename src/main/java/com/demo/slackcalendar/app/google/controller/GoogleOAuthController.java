package com.demo.slackcalendar.app.google.controller;

import com.demo.slackcalendar.app.google.service.GoogleOAuthService;
import com.demo.slackcalendar.commons.exception.GoogleOAuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/google")
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthController {

    private final Map<String, Map<String, Object>> googleStateStore;

    private final GoogleOAuthService service;

    @GetMapping("/auth")
    protected String auth(@RequestParam(required = false) String code,
                          @RequestParam(required = false) String state,
                          @RequestParam(required = false) String error,
                          Model model
    ) throws GoogleOAuthException {
        log.info(" code {} status : {} ", code, state);
        Map<String, Object> data = googleStateStore.get(state);

        if (error != null) {
           throw new GoogleOAuthException(error);
        }

        if (data == null) {
            throw new GoogleOAuthException("잘못된 state " + state);
        }
        Map<String, Object> result = service.processOAuth(code, state);
        Boolean isOk = (Boolean) result.get("ok");
        if (isOk) {
            String teamId = (String) result.get("teamId");
            model.addAttribute("teamId", teamId);
            return "google/success";
        }
        return "/google/fail";
    }

}
