package com.demo.slackcalendar.commons.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "vault.mount-paths")
@Data
@Component
public class VaultMountPathProperties {

    private Map<String, VaultPath> contexts = new HashMap<>();

    @Data
    public static class VaultPath {
        private String name;
        private String path;
    }
}
