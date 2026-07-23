# Configuration reference

The starter has two independent property groups: `jeap.config.aws.appconfig.*` for AWS AppConfig and
`jeap.aws.secretsmanager.*` for AWS Secrets Manager. Both integrations are activated by adding their
location to `spring.config.import` (`jeap-app-config-aws:` and `jeap-aws-secretsmanager:`
respectively) — without the import, the corresponding integration stays inactive.

## AWS AppConfig (`jeap.config.aws.appconfig.*`)

| Name                                       | Default | Description                                                                                                                  |
|--------------------------------------------|---------|----------------------------------------------------------------------------------------------------------------------------|
| `env-id`                                   | —       | Id or name of the AppConfig environment to fetch deployed configuration from (e.g. `dev`). Required when importing AppConfig |
| `required-minimum-poll-interval-in-seconds`| `60`    | Minimum and standard interval (seconds) between polls. AWS valid range 15–86400                                            |
| `no-default-common-config`                 | `false` | Do not load the default `common` application profile                                                                        |
| `no-default-common-platform-config`        | `false` | Do not load the default `common-platform` application profile                                                               |
| `trust-all-certificates`                   | `false` | Trust all TLS certificates when connecting to AppConfig. **Development only**                                               |

`spring.application.name` is read separately and used as the AppConfig application name for the
service's own profile when the default layout is used. See [AWS AppConfig](appconfig.md).

## AWS Secrets Manager (`jeap.aws.secretsmanager.*`)

| Name                                      | Default | Description                                                                                   |
|-------------------------------------------|---------|-----------------------------------------------------------------------------------------------|
| `enabled`                                 | `true`  | Load secrets. Set to `false` to skip Secrets Manager loading entirely                         |
| `region`                                  | —       | AWS region override. If unset, the default AWS region resolution applies                      |
| `endpoint-override`                       | —       | Override the Secrets Manager endpoint URI (e.g. a Floci endpoint in tests)                    |
| `access-key-id`                           | —       | Static access key id. When set together with `secret-access-key`, static credentials are used |
| `secret-access-key`                       | —       | Static secret access key (see `access-key-id`)                                                |
| `http-proxy-use-externally-defined-settings` | `true` | Let the HTTP client use system-property and environment-variable proxy settings               |

If `access-key-id` and `secret-access-key` are not both set, the AWS
`DefaultCredentialsProvider` chain is used. For backwards compatibility the legacy property
`spring.cloud.aws.secretsmanager.enabled` (default `true`) is also honoured — Secrets Manager loads
only when both it and `jeap.aws.secretsmanager.enabled` are `true`. See
[AWS Secrets Manager](secrets-management.md).

## Location syntax

| Location                                                  | Meaning                                                                 |
|----------------------------------------------------------|-------------------------------------------------------------------------|
| `jeap-app-config-aws:`                                   | AppConfig with the default profile layout                               |
| `jeap-app-config-aws:<app>/config;<app2>/config`         | AppConfig with explicit `<app-name>/<profile-name>` profiles            |
| `jeap-aws-secretsmanager:<secret>`                       | Import one secret                                                        |
| `jeap-aws-secretsmanager:<secret>?prefix=<prefix>`       | Import a secret, prefixing every resolved key with `<prefix>`           |
| `jeap-aws-secretsmanager:<secret1>;<secret2>`            | Import several secrets from one location                                 |
| `optional:<location>`                                    | Tolerate a missing/unreachable location instead of failing startup      |

## Related

- [Getting started](getting-started.md)
- [AWS AppConfig](appconfig.md)
- [AWS Secrets Manager](secrets-management.md)
- [Dynamic configuration refresh](dynamic-refresh.md)
- [jeap-spring-boot-config-aws-starter](../README.md)
