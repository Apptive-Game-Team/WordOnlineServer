#!/bin/bash
set -e

# build
./gradlew clean build

DEPLOY_PATH=deploy/word-online-server

PROJECT_ROOT=$(pwd)
BUILD_PATH=$(ls $PROJECT_ROOT/build/libs/*.jar)
JAR_NAME=$(basename $BUILD_PATH)

# Copy To Server
scp $BUILD_PATH $DEPLOY_USER@$DEPLOY_SERVER:$DEPLOY_PATH

# Run Application
ssh $DEPLOY_USER@$DEPLOY_SERVER << EOF
CURRENT_PID=\$(pgrep -f $JAR_NAME)

if [ -z "\$CURRENT_PID" ]
then
  echo "No running process"
  sleep 1
else
  echo "Stopping process \$CURRENT_PID"
  kill -15 \$CURRENT_PID
  sleep 5
fi

export DATABASE_PW=$DATABASE_PW
export DATABASE_URL=$DATABASE_URL
export DATABASE_USER=$DATABASE_USER
export JWT_SECRET=$JWT_SECRET
export DISCORD_WEBHOOK_URL=$DISCORD_WEBHOOK_URL

nohup java -jar $DEPLOY_PATH/$JAR_NAME > $DEPLOY_PATH/app.log 2>&1 &
EOF
