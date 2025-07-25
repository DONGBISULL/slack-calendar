package com.demo.slackcalendar.commons.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.vault.VaultException;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaultService {

    private final VaultTemplate vaultTemplate;

    public Map<String, Object> getSecrets(String path) {
        VaultResponse response = vaultTemplate.read(path);
        if (response == null || response.getData() == null) {
            throw new VaultException("Secret not found for path: " + path);
        }
        Map<String, Object> outerData = response.getData();
        return (Map<String, Object>) outerData.get("data");
    }

    public Map<String, Object> saveSecret(String path, Map<String, Object> payload) {

        VaultResponse response = vaultTemplate.write(path, payload);

        if (response == null || response.getData() == null) {
            throw new VaultException("not allow save secret for path: " + path);
        }

        return response.getData();
    }

    public String getSecret(String path, String key) {
        return getSecrets(path).get(key).toString();
    }

}
