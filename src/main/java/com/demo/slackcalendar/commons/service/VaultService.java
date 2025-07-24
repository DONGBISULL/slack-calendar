package com.demo.slackcalendar.commons.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaultService {

    private final VaultTemplate vaultTemplate;

    public Map<String, String> getSecrets(String path) {
        try {
            VaultResponse response = vaultTemplate.read(path);
            if (response == null || response.getData() == null) {
                return Collections.emptyMap();
            }
            Map<String, Object> outerData = response.getData();
            return (Map<String, String>) outerData.get("data");
        } catch (Exception e) {
            log.error("Vault read error: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    public String getSecret(String path, String key) {
        return getSecrets(path).get(key);
    }

}
