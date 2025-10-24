#!/bin/bash

# ========================================
# Script: Update ECS Task Definition
# ========================================
# Cập nhật task definition với AWS Account ID và image tag mới
# Usage: ./update-task-definition.sh [IMAGE_TAG]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}ECS Task Definition Updater${NC}"
echo -e "${GREEN}========================================${NC}"

# Get AWS Account ID
echo -e "\n${YELLOW}Getting AWS Account ID...${NC}"
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
if [ -z "$ACCOUNT_ID" ]; then
    echo -e "${RED}Error: Could not get AWS Account ID${NC}"
    echo -e "${RED}Make sure AWS CLI is configured properly${NC}"
    exit 1
fi
echo -e "${GREEN}✓ AWS Account ID: ${ACCOUNT_ID}${NC}"

# Get image tag (default to latest)
IMAGE_TAG=${1:-latest}
echo -e "${GREEN}✓ Image Tag: ${IMAGE_TAG}${NC}"

# AWS Region
REGION="ap-southeast-2"
echo -e "${GREEN}✓ Region: ${REGION}${NC}"

# Read task definition template
echo -e "\n${YELLOW}Reading task-definition.json...${NC}"
if [ ! -f "task-definition.json" ]; then
    echo -e "${RED}Error: task-definition.json not found${NC}"
    exit 1
fi

# Create updated task definition
echo -e "${YELLOW}Creating updated task definition...${NC}"
cat task-definition.json | \
    sed "s|YOUR_AWS_ACCOUNT_ID|${ACCOUNT_ID}|g" | \
    sed "s|:latest|:${IMAGE_TAG}|g" > task-definition-updated.json

echo -e "${GREEN}✓ Task definition updated${NC}"

# Show diff (optional)
echo -e "\n${YELLOW}Changes made:${NC}"
echo -e "  - AWS Account ID: YOUR_AWS_ACCOUNT_ID → ${ACCOUNT_ID}"
echo -e "  - Image Tag: :latest → :${IMAGE_TAG}"

# Register task definition
echo -e "\n${YELLOW}Registering new task definition...${NC}"
TASK_DEF_ARN=$(aws ecs register-task-definition \
    --cli-input-json file://task-definition-updated.json \
    --region ${REGION} \
    --query 'taskDefinition.taskDefinitionArn' \
    --output text)

if [ -z "$TASK_DEF_ARN" ]; then
    echo -e "${RED}Error: Failed to register task definition${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Task definition registered: ${TASK_DEF_ARN}${NC}"

# Update ECS service
echo -e "\n${YELLOW}Updating ECS service...${NC}"
aws ecs update-service \
    --cluster flower-shop-cluster \
    --service flower-shop-service \
    --task-definition ${TASK_DEF_ARN} \
    --force-new-deployment \
    --region ${REGION} > /dev/null

echo -e "${GREEN}✓ Service update initiated${NC}"

# Wait for service to stabilize (optional)
read -p "$(echo -e ${YELLOW}Wait for service to stabilize? [y/N]: ${NC})" -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}Waiting for service to stabilize...${NC}"
    echo -e "${YELLOW}This may take a few minutes...${NC}"
    aws ecs wait services-stable \
        --cluster flower-shop-cluster \
        --services flower-shop-service \
        --region ${REGION}
    echo -e "${GREEN}✓ Service is now stable!${NC}"
fi

# Get task public IP
echo -e "\n${YELLOW}Getting task public IP...${NC}"
TASK_ARN=$(aws ecs list-tasks \
    --cluster flower-shop-cluster \
    --service-name flower-shop-service \
    --region ${REGION} \
    --query 'taskArns[0]' \
    --output text)

if [ "$TASK_ARN" != "None" ] && [ ! -z "$TASK_ARN" ]; then
    ENI_ID=$(aws ecs describe-tasks \
        --cluster flower-shop-cluster \
        --tasks ${TASK_ARN} \
        --region ${REGION} \
        --query 'tasks[0].attachments[0].details[?name==`networkInterfaceId`].value' \
        --output text)
    
    if [ ! -z "$ENI_ID" ]; then
        PUBLIC_IP=$(aws ec2 describe-network-interfaces \
            --network-interface-ids ${ENI_ID} \
            --region ${REGION} \
            --query 'NetworkInterfaces[0].Association.PublicIp' \
            --output text)
        
        if [ ! -z "$PUBLIC_IP" ] && [ "$PUBLIC_IP" != "None" ]; then
            echo -e "${GREEN}✓ Public IP: ${PUBLIC_IP}${NC}"
        fi
    fi
fi

# Cleanup
rm -f task-definition-updated.json

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}✓ Deployment Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "\n${YELLOW}Application URL:${NC} http://starshop-hcmute.duckdns.org"
echo -e "${YELLOW}Health Check:${NC} curl http://starshop-hcmute.duckdns.org/actuator/health"
echo -e "\n${YELLOW}Monitor logs:${NC}"
echo -e "  aws logs tail /ecs/flower-shop-task --follow --region ${REGION}"
echo -e "\n${YELLOW}Check service status:${NC}"
echo -e "  aws ecs describe-services --cluster flower-shop-cluster --services flower-shop-service --region ${REGION}"
echo ""
