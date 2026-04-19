#!/bin/bash

# === CONFIG ===
PROJECT_NAME="restrunner-web"
TOMCAT_PATH="/Users/ansh/Desktop/Clg/Java/apache-tomcat-11.0.20"
WAR_FILE="target/${PROJECT_NAME}.war"

echo "🔨 Building project..."

echo "🛑 Stopping Tomcat..."
$TOMCAT_PATH/bin/shutdown.sh

sleep 3

echo "🧹 Removing old deployment..."
rm -rf $TOMCAT_PATH/webapps/$PROJECT_NAME
rm -f $TOMCAT_PATH/webapps/${PROJECT_NAME}.war

echo "📦 Copying new WAR..."
cp $WAR_FILE $TOMCAT_PATH/webapps/

echo "🚀 Starting Tomcat..."
$TOMCAT_PATH/bin/startup.sh

echo "✅ Deployment complete!"
echo "🌐 Open: http://localhost:8080/$PROJECT_NAME"