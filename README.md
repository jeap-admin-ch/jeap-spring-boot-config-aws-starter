# jEAP Spring Boot Config AWS Starter

jEAP Spring Boot Config AWS Starter is a Spring Boot auto-configuration library that feeds a jEAP
service's configuration from two AWS services: **AWS AppConfig** (externalized application
configuration) and **AWS Secrets Manager** (sensitive credentials). It plugs into Spring Boot's
`spring.config.import` mechanism, so AppConfig profiles and secrets become ordinary entries in the
Spring `Environment` and bind to `@ConfigurationProperties` like any other property source. It
provides:

* Importing AWS AppConfig configuration profiles as Spring property sources via `jeap-app-config-aws:`
* A sensible default profile layout (`common`, `common-platform`, `common-certs`, the service itself)
* Background polling of AppConfig with dynamic context refresh when a configuration changes at runtime
* Importing AWS Secrets Manager secrets as Spring property sources via `jeap-aws-secretsmanager:`
* Optional key prefixing for secrets to avoid property-name clashes
* AWS SDK clients backed by the JDK URL-connection HTTP client (no Apache/Netty client pulled in)

## Documentation

Start with [Getting started](docs/getting-started.md), then follow the links below.

| Topic                                                       | File                                                       |
|-------------------------------------------------------------|------------------------------------------------------------|
| Getting started (add the dependency, import config/secrets) | [docs/getting-started.md](docs/getting-started.md)         |
| Configuration reference (`jeap.config.aws.appconfig.*`, `jeap.aws.secretsmanager.*`) | [docs/configuration.md](docs/configuration.md) |
| AWS AppConfig integration                                   | [docs/appconfig.md](docs/appconfig.md)                     |
| Dynamic configuration refresh                               | [docs/dynamic-refresh.md](docs/dynamic-refresh.md)         |
| AWS Secrets Manager integration                             | [docs/secrets-management.md](docs/secrets-management.md)   |

## Modules

The artifact consumers depend on is `jeap-spring-boot-config-aws-starter`. Group id for all modules
is `ch.admin.bit.jeap`; the version is managed by the jEAP Spring Boot parent.

| Module                                   | Purpose                                                                                  |
|------------------------------------------|------------------------------------------------------------------------------------------|
| `jeap-spring-boot-config-aws-starter-parent` | Parent POM (packaging `pom`) declaring the starter module                            |
| `jeap-spring-boot-config-aws-starter`    | Auto-configuration and `ConfigData` integration for AWS AppConfig and Secrets Manager    |

## Changes

This library is versioned using [Semantic Versioning](http://semver.org/) and all changes are documented in
[CHANGELOG.md](./CHANGELOG.md) following the format defined in [Keep a Changelog](http://keepachangelog.com/).

## Note

This repository is part the open source distribution of jEAP. See [github.com/jeap-admin-ch/jeap](https://github.com/jeap-admin-ch/jeap)
for more information.

## License

This repository is Open Source Software licensed under the [Apache License 2.0](./LICENSE).
