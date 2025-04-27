#!/bin/bash

# AWS Deployment Script for VibeAI News

# Configuration
APP_NAME="vibeai-news"
AWS_REGION="us-east-1"  # Change this to your preferred region
S3_BUCKET="vibeai-news-deploy"
ECR_REPOSITORY="vibeai-news"
ECS_CLUSTER="vibeai-news-cluster"
ECS_SERVICE="vibeai-news-service"
ECS_TASK_DEFINITION="vibeai-news-task"

# Build the application
echo "Building the application..."
#./gradlew shadowJar

# Create deployment directory
echo "Creating deployment directory..."
mkdir -p aws-deploy

# Copy the JAR and configuration files
echo "Copying files to deployment directory..."
cp build/libs/ai-vibe-news-all.jar aws-deploy/
cp src/main/resources/application.conf aws-deploy/

# Create Dockerfile
echo "Creating Dockerfile..."
cat > aws-deploy/Dockerfile << EOL
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY ai-vibe-news-all.jar /app/
COPY application.conf /app/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "ai-vibe-news-all.jar"]
EOL

# Create ECS Task Definition
echo "Creating ECS Task Definition..."
cat > aws-deploy/task-definition.json << EOL
{
    "family": "$ECS_TASK_DEFINITION",
    "networkMode": "awsvpc",
    "requiresCompatibilities": ["FARGATE"],
    "cpu": "256",
    "memory": "512",
    "executionRoleArn": "arn:aws:iam::\${AWS_ACCOUNT_ID}:role/ecsTaskExecutionRole",
    "taskRoleArn": "arn:aws:iam::\${AWS_ACCOUNT_ID}:role/ecsTaskRole",
    "containerDefinitions": [
        {
            "name": "$APP_NAME",
            "image": "\${AWS_ACCOUNT_ID}.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY:latest",
            "essential": true,
            "portMappings": [
                {
                    "containerPort": 8080,
                    "hostPort": 8080,
                    "protocol": "tcp"
                }
            ],
            "logConfiguration": {
                "logDriver": "awslogs",
                "options": {
                    "awslogs-group": "/ecs/$APP_NAME",
                    "awslogs-region": "$AWS_REGION",
                    "awslogs-stream-prefix": "ecs"
                }
            }
        }
    ]
}
EOL

# Create CloudFormation template
echo "Creating CloudFormation template..."
cat > aws-deploy/cloudformation.yaml << EOL
AWSTemplateFormatVersion: '2010-09-09'
Description: 'VibeAI News Infrastructure'

Resources:
  VPC:
    Type: AWS::EC2::VPC
    Properties:
      CidrBlock: 10.0.0.0/16
      EnableDnsHostnames: true
      EnableDnsSupport: true
      Tags:
        - Key: Name
          Value: VibeAI-News-VPC

  PublicSubnet1:
    Type: AWS::EC2::Subnet
    Properties:
      VpcId: !Ref VPC
      CidrBlock: 10.0.1.0/24
      AvailabilityZone: !Select [0, !GetAZs '']
      MapPublicIpOnLaunch: true
      Tags:
        - Key: Name
          Value: VibeAI-News-Public-Subnet-1

  InternetGateway:
    Type: AWS::EC2::InternetGateway
    Properties:
      Tags:
        - Key: Name
          Value: VibeAI-News-IGW

  GatewayAttachment:
    Type: AWS::EC2::VPCGatewayAttachment
    Properties:
      VpcId: !Ref VPC
      InternetGatewayId: !Ref InternetGateway

  RouteTable:
    Type: AWS::EC2::RouteTable
    Properties:
      VpcId: !Ref VPC
      Tags:
        - Key: Name
          Value: VibeAI-News-Route-Table

  Route:
    Type: AWS::EC2::Route
    DependsOn: GatewayAttachment
    Properties:
      RouteTableId: !Ref RouteTable
      DestinationCidrBlock: 0.0.0.0/0
      GatewayId: !Ref InternetGateway

  SubnetRouteTableAssociation:
    Type: AWS::EC2::SubnetRouteTableAssociation
    Properties:
      SubnetId: !Ref PublicSubnet1
      RouteTableId: !Ref RouteTable

  ECSCluster:
    Type: AWS::ECS::Cluster
    Properties:
      ClusterName: $ECS_CLUSTER

  ALB:
    Type: AWS::ElasticLoadBalancingV2::LoadBalancer
    Properties:
      Name: vibeai-news-alb
      Scheme: internet-facing
      Subnets: [!Ref PublicSubnet1]
      SecurityGroups: [!Ref ALBSecurityGroup]

  ALBSecurityGroup:
    Type: AWS::EC2::SecurityGroup
    Properties:
      GroupDescription: Security group for ALB
      VpcId: !Ref VPC
      SecurityGroupIngress:
        - IpProtocol: tcp
          FromPort: 80
          ToPort: 80
          CidrIp: 0.0.0.0/0
        - IpProtocol: tcp
          FromPort: 443
          ToPort: 443
          CidrIp: 0.0.0.0/0

  TargetGroup:
    Type: AWS::ElasticLoadBalancingV2::TargetGroup
    Properties:
      Name: vibeai-news-tg
      Port: 8080
      Protocol: HTTP
      TargetType: ip
      VpcId: !Ref VPC
      HealthCheckPath: /

  Listener:
    Type: AWS::ElasticLoadBalancingV2::Listener
    Properties:
      DefaultActions:
        - Type: forward
          TargetGroupArn: !Ref TargetGroup
      LoadBalancerArn: !Ref ALB
      Port: 80
      Protocol: HTTP

Outputs:
  LoadBalancerDNS:
    Description: DNS name of the load balancer
    Value: !GetAtt ALB.DNSName
EOL

# Create deployment instructions
echo "Creating AWS deployment instructions..."
cat > aws-deploy/README.md << EOL
# AWS Deployment Instructions for VibeAI News

## Prerequisites
1. AWS CLI installed and configured
2. Docker installed
3. AWS ECR access
4. Domain name (vibeai.news) registered in Route 53

## Deployment Steps

1. Build and push Docker image:
   \`\`\`bash
   aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin \${AWS_ACCOUNT_ID}.dkr.ecr.$AWS_REGION.amazonaws.com
   docker build -t $ECR_REPOSITORY aws-deploy/
   docker tag $ECR_REPOSITORY:latest \${AWS_ACCOUNT_ID}.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY:latest
   docker push \${AWS_ACCOUNT_ID}.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY:latest
   \`\`\`

2. Deploy CloudFormation stack:
   \`\`\`bash
   aws cloudformation deploy --template-file aws-deploy/cloudformation.yaml --stack-name vibeai-news-stack --capabilities CAPABILITY_IAM
   \`\`\`

3. Register ECS Task Definition:
   \`\`\`bash
   aws ecs register-task-definition --cli-input-json file://aws-deploy/task-definition.json
   \`\`\`

4. Create ECS Service:
   \`\`\`bash
   aws ecs create-service --cluster $ECS_CLUSTER --service-name $ECS_SERVICE --task-definition $ECS_TASK_DEFINITION --desired-count 1 --launch-type FARGATE --network-configuration "awsvpcConfiguration={subnets=[subnet-xxxxxx],securityGroups=[sg-xxxxxx],assignPublicIp=ENABLED}" --load-balancers "targetGroupArn=arn:aws:elasticloadbalancing:$AWS_REGION:\${AWS_ACCOUNT_ID}:targetgroup/vibeai-news-tg/xxxxxx,containerName=$APP_NAME,containerPort=8080"
   \`\`\`

5. Set up Route 53 (if using AWS for DNS):
   \`\`\`bash
   aws route53 change-resource-record-sets --hosted-zone-id YOUR_HOSTED_ZONE_ID --change-batch '{"Changes":[{"Action":"CREATE","ResourceRecordSet":{"Name":"vibeai.news","Type":"A","AliasTarget":{"HostedZoneId":"Z35SXDOTRQ7X7K","DNSName":"\${ALB_DNS_NAME}","EvaluateTargetHealth":false}}}]}'
   \`\`\`

6. Set up SSL (using AWS Certificate Manager):
   \`\`\`bash
   aws acm request-certificate --domain-name vibeai.news --validation-method DNS
   \`\`\`

## Monitoring
- CloudWatch Logs: /ecs/$APP_NAME
- CloudWatch Metrics: ECS Service metrics
- Application Load Balancer metrics

## Scaling
- Configure Auto Scaling based on CPU/Memory usage
- Set up CloudWatch Alarms for monitoring
EOL

echo "AWS deployment package created in 'aws-deploy' directory"
echo "Please follow the instructions in aws-deploy/README.md to deploy to AWS" 