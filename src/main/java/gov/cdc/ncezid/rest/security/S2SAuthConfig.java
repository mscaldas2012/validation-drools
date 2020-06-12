package gov.cdc.ncezid.rest.security;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Value;
import lombok.Data;

@ConfigurationProperties("s2s-auth-config")
@Data
public class S2SAuthConfig {
    @Value("${token}")
    private String token;
}