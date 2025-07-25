package com.demo.slackcalendar.commons.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class VaultPathCacheService {

    /**
     * 자기 자신에 있는 함수로 실행하면 캐싱 처리 안됨 이를 위해 self 주입
     * - AOP 방식을 통하여 작동하고 있어 자기 메서드 호출 시 인식 안됨
     */
    @Lazy
    @Autowired
    private VaultPathCacheService self;

    private final VaultService service;

    @Value("${vault.config-path}")
    private String configPath;

    @Cacheable("vaultPathMappings")
    public Map<String, Object> getAllPaths() {
        try {
            log.info("Loading Vault path mappings from {}", configPath);
            Map<String, Object> valueMap = service.getSecrets(configPath);
            log.info("vaultPathMappings: {}", valueMap.size());
            return valueMap;
        } catch (Exception e) {
            log.error("vaultPathMappings: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    public String getVaultPath(String key) {
        if (!StringUtils.hasText(key)) {
            log.error("target key is  null");
            return null;
        }
        Map<String, Object> allPaths = self.getAllPaths();
        if (allPaths == null) {
            log.error("vaultPathMappings is null");
            return null;
        }
        return allPaths.get(key).toString();
    }

    @Scheduled(fixedDelay = 60000 * 2)
    @CacheEvict(value = "vaultPathMappings", allEntries = true)
    public void refreshVaultPath() {
        log.info("Scheduled vault path cache refresh");
    }

    /* 추후 호출용 메서드로 사용 */
    @CacheEvict(value = "vaultPathMappings", allEntries = true)
    public void forceVaultPath() {
        log.info("Force vault path cache refresh requested");
    }


}
