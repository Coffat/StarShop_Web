#!/bin/bash

# Script to update ECS Task Definition with new environment variables
# Usage: ./update-env-vars.sh

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🔧 Updating ECS Task Definition Environment Variables${NC}"
echo ""

# Get current task definition
echo -e "${YELLOW}📥 Fetching current task definition...${NC}"
TASK_DEF=$(aws ecs describe-task-definition \
  --task-definition flower-shop-task \
  --region ap-southeast-2 \
  --query 'taskDefinition' \
  --output json)

# Extract necessary fields
FAMILY=$(echo $TASK_DEF | jq -r '.family')
TASK_ROLE_ARN=$(echo $TASK_DEF | jq -r '.taskRoleArn')
EXECUTION_ROLE_ARN=$(echo $TASK_DEF | jq -r '.executionRoleArn')
NETWORK_MODE=$(echo $TASK_DEF | jq -r '.networkMode')
CPU=$(echo $TASK_DEF | jq -r '.cpu')
MEMORY=$(echo $TASK_DEF | jq -r '.memory')
REQUIRES_COMPATIBILITIES=$(echo $TASK_DEF | jq -r '.requiresCompatibilities')
RUNTIME_PLATFORM=$(echo $TASK_DEF | jq -r '.runtimePlatform')

echo -e "${GREEN}✅ Current task definition: $FAMILY${NC}"
echo ""

# Create new task definition with updated environment variables
echo -e "${YELLOW}📝 Creating new task definition with updated URLs...${NC}"

# Get current container definition
CONTAINER_DEF=$(echo $TASK_DEF | jq '.containerDefinitions[0]')

# Update environment variables
UPDATED_CONTAINER=$(echo $CONTAINER_DEF | jq '
  .environment |= map(
    if .name == "APP_BASE_URL" then .value = "http://starshop-hcmute.duckdns.org"
    elif .name == "MOMO_RETURN_URL" then .value = "http://starshop-hcmute.duckdns.org/payment/momo/return"
    elif .name == "MOMO_NOTIFY_URL" then .value = "http://starshop-hcmute.duckdns.org/payment/momo/notify"
    elif .name == "SWAGGER_SERVER_PROD_URL" then .value = "http://starshop-hcmute.duckdns.org"
    elif .name == "CORS_ALLOWED_ORIGINS" then .value = "http://starshop-hcmute.duckdns.org"
    else .
    end
  )
')

# Create new task definition JSON (exclude taskRoleArn if null)
if [ "$TASK_ROLE_ARN" = "null" ]; then
    NEW_TASK_DEF=$(jq -n \
      --arg family "$FAMILY" \
      --arg executionRoleArn "$EXECUTION_ROLE_ARN" \
      --arg networkMode "$NETWORK_MODE" \
      --arg cpu "$CPU" \
      --arg memory "$MEMORY" \
      --argjson requiresCompatibilities "$REQUIRES_COMPATIBILITIES" \
      --argjson runtimePlatform "$RUNTIME_PLATFORM" \
      --argjson containerDefinitions "[$UPDATED_CONTAINER]" \
      '{
        family: $family,
        executionRoleArn: $executionRoleArn,
        networkMode: $networkMode,
        cpu: $cpu,
        memory: $memory,
        requiresCompatibilities: $requiresCompatibilities,
        runtimePlatform: $runtimePlatform,
        containerDefinitions: $containerDefinitions
      }')
else
    NEW_TASK_DEF=$(jq -n \
      --arg family "$FAMILY" \
      --arg taskRoleArn "$TASK_ROLE_ARN" \
      --arg executionRoleArn "$EXECUTION_ROLE_ARN" \
      --arg networkMode "$NETWORK_MODE" \
      --arg cpu "$CPU" \
      --arg memory "$MEMORY" \
      --argjson requiresCompatibilities "$REQUIRES_COMPATIBILITIES" \
      --argjson runtimePlatform "$RUNTIME_PLATFORM" \
      --argjson containerDefinitions "[$UPDATED_CONTAINER]" \
      '{
        family: $family,
        taskRoleArn: $taskRoleArn,
        executionRoleArn: $executionRoleArn,
        networkMode: $networkMode,
        cpu: $cpu,
        memory: $memory,
        requiresCompatibilities: $requiresCompatibilities,
        runtimePlatform: $runtimePlatform,
        containerDefinitions: $containerDefinitions
      }')
fi

# Register new task definition
echo -e "${YELLOW}📤 Registering new task definition...${NC}"
NEW_REVISION=$(aws ecs register-task-definition \
  --cli-input-json "$NEW_TASK_DEF" \
  --region ap-southeast-2 \
  --query 'taskDefinition.revision' \
  --output text)

echo -e "${GREEN}✅ New task definition registered: $FAMILY:$NEW_REVISION${NC}"
echo ""

# Update service
echo -e "${YELLOW}🔄 Updating ECS service...${NC}"
aws ecs update-service \
  --cluster flower-shop-cluster \
  --service flower-shop-service \
  --task-definition $FAMILY:$NEW_REVISION \
  --force-new-deployment \
  --region ap-southeast-2 \
  --query 'service.serviceName' \
  --output text > /dev/null

echo -e "${GREEN}✅ Service updated successfully!${NC}"
echo ""

echo -e "${BLUE}📊 Summary:${NC}"
echo "  • Task Definition: $FAMILY:$NEW_REVISION"
echo "  • Updated URLs to use domain: starshop-hcmute.duckdns.org"
echo "  • Service is deploying..."
echo ""
echo -e "${YELLOW}⏳ Deployment will take 2-3 minutes${NC}"
echo ""
echo "Monitor deployment:"
echo "  aws ecs describe-services --cluster flower-shop-cluster --services flower-shop-service --region ap-southeast-2"
echo ""
echo "View logs:"
echo "  aws logs tail /ecs/flower-shop-task --follow --region ap-southeast-2"
