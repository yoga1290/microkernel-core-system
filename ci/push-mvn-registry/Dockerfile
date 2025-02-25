FROM maven:3.9.6-eclipse-temurin-21-alpine

ENV GIT_REPOSITORY_NAME="spring-core-system"
ENV MVN_PROFILE=github:TODO
ENV USERNAME=USERNAME:TODO
ENV ACCESS_TOKEN=ACCESS_TOKEN:TODO
ENV GITLAB_PROJECT_ID=GITLAB_PROJECT_ID:TODO
ENV M2_SETTINGS_OUTPUT="/root/.m2"
ENV CI_DIR_PATH="./ci/push-mvn-registry"
# see https://hub.docker.com/_/maven

WORKDIR /usr/app
COPY . .

CMD 'bash' '+x' "$CI_DIR_PATH/mvn-deploy.sh"