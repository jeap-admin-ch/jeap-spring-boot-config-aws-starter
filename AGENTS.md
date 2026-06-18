# AGENTS.md

Guidance for AI coding agents working **in this repository**. For how to *use* the library in a
consuming service, read [README.md](README.md) and the [docs/](docs/) folder instead.

## Project

jEAP Spring Boot Config AWS Starter is a Maven library that integrates AWS AppConfig and AWS Secrets
Manager into Spring Boot's configuration loading. It is implemented as Spring Boot `ConfigData`
support: `ConfigDataLocationResolver` and `ConfigDataLoader` implementations turn
`spring.config.import` locations (`jeap-app-config-aws:` and `jeap-aws-secretsmanager:`) into
property sources during application startup, before the application context is created. An
auto-configuration adds runtime polling and context refresh for AppConfig.

## Repository layout

```
pom.xml                                                  # Parent POM (packaging=pom); declares the module below
jeap-spring-boot-config-aws-starter/                     # The single starter module
  src/main/java/ch/admin/bit/jeap/config/aws/
    appconfig/                                           # AWS AppConfig integration
      JeapAWSAppConfigProperties.java                    # @ConfigurationProperties("jeap.config.aws.appconfig")
      JeapSpringApplicationProperties.java               # binds spring.application.name
      JeapAWSAppConfigAutoConfig.java                    # @AutoConfiguration; registers AppConfigContextRefresher
      config/                                            # ConfigData resolver/loader + property sources
      client/                                            # JeapAppConfigDataClient (polling) + factory + change listener
      refresh/AppConfigContextRefresher.java             # triggers ContextRefresher on config change
    secretsmanager/                                      # AWS Secrets Manager integration
      JeapAwsSecretsManagerProperties.java               # @ConfigurationProperties("jeap.aws.secretsmanager")
      JeapAwsSecretsManagerAutoConfig.java               # @AutoConfiguration (conditional)
      config/                                            # ConfigData resolver/loader + property sources
    context/                                             # ConfigContexts, BootstrapLoggingHelper (bootstrap helpers)
  src/main/resources/META-INF/
    spring.factories                                     # registers ConfigDataLocationResolver + ConfigDataLoader
    spring/...AutoConfiguration.imports                  # registers the two @AutoConfiguration classes
Jenkinsfile, publiccode.yml, CHANGELOG.md, LICENSE
```

The two integrations are independent: AppConfig is resolved by `AppConfigDataLocationResolver` /
`AppConfigDataLoader`, Secrets Manager by `AwsSecretsManagerConfigDataLocationResolver` /
`AwsSecretsManagerConfigDataLoader`. The Secrets Manager classes are based on Spring Cloud AWS code
and run at `HIGHEST_PRECEDENCE + 5` so they override Spring Cloud AWS's own `aws-secretsmanager:`
handling; jEAP intentionally uses the distinct `jeap-aws-secretsmanager:` prefix.

## Build & test

```bash
./mvnw verify                                # full build incl. tests
./mvnw -pl jeap-spring-boot-config-aws-starter test
```

- Parent: `ch.admin.bit.jeap:jeap-internal-spring-boot-parent` (Spring Boot 4 aligned).
- Integration tests run a real `SpringApplication`. `AppConfigDataLoaderIT` mocks the AWS
  `AppConfigDataClient` via a `BootstrapRegistryInitializer`; `SecretsManagerIT` starts a LocalStack
  Testcontainer. Loading happens at bootstrap time, so loggers are reconfigured through
  `BootstrapLoggingHelper`.
- Spring Boot 3 maintenance happens on the `release/springboot3` branch; `master` targets Spring Boot 4.

## jEAP conventions

- Java packages live under `ch.admin.bit.jeap.config.aws.*`.
- Configuration properties use the prefixes `jeap.config.aws.appconfig.*` (AppConfig) and
  `jeap.aws.secretsmanager.*` (Secrets Manager).
- `spring.config.import` location prefixes are `jeap-app-config-aws:` and `jeap-aws-secretsmanager:`.
- AWS clients use the `url-connection-client`; the `appconfig`, `appconfigdata` and `secretsmanager`
  SDK modules exclude the Apache and Netty HTTP clients in the POM. Keep that exclusion intact.
- Auto-configuration is registered via `@AutoConfiguration` and
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`; the
  `ConfigData` resolvers/loaders are registered in `META-INF/spring.factories`.

## Docs

When changing public behaviour, update the matching focused file under [docs/](docs/) (one topic per
file) and the documentation index in the README.

## Versioning

- Semantic Versioning; all changes documented in [CHANGELOG.md](./CHANGELOG.md) (Keep a Changelog format).
- `setPomVersions.sh` updates the version across all module POMs.
- When working on a feature branch, increase the version to `x.y.z-SNAPSHOT` in the POMs.
- Always keep the -SNAPSHOT postfix in the POMs, CI will remove it when releasing a version. Do not use the SNAPSHOT
  postfix in other places (CHANGELOG, publiccode.yml etc).
- Keep changelog entries concise and to the point, follow existing patterns.
- Keep commit messages short, use the JIRA ID from the branch name as a prefix, do not use conventional commits (for
  example: "JEAP-1234 Added feature X").
- When bumping the version, also update the changelog, and update version/date in `publiccode.yml`.
