package ch.admin.bit.jeap.config.aws.secretsmanager.config;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AwsSecretsManagerPropertySourceTest {

    private final SecretsManagerClient client = mock(SecretsManagerClient.class);

    @Test
    void loadsJsonSecretAsProperties() {
        when(client.getSecretValue(any(GetSecretValueRequest.class))).thenReturn(GetSecretValueResponse.builder()
                .name("path/secret")
                .secretString("{\"username\":\"user\",\"enabled\":true}")
                .build());
        AwsSecretsManagerPropertySource propertySource = createPropertySource("path/secret?prefix=db.");

        propertySource.initPropertiesFromAwsSecretsManager();

        assertThat(propertySource.getPropertyNames()).containsExactly("db.username", "db.enabled");
        assertThat(propertySource.getProperty("db.username")).isEqualTo("user");
        assertThat(propertySource.getProperty("db.enabled")).isEqualTo(true);
    }

    @Test
    void loadsPlainTextSecretUnderSecretName() {
        when(client.getSecretValue(any(GetSecretValueRequest.class))).thenReturn(GetSecretValueResponse.builder()
                .name("path/secret")
                .secretString("plain-text-value")
                .build());
        AwsSecretsManagerPropertySource propertySource = createPropertySource("path/secret");

        propertySource.initPropertiesFromAwsSecretsManager();

        assertThat(propertySource.getPropertyNames()).containsExactly("secret");
        assertThat(propertySource.getProperty("secret")).isEqualTo("plain-text-value");
    }

    private AwsSecretsManagerPropertySource createPropertySource(String secretName) {
        AwsSecretsManagerConfigDataResource resource = new AwsSecretsManagerConfigDataResource(
                secretName, false, true, null);
        return new AwsSecretsManagerPropertySource(resource, client);
    }
}
