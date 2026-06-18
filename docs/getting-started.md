# Getting started

This page shows how to add the jEAP Spring Boot Config AWS Starter to a service and import
configuration from AWS AppConfig and secrets from AWS Secrets Manager. Both work through Spring
Boot's `spring.config.import`, so the imported values become ordinary entries in the Spring
`Environment`. For the details of each integration see [AWS AppConfig](appconfig.md) and
[AWS Secrets Manager](secrets-management.md).

## 1. Add the dependency

```xml
<dependency>
    <groupId>ch.admin.bit.jeap</groupId>
    <artifactId>jeap-spring-boot-config-aws-starter</artifactId>
</dependency>
```

The version is managed by the jEAP Spring Boot parent. The starter auto-configures itself; there is
nothing else to register.

## 2. Import AppConfig configuration

Add a `spring.config.import` with the `jeap-app-config-aws:` location and tell the starter which
AppConfig environment to read from. With just the bare prefix, the default profile layout is used
(see [AWS AppConfig](appconfig.md)).

```yaml
spring:
  application:
    name: jme-aws-config-example
  config:
    import: "jeap-app-config-aws:"

jeap:
  config:
    aws:
      appconfig:
        env-id: dev
```

The AppConfig application named after `spring.application.name` is required; its `config` profile and
the shared `common` / `common-platform` / `common-certs` profiles are fetched automatically.

## 3. Import secrets from Secrets Manager

Add one `jeap-aws-secretsmanager:` import per secret. The secret's JSON keys become properties; an
optional `?prefix=` keeps them in their own namespace.

```yaml
spring:
  config:
    import:
      - "jeap-aws-secretsmanager:jme-aws-config-example?prefix=aws.secrets."
      - "jeap-aws-secretsmanager:shared/kafka?prefix=aws.secrets-shared.kafka."
```

## 4. Use the values

Imported properties bind like any other Spring property — through `@ConfigurationProperties` or
`@Value`:

```java
@ConfigurationProperties(prefix = "aws.secrets")
@Data
class SecretConfigProperties {
    private String credentialOne;
    private String credentialTwo;
}
```

## Local development

For local runs, simply omit the `jeap-app-config-aws:` / `jeap-aws-secretsmanager:` imports and
supply the same properties from a local file or environment variables. With the imports removed the
starter does not activate and no AWS call is made; the `jeap.config.aws.appconfig.*` properties can
be dropped as well.

## Related

- [Configuration reference](configuration.md)
- [AWS AppConfig](appconfig.md)
- [AWS Secrets Manager](secrets-management.md)
- [jeap-spring-boot-config-aws-starter](../README.md)
