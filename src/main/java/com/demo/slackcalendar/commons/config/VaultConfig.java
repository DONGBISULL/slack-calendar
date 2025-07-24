package com.demo.slackcalendar.commons.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.client.VaultClients;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.web.client.RestOperations;

import java.net.URI;

@Configuration
@Slf4j
public class VaultConfig {

    @Value("${spring.cloud.vault.app-role.role-id}")
    private String roleId;

    @Value("${spring.cloud.vault.app-role.secret-id}")
    private String secretId;

    @Value("${spring.cloud.vault.uri}")
    private String vaultUri;

    @Bean
    public VaultTemplate vaultTemplate(VaultEndpoint vaultEndpoint, ClientAuthentication clientAuthentication) {
        return new VaultTemplate(vaultEndpoint, clientAuthentication);
    }

    @Bean
    public VaultEndpoint vaultEndpoint() {
        log.info("Creating VaultEndpoint with URI: '{}'", vaultUri);
        log.info("URI length: {}", vaultUri.length());
        log.info("URI after trim: '{}'", vaultUri.trim());

        URI uri = URI.create(vaultUri.trim());
        log.info("Created URI: {}", uri);

        return VaultEndpoint.from(uri);
    }

    @Bean
    public ClientAuthentication clientAuthentication(VaultEndpoint vaultEndpoint) {
        /* AppRole 방식으로 통신하기 위해 설정 */
        AppRoleAuthenticationOptions options = AppRoleAuthenticationOptions
                .builder()
                .roleId(AppRoleAuthenticationOptions.RoleId.provided(roleId))
                .secretId(AppRoleAuthenticationOptions.SecretId.provided(secretId))
                .build();
        return new AppRoleAuthentication(options, restOperations(vaultEndpoint));
    }

    @Bean
    public RestOperations restOperations(VaultEndpoint vaultEndpoint) {
        ClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        ((HttpComponentsClientHttpRequestFactory) factory).setConnectTimeout(5000);
        ((HttpComponentsClientHttpRequestFactory) factory).setReadTimeout(5000);

        return VaultClients.createRestTemplate(vaultEndpoint, factory);
    }
}
