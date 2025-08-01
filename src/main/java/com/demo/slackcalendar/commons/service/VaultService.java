package com.demo.slackcalendar.commons.service;

import com.demo.slackcalendar.commons.dto.VaultMountPathProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.vault.VaultException;
import org.springframework.vault.core.VaultKeyValueOperations;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class VaultService {

    private final VaultTemplate vaultTemplate;

    private final VaultMountPathProperties properties;

    private final Map<String, VaultKeyValueOperations> operationsMap = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    public VaultService(VaultTemplate vaultTemplate, VaultMountPathProperties properties, ObjectMapper objectMapper) {
        this.vaultTemplate = vaultTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
        initializeOperation();
    }

    private void initializeOperation() {
        for (Map.Entry<String, VaultMountPathProperties.VaultPath> entry : properties.getContexts().entrySet()) {
            log.info("Initializing vault context path: {}", entry.getKey());
            VaultMountPathProperties.VaultPath value = entry.getValue();
            if (value != null && !StringUtils.isBlank(value.getPath())) {
                log.info("Initializing vault value: {}", value);
                VaultKeyValueOperations ops = vaultTemplate.opsForKeyValue(value.getPath(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_2);
                operationsMap.put(entry.getKey(), ops);
            }
        }
    }

    public <T> T getSecrets(String context, String path, Class<T> type) {
        validateInputs(context, path, type);
        VaultKeyValueOperations ops = operationsMap.get(context);
        if (ops == null) {
            log.error("Vault context not found: {}", context);
            throw new VaultException("context not found");
        }
        VaultResponse vaultResponse = ops.get(path);
        if (vaultResponse != null) {
            return convertToType(vaultResponse.getData(), type);
        } else {
            throw new VaultException("secrete not found: " + path);
        }
    }

    private void validateInputs(String context, String path, Class<?> type) {
        if (StringUtils.isBlank(context)) {
            throw new IllegalArgumentException("Context cannot be empty");
        }
        if (StringUtils.isBlank(path)) {
            throw new IllegalArgumentException("Path cannot be empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
    }

    private <T> T convertToType(Map<String, Object> data, Class<T> type) {
        if (data == null) {
            return null;
        }

        try {
            if (type == String.class) {
                return type.cast(data.values().iterator().next());
            }

            if (type == Map.class) {
                Map<String, Object> map = new HashMap<>();
                for (Map.Entry<String, Object> value : data.entrySet()) {
                    map.put(value.getKey(), value.getValue());
                }
                return type.cast(map);
            }
            return objectMapper.convertValue(data, type);

        } catch (IllegalArgumentException e) {
            throw new VaultException(" convert vault data");
        }

    }


    private static final String DEFAULT_CONTEXT = "app";

    public Map<String, Object> writeSecret(String path, Map<String, Object> payload) {
        return writeSecret(DEFAULT_CONTEXT, path, payload);
    }

    /**
     * vault 에서 특정 경로 추가
     */
    public Map<String, Object> writeSecret(String context, String path, Map<String, Object> payload) {
        VaultKeyValueOperations ops = operationsMap.get(context);
        if (ops == null) {
            log.error("Vault context not found: {}", context);
            throw new VaultException("context not found");
        }

        ops.put(path, payload);

        VaultResponse response = ops.get(path);

        if (response == null || response.getData() == null) {
            throw new VaultException("not allow save secret for path: " + path);
        }

        return response.getData();
    }

}
