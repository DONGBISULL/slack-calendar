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

    /**
     * vault 에서 특정 경로 추가
     */
    public Map<String, Object> writeSecret(String path, Map<String, Object> payload) {

        VaultResponse response = vaultTemplate.write(path, payload);

        if (response == null || response.getData() == null) {
            throw new VaultException("not allow save secret for path: " + path);
        }

        return response.getData();
    }

    public String getSecret(String path, String key) {
        return getSecrets(path).get(key).toString();
    }

    /**
     * Vault에서 특정 경로의 시크릿 삭제
     */
    public void deleteSecret(String path) {
        try {
            log.info("Vault 시크릿 삭제 시작: {}", path);
            vaultTemplate.delete(path);  // ← 삭제는 delete() 메서드 사용
            log.info("Vault 시크릿 삭제 완료: {}", path);
        } catch (Exception e) {
            log.error("Vault 시크릿 삭제 실패: {} - {}", path, e.getMessage(), e);
            throw new VaultException("Failed to delete secret at path: " + path, e);
        }
    }

    /**
     * Vault에서 특정 경로가 존재하는지 확인
     */
    public boolean pathExists(String path) {
        try {
            VaultResponse response = vaultTemplate.read(path);
            return response != null && response.getData() != null;
        } catch (Exception e) {
            log.debug("경로 존재 확인 실패 (정상적일 수 있음): {} - {}", path, e.getMessage());
            return false;
        }
    }

}
