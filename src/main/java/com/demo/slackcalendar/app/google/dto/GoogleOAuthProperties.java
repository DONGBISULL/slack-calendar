package com.demo.slackcalendar.app.google.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@ConfigurationProperties("google.oauth")
@Data
@Component
public class GoogleOAuthProperties {

    private String authorizeUrl;
    private String clientId;
    private String redirectUri;
    private String responseType;
    private String accessType;
    private String prompt;
    private List<String> scopes;

    public String buildAuthorizeUrl() {
        return buildAuthorizeUrl(null);
    }

    public String buildAuthorizeUrl(String state) {
        StringBuilder url = new StringBuilder(authorizeUrl);
        url.append("?scope=").append(URLEncoder.encode(String.join(" ", scopes), StandardCharsets.UTF_8));
        url.append("&access_type=").append(URLEncoder.encode(accessType, StandardCharsets.UTF_8));
        url.append("&response_type=").append(URLEncoder.encode(responseType, StandardCharsets.UTF_8));

        if (state != null) {
            url.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
        }
        url.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
        url.append("&client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8));
        url.append("&prompt=").append(URLEncoder.encode(prompt, StandardCharsets.UTF_8));
        return url.toString();
    }

}
