package ch.admin.bit.jeap.config.aws.secretsmanager;

import io.floci.testcontainers.FlociContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Testcontainers
class SecretsManagerIT {

    @Container
    protected static final FlociContainer FLOCI = createFlociContainer();

    private static FlociContainer createFlociContainer() {
        return new FlociContainer(DockerImageName.parse("floci/floci:1.5.31")
                .asCompatibleSubstituteFor("floci/floci"));
    }

    @BeforeAll
    static void beforeAll() {
        createSecret("secret1");
        createSecret("secret2");
    }

    private static void createSecret(String secretName) {
        try (SecretsManagerClient client = SecretsManagerClient.builder()
                .endpointOverride(URI.create(FLOCI.getEndpoint()))
                .region(Region.of(FLOCI.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(FLOCI.getAccessKey(), FLOCI.getSecretKey())))
                .build()) {
            client.createSecret(CreateSecretRequest.builder()
                    .name(secretName)
                    .secretString(secretName + "Value")
                    .build());
            log.info("Created secret: {}", secretName);
        }
    }

    @Test
    void testSecretManagerIntegration_singleSecret() {
        SpringApplication application = new SpringApplication(Application.class);
        application.setWebApplicationType(WebApplicationType.NONE);

        try (ConfigurableApplicationContext context = runApplication(application,
                "jeap-aws-secretsmanager:secret1")) {
            assertThat(context.getEnvironment().getProperty("secret1"))
                    .isEqualTo("secret1Value");
        }
    }

    @Test
    void testSecretManagerIntegration_twoSecrets() {
        SpringApplication application = new SpringApplication(Application.class);
        application.setWebApplicationType(WebApplicationType.NONE);

        try (ConfigurableApplicationContext context = runApplication(application,
                "jeap-aws-secretsmanager:secret1;secret2")) {
            assertThat(context.getEnvironment().getProperty("secret1"))
                    .isEqualTo("secret1Value");
            assertThat(context.getEnvironment().getProperty("secret2"))
                    .isEqualTo("secret2Value");
        }
    }

    @Test
    void testSecretManagerIntegration_disabled() {
        SpringApplication application = new SpringApplication(Application.class);
        application.setWebApplicationType(WebApplicationType.NONE);

        try (ConfigurableApplicationContext context = runApplicationWithSecretsManagerDisabled(application,
                "jeap-aws-secretsmanager:secret1;secret2")) {
            assertThat(context.getEnvironment().getProperty("secret1"))
                    .isNull();
            assertThat(context.getEnvironment().getProperty("secret2"))
                    .isNull();
        }
    }

    @Test
    void testSecretManagerIntegration_noImportedSecrets() {
        SpringApplication application = new SpringApplication(Application.class);
        application.setWebApplicationType(WebApplicationType.NONE);

        try (ConfigurableApplicationContext context = application.run()) {
            assertThat(context.getEnvironment().getProperty("secret1"))
                    .isNull();
        }
    }

    private ConfigurableApplicationContext runApplication(SpringApplication application, String springConfigImport) {
        return application.run("--spring.config.import=" + springConfigImport,
                "--jeap.aws.secretsmanager.region=" + FLOCI.getRegion(),
                "--jeap.aws.secretsmanager.access-key-id=" + FLOCI.getAccessKey(),
                "--jeap.aws.secretsmanager.secret-access-key=" + FLOCI.getSecretKey(),
                "--jeap.aws.secretsmanager.endpoint-override=" + FLOCI.getEndpoint(),
                "--jeap.aws.secretsmanager.httpProxyUseExternallyDefinedSettings=false");
    }

    private ConfigurableApplicationContext runApplicationWithSecretsManagerDisabled(SpringApplication application, String springConfigImport) {
        return application.run("--spring.config.import=" + springConfigImport,
                "--jeap.aws.secretsmanager.enabled=false");
    }
}
