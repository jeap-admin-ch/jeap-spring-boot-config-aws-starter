# AWS Secrets Manager integration

AWS Secrets Manager stores sensitive credentials. The starter imports a secret's contents into the
Spring `Environment` so they bind like any other property. It uses the `jeap-aws-secretsmanager:`
location prefix and is implemented as Spring Boot `ConfigData` support
(`AwsSecretsManagerConfigDataLocationResolver` and `AwsSecretsManagerConfigDataLoader`), registered in
`META-INF/spring.factories`.

> The classes are adapted from Spring Cloud AWS and run at `HIGHEST_PRECEDENCE + 5` so they take
> precedence over Spring Cloud AWS's own resolver/loader. jEAP deliberately uses the
> `jeap-aws-secretsmanager:` prefix (not `aws-secretsmanager:`) to avoid clashing with Spring Cloud
> AWS, which owns `aws-secretsmanager:`.

## Importing secrets

List one import per secret. Several secrets in one location can be separated with `;`:

```yaml
spring:
  config:
    import:
      - "jeap-aws-secretsmanager:jme-aws-config-example"
      - "jeap-aws-secretsmanager:shared/kafka"
```

A non-optional location that resolves to no secret fails startup; prefix it with `optional:` to
tolerate a missing secret.

## How a secret becomes properties

The loader calls `GetSecretValue` for each secret and turns the result into an
`AwsSecretsManagerPropertySource`:

- **JSON secret string** — each top-level key/value pair becomes one property.
- **Plain-text secret string** — the secret's name becomes a single property whose value is the text.
- **Binary secret** — the secret's name becomes a single property whose value is the raw bytes.

## Naming and prefixes

Secret naming follows the platform convention: `<microservice-name>` for service-specific secrets and
`shared/<context>` (e.g. `shared/kafka`) for shared secrets. A microservice has read access to its
own and to `shared/*` secrets.

If the secret's keys already carry a full context (e.g. `foo.bar.key`), import them as-is. If the
keys are short (e.g. `key`), add a `?prefix=` so the keys land in their own namespace and cannot
collide. When two imported secrets define the same property name, the last one wins.

```yaml
spring:
  config:
    import:
      - "jeap-aws-secretsmanager:jme-aws-config-example?prefix=aws.secrets."
      - "jeap-aws-secretsmanager:shared/kafka?prefix=aws.secrets-shared.kafka."
```

With the prefix `aws.secrets.`, a secret key `credentialOne` is exposed as the property
`aws.secrets.credentialOne` and binds to:

```java
@ConfigurationProperties(prefix = "aws.secrets")
@Data
class SecretConfigProperties {
    private String credentialOne;
    private String credentialTwo;
}
```

## Client configuration and disabling

The `SecretsManagerClient` is built with the JDK URL-connection HTTP client. Region, endpoint and
credentials come from `jeap.aws.secretsmanager.*`; by default the
`DefaultCredentialsProvider` chain and the AWS region resolution are used. Static
`access-key-id`/`secret-access-key` and an `endpoint-override` exist mainly for tests against
LocalStack. Set `jeap.aws.secretsmanager.enabled=false` to skip Secrets Manager loading; the legacy
`spring.cloud.aws.secretsmanager.enabled=false` is also honoured for backwards compatibility. See the
[Configuration reference](configuration.md) for the full list.

## Related

- [Configuration reference](configuration.md)
- [Getting started](getting-started.md)
- [jeap-spring-boot-config-aws-starter](../README.md)
