#!/bin/bash

# Build the application
echo "Building the application..."
./gradlew shadowJar

# Create deployment directory
echo "Creating deployment directory..."
mkdir -p deploy

# Copy the JAR and configuration files
echo "Copying files to deployment directory..."
cp build/libs/vibeai-news.jar deploy/
cp src/main/resources/application.conf deploy/

# Create systemd service file
echo "Creating systemd service file..."
cat > deploy/vibeai-news.service << EOL
[Unit]
Description=VibeAI News Service
After=network.target

[Service]
Type=simple
User=www-data
WorkingDirectory=/var/www/vibeai.news
ExecStart=/usr/bin/java -jar /var/www/vibeai.news/vibeai-news.jar
Restart=always

[Install]
WantedBy=multi-user.target
EOL

# Create deployment instructions
echo "Creating deployment instructions..."
cat > deploy/README.md << EOL
# VibeAI News Deployment Instructions

1. Copy files to server:
   \`\`\`bash
   scp -r deploy/* user@vibeai.news:/var/www/vibeai.news/
   \`\`\`

2. SSH into the server:
   \`\`\`bash
   ssh user@vibeai.news
   \`\`\`

3. Set up the service:
   \`\`\`bash
   sudo cp /var/www/vibeai.news/vibeai-news.service /etc/systemd/system/
   sudo systemctl daemon-reload
   sudo systemctl enable vibeai-news
   sudo systemctl start vibeai-news
   \`\`\`

4. Set up Nginx (if not already configured):
   \`\`\`bash
   sudo apt-get install nginx
   sudo cp /var/www/vibeai.news/nginx.conf /etc/nginx/sites-available/vibeai.news
   sudo ln -s /etc/nginx/sites-available/vibeai.news /etc/nginx/sites-enabled/
   sudo nginx -t
   sudo systemctl restart nginx
   \`\`\`

5. Check the service status:
   \`\`\`bash
   sudo systemctl status vibeai-news
   \`\`\`
EOL

# Create Nginx configuration
echo "Creating Nginx configuration..."
cat > deploy/nginx.conf << EOL
server {
    listen 80;
    server_name vibeai.news www.vibeai.news;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location /static/ {
        alias /var/www/vibeai.news/static/;
        expires 30d;
        add_header Cache-Control "public, no-transform";
    }
}
EOL

echo "Deployment package created in 'deploy' directory"
echo "Please follow the instructions in deploy/README.md to deploy the application" 