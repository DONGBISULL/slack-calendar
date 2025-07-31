package com.demo.slackcalendar.app.slack.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@ConfigurationProperties("slack.oauth")
@Data
@Component
public class SlackOAuthProperties {

    private String clientId;
    private String redirectUri;
    private String authorizeUrl;
    private String tokenUrl;

    private List<String> botScopes;
    private List<String> userScopes;

    public String buildAuthorizeUrl() {
        return buildAuthorizeUrl(null);
    }

    public String buildAuthorizeUrl(String state) {
        StringBuilder url = new StringBuilder(authorizeUrl);
        url.append("?scope=").append(URLEncoder.encode(String.join(",", botScopes), StandardCharsets.UTF_8));
        url.append("&user_scope=").append(URLEncoder.encode(String.join(",", userScopes), StandardCharsets.UTF_8));
        url.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
        url.append("&client_id=").append(clientId);

        if (state != null && !state.isEmpty()) {
            url.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
        }

        return url.toString();
    }
}
