#!/usr/bin/env bash
set -Eeuo pipefail

service="${1:?service name is required}"
compose_dir="${2:?compose directory is required}"
image="${3:?image is required}"
aws_region="${4:?AWS region is required}"
env_parameter="${5:?SecureString parameter name is required}"
compose_file="${compose_dir}/docker-compose.yml"

if ! command -v aws >/dev/null 2>&1; then
  echo "AWS CLI is required on the instance to retrieve deployment environment variables." >&2
  exit 127
fi

if [[ ! -f "$compose_file" ]]; then
  echo "Compose file not found: ${compose_file}" >&2
  exit 1
fi

env_file="$(sudo mktemp "${compose_dir}/.env.XXXXXX")"
trap 'sudo rm -f "$env_file"' EXIT

aws ssm get-parameter \
  --region "$aws_region" \
  --name "$env_parameter" \
  --with-decryption \
  --query 'Parameter.Value' \
  --output text |
  sudo sh -c 'umask 077; cat > "$1"' sh "$env_file"
sudo mv "$env_file" "${compose_dir}/.env"
trap - EXIT

cd "$compose_dir"
instance_name="$(hostname)"
private_ip="$(hostname -I | awk '{print $1}')"
sudo env IMAGE_NAME="$image" SERVICE_NAME="${SERVICE_NAME:-order-payment}" \
  INSTANCE_NAME="$instance_name" PRIVATE_IP="$private_ip" \
  docker compose -f "$compose_file" pull "$service"
sudo env IMAGE_NAME="$image" SERVICE_NAME="${SERVICE_NAME:-order-payment}" \
  INSTANCE_NAME="$instance_name" PRIVATE_IP="$private_ip" \
  docker compose -f "$compose_file" up \
  -d \
  --no-deps \
  --wait \
  --wait-timeout 120 \
  "$service"

if [[ "$service" == "app" ]]; then
  sudo env IMAGE_NAME="$image" SERVICE_NAME="${SERVICE_NAME:-order-payment}" \
    INSTANCE_NAME="$instance_name" PRIVATE_IP="$private_ip" \
    docker compose -f "$compose_file" up \
    -d \
    --wait \
    --wait-timeout 120 \
    alloy
fi

echo "${service} is running with ${image}."
