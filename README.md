# order-payment

Spring Boot order and payment service built with Gradle and Java 21.

## Local build

```bash
./gradlew clean build
docker build -t order-payment:local .
```

On Windows PowerShell, use `./gradlew.bat clean build`.

## CI

`.github/workflows/ci.yml` builds and tests pushes and pull requests to `main`. For a push to `main`, it also publishes `ghcr.io/OWNER/REPOSITORY:<commit-sha>` for the CD workflows. Pull requests do not publish an image.

## CD

`.github/workflows/cd.yml` deploys `order-payment` to `/home/ubuntu/app` through AWS Systems Manager Run Command. It deploys the GHCR image tagged with the commit SHA by default, or accepts an existing image tag when started manually. Run CI successfully first so the image exists in GHCR.

Create a GitHub environment named `production` and configure:

| Type | Name | Value |
| --- | --- | --- |
| Variable | `AWS_REGION` | AWS region containing the deployment instances |
| Variable | `AWS_DEPLOY_ROLE_ARN` | IAM role ARN that GitHub Actions assumes through OIDC |
| Variable | `ORDER_SERVICE_INSTANCE_IDS` | Space-separated EC2 instance IDs managed by SSM, for example `i-0123456789abcdef0 i-0abcdef0123456789` |
| Secret | `ORDER_PAYMENT_MYSQL_URL` | JDBC URL for the order-payment database |
| Secret | `ORDER_PAYMENT_MYSQL_USERNAME` | Database username |
| Secret | `ORDER_PAYMENT_MYSQL_PASSWORD` | Database password |
| Secret | `KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap server list |
| Secret | `TOSS_PAYMENT_SECRET` | Toss Payments secret key |

Before the first deployment, configure GitHub's AWS OIDC provider and the `AWS_DEPLOY_ROLE_ARN` trust policy to allow this repository's `production` environment to assume the role. Grant that role `ssm:SendCommand` for the intended instances and `AWS-RunShellScript`, `ssm:GetCommandInvocation`, and `ssm:PutParameter` / `ssm:DeleteParameter` for `/order-payment/deploy/*`. The GitHub job stores the generated `.env` as a per-run SSM `SecureString`, deploys, then deletes it. Each instance needs an instance profile with Systems Manager managed-instance permissions (for example, `AmazonSSMManagedInstanceCore`), `ssm:GetParameter` access to that parameter path (and `kms:Decrypt` if using a customer-managed KMS key), a running SSM Agent, AWS CLI, Docker Compose v2, and network access to SSM and GHCR through NAT or suitable VPC endpoints. Root's Docker credentials must be configured to pull the GHCR package, using a credential with `read:packages`, because Compose runs through `sudo`.

Keep `/home/ubuntu/app/docker-compose.yml` on every instance. It must define an `order-payment` service whose image uses `${IMAGE}`, and load `/home/ubuntu/app/.env` (for example with `env_file: .env`). The workflow writes that `.env` from the GitHub Environment secrets before Compose recreates the service. The Compose file should define any ports, mounts, health check, and resource limits needed by the service. The deployment runs `docker compose pull order-payment` followed by `docker compose up -d --no-deps --wait order-payment` from that directory.

The deployment scripts are kept under `.github/scripts`: `deploy-all.sh` sends the deployment to each configured instance with SSM, while `deploy-instance.sh` updates the service on the instance.
